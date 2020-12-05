package com.cym.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.cym.controller.adminPage.CertController;
import com.cym.controller.adminPage.ConfController;
import com.cym.controller.adminPage.RemoteController;
import com.cym.model.Cert;
import com.cym.model.Http;
import com.cym.model.Remote;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.HttpService;
import com.cym.service.LogService;
import com.cym.service.RemoteService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.MessageUtils;
import com.cym.utils.SendMailUtils;
import com.cym.utils.TelnetUtils;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Configuration // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling // 2.开启定时任务
public class ScheduleTask {

	@Value("${server.port}")
	Integer port;

	final SqlHelper sqlHelper;
	final CertController certController;
	final SettingService settingService;
	final ConfController confController;
	final RemoteController remoteController;
	final RemoteService remoteService;
	final UpstreamService upstreamService;
	final LogService logInfoService;
	final SendMailUtils sendMailUtils;
	final HttpService httpService;
	final MessageUtils m;

	public ScheduleTask(MessageUtils m, UpstreamService upstreamService, RemoteService remoteService, SendMailUtils sendMailUtils, RemoteController remoteController, SqlHelper sqlHelper,
			CertController certController, SettingService settingService, ConfController confController, LogService logInfoService, HttpService httpService) {
		this.sqlHelper = sqlHelper;
		this.upstreamService = upstreamService;
		this.remoteService = remoteService;
		this.certController = certController;
		this.settingService = settingService;
		this.confController = confController;
		this.logInfoService = logInfoService;
		this.remoteController = remoteController;
		this.sendMailUtils = sendMailUtils;
		this.httpService = httpService;
		this.m = m;
	}

	// 使用TimeUnit.DAYS.toMillis()进行时间粒度转换。Modified by Sai on 2020-6-17.
	@Scheduled(cron = "0 0 2 * * ?")
	public void certTasks() {
		List<Cert> certList = sqlHelper.findAll(Cert.class);

		// 检查需要续签的证书
		long time = System.currentTimeMillis();
		for (Cert cert : certList) {
			// 大于50天的续签
			if (cert.getMakeTime() != null && cert.getAutoRenew() == 1 && time - cert.getMakeTime() > TimeUnit.DAYS.toMillis(50)) {
				certController.apply(cert.getId(), "renew");
			}
		}
	}

	// 分隔日志,每天
	@Scheduled(cron = "0 55 23 * * ?")
	public void diviLog() {
		Http http = httpService.getName("access_log");
		String path = null;
		if (http != null && StrUtil.isNotEmpty(http.getValue())) {
			path = http.getValue().split(" ")[0];
		}

		if (StrUtil.isNotEmpty(path) && FileUtil.exist(path)) {

			String date = DateUtil.format(new Date(), "yyyy-MM-dd");
			// 分隔日志
			File dist = new File(InitConfig.home + "log/access." + date + ".log");
			FileUtil.move(new File(InitConfig.home + "log/access.log"), dist, true);
			ZipUtil.zip(dist); // 打包
			FileUtil.del(dist); // 删除原文件
			// 重载Nginx。
			confController.reload(null, null, null);
			// 马上解析分隔出来的日志
			logInfoService.buildDataGroup(InitConfig.home + "log/access." + date + ".zip");
			logInfoService.clearDb();
		}

		// 删除多余文件
		delCache();
	}

	public void delCache() {
		// 删掉7天前日志文件(zip)
		long time = System.currentTimeMillis();
		File dir = new File(InitConfig.home + "log/");
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (file.getName().contains("access.") && file.getName().endsWith(".zip")) {
					String dateStr = file.getName().replace("access.", "").replace(".zip", "");
					DateTime date = null;
					if (dateStr.length() != 10) {
						FileUtil.del(file);
					} else {
						date = DateUtil.parse(dateStr, "yyyy-MM-dd");
						if (time - date.getTime() > TimeUnit.DAYS.toMillis(8)) {
							FileUtil.del(file);
						}
					}
				}
			}
		}

		// 删除7天前的备份
		dir = new File(InitConfig.home + "bak/");
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (file.getName().contains("nginx.conf.") && (file.getName().endsWith(".zip") || file.getName().endsWith(".bak"))) {
					String dateStr = file.getName().replace("nginx.conf.", "").replace(".zip", "").replace(".bak", "").split("_")[0];
					DateTime date = null;
					if (dateStr.length() != 10) {
						FileUtil.del(file);
					} else {
						date = DateUtil.parse(dateStr, "yyyy-MM-dd");
						if (time - date.getTime() > TimeUnit.DAYS.toMillis(8)) {
							FileUtil.del(file);
						}
					}
				}
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
					e.printStackTrace();

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

	// 检查节点情况
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