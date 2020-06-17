package com.cym.config;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.cym.controller.adminPage.CertController;
import com.cym.controller.adminPage.ConfController;
import com.cym.model.Cert;
import com.cym.service.LogService;
import com.cym.service.SettingService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Configuration // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling // 2.开启定时任务
public class ScheduleTask {

	final
	SqlHelper sqlHelper;
	final
	CertController certController;
	final
	SettingService settingService;
	final
	ConfController confController;
	final
	LogService logInfoService;

	public ScheduleTask(SqlHelper sqlHelper, CertController certController,
						SettingService settingService, ConfController confController, LogService logInfoService) {
		this.sqlHelper = sqlHelper;
		this.certController = certController;
		this.settingService = settingService;
		this.confController = confController;
		this.logInfoService = logInfoService;
	}

	@PostConstruct
	public void runTest() {
	}

	// 使用TimeUnit.DAYS.toMillis()进行时间粒度转换。Modified by Sai on 2020-6-17.
	@Scheduled(cron = "0 0 2 * * ?")
	public void mongodbTasks() {
		List<Cert> certList = sqlHelper.findAll(Cert.class);

		System.out.println("检查需要续签的证书");
		long time = System.currentTimeMillis();
		for (Cert cert : certList) {
			if (cert.getMakeTime() != null && cert.getAutoRenew() == 1 && time - cert.getMakeTime() > TimeUnit.DAYS.toMillis(59)) {
				System.out.println(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " 开始续签证书:" + cert.getDomain());
				// 大于60天的续签
				certController.renew(cert.getId());
			}
		}
	}

	// 分隔日志,每天
	@Scheduled(cron = "0 0 0 * * ?")
	public void diviLog() {
		if (FileUtil.exist(InitConfig.home + "log/access.log")) {

			String date = DateUtil.format(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)), "yyyy-MM-dd_HH-mm-ss");
			// 分隔日志
			File dist = new File(InitConfig.home + "log/access." + date + ".log");
			FileUtil.move(new File(InitConfig.home + "log/access.log"), dist, true);
			ZipUtil.zip(dist); // 打包
			FileUtil.del(dist); // 删除原文件
			// 重启Nginx。
			confController.reload(null, null, null);
			// 马上解析分隔出来的日志
			logInfoService.buildDataGroup(InitConfig.home + "log/access." + date + ".zip");
		}

		// 删掉7天前日志文件(zip)
		long time = System.currentTimeMillis();
		File dir = new File(InitConfig.home + "log/");
		Optional.ofNullable(dir.listFiles()).ifPresent(fileList ->
				Arrays.stream(fileList)
						.filter(file -> file.getName().contains("access") && file.getName().endsWith(".zip"))
						.forEach(file -> {
							DateTime date = DateUtil.parse(file.getName().replace("access.", "").replace(".zip", ""), "yyyy-MM-dd_HH-mm-ss");
							if (time - date.getTime() > TimeUnit.DAYS.toMillis(7)) {
								FileUtil.del(file);
							}
						})

		);
	}

}