package com.cym.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.HomeConfig;
import com.cym.controller.adminPage.CertController;
import com.cym.controller.adminPage.ConfController;
import com.cym.controller.adminPage.RemoteController;
import com.cym.model.Cert;
import com.cym.model.Remote;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.HttpService;
import com.cym.service.LogService;
import com.cym.service.RemoteService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.BLogFileTailer;
import com.cym.utils.MessageUtils;
import com.cym.utils.SendMailUtils;
import com.cym.utils.TelnetUtils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Component
public class ScheduleTask {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject("${server.port}")
	Integer port;
	@Inject("${solon.logging.appender.file.maxHistory}")
	Integer maxHistory;
	@Inject
	SqlHelper sqlHelper;
	@Inject
	CertController certController;
	@Inject
	SettingService settingService;
	@Inject
	ConfController confController;
	@Inject
	RemoteController remoteController;
	@Inject
	RemoteService remoteService;
	@Inject
	UpstreamService upstreamService;
	@Inject
	LogService logInfoService;
	@Inject
	SendMailUtils sendMailUtils;
	@Inject
	HttpService httpService;
	@Inject
	MessageUtils m;
	@Inject
	HomeConfig homeConfig;
	@Inject
	BLogFileTailer bLogFileTailer;

	// 续签证书
	@Scheduled(cron = "0 0 0/2 * * ?")
	public void certTasks() {
		List<Cert> certList = sqlHelper.findListByQuery(new ConditionAndWrapper().in(Cert::getType, new Integer[] { 0, 2 }), Cert.class);

		// 检查需要续签的证书
		long time = System.currentTimeMillis();
		for (Cert cert : certList) {
			// 大于60天的续签
			if (cert.getMakeTime() != null && cert.getAutoRenew() == 1 && time - cert.getMakeTime() > TimeUnit.DAYS.toMillis(60)) {
				certController.apply(cert.getId(), "renew");
			}
		}
	}

	// 检查远程服务器
	@Scheduled(cron = "0/30 * * * * ?")
	public void nginxTasks() {

		String lastNginxSend = settingService.get("lastNginxSend");
		String mail = settingService.get("mail");
		String nginxMonitor = settingService.get("nginxMonitor");
		String mailInterval = settingService.get("mail_interval");
		if (StrUtil.isEmpty(mailInterval)) {
			mailInterval = "30";
		}

		if ("true".equals(nginxMonitor) && StrUtil.isNotEmpty(mail)
				&& (StrUtil.isEmpty(lastNginxSend) || System.currentTimeMillis() - Long.parseLong(lastNginxSend) > TimeUnit.MINUTES.toMillis(Integer.parseInt(mailInterval)))) {
			List<String> names = new ArrayList<>();

			// 测试远程
			List<Remote> remoteList = remoteService.getMonitorRemoteList();
			for (Remote remote : remoteList) {
				try {
					String json = HttpUtil.get(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/remote/version?creditKey=" + remote.getCreditKey(), 1000);
					Map<String, Object> map = JSONUtil.toBean(json, new TypeReference<Map<String, Object>>() {
					}.getType(), false);

					if ((Integer) map.get("nginx") == 0 && remote.getMonitor() == 1) {
						names.add(remote.getDescr() + "[" + remote.getIp() + ":" + remote.getPort() + "]");
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);

					names.add(remote.getDescr() + "[" + remote.getIp() + ":" + remote.getPort() + "]");
				}
			}

			// 测试本地
			if ("1".equals(settingService.get("monitorLocal"))) {
				Map<String, Object> map = remoteController.version();
				if ((Integer) map.get("nginx") == 0) {
					names.add(0, m.get("remoteStr.local") + "[127.0.0.1:" + port + "]");
				}
			}

			if (names.size() > 0) {
				sendMailUtils.sendMailSmtp(mail, m.get("mailStr.nginxFail"), m.get("mailStr.nginxTips") + StrUtil.join(" ", names));
				settingService.set("lastNginxSend", String.valueOf(System.currentTimeMillis()));
			}
		}

	}

	// 检查负载节点情况
	@Scheduled(cron = "0/30 * * * * ?")
	public void nodeTasks() {
		String lastUpstreamSend = settingService.get("lastUpstreamSend");
		String mail = settingService.get("mail");
		String upstreamMonitor = settingService.get("upstreamMonitor");
		String mailInterval = settingService.get("mail_interval");
		if (StrUtil.isEmpty(mailInterval)) {
			mailInterval = "30";
		}

		if ("true".equals(upstreamMonitor)) {

			List<UpstreamServer> upstreamServers = upstreamService.getAllServer();

			List<String> ips = new ArrayList<>();
			for (UpstreamServer upstreamServer : upstreamServers) {
				if (!TelnetUtils.isRunning(upstreamServer.getServer(), upstreamServer.getPort())) {
					Upstream upstream = sqlHelper.findById(upstreamServer.getUpstreamId(), Upstream.class);
					if (upstream.getMonitor() == 1 && StrUtil.isNotEmpty(mail)
							&& (StrUtil.isEmpty(lastUpstreamSend) || System.currentTimeMillis() - Long.parseLong(lastUpstreamSend) > TimeUnit.MINUTES.toMillis(Integer.parseInt(mailInterval)))) {
						ips.add(upstreamServer.getServer() + ":" + upstreamServer.getPort());
					}
					upstreamServer.setMonitorStatus(0);
				} else {
					upstreamServer.setMonitorStatus(1);
				}

				sqlHelper.updateById(upstreamServer);
			}

			if (ips.size() > 0) {
				String dateStr = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
				if (settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {

					SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH);
					dateStr = dateFormat.format(new Date());
				}

				sendMailUtils.sendMailSmtp(mail, m.get("mailStr.upstreamFail"), m.get("mailStr.upstreamTips") + StrUtil.join(" ", ips) + "\r\n" + dateStr);
				settingService.set("lastUpstreamSend", String.valueOf(System.currentTimeMillis()));
			}
		}
	}

}