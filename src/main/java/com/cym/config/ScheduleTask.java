package com.cym.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.cym.controller.adminPage.CertController;
import com.cym.controller.adminPage.ConfController;
import com.cym.model.Cert;
import com.cym.model.Log;
import com.cym.service.LogService;
import com.cym.service.SettingService;
import com.cym.utils.SystemTool;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Configuration // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling // 2.开启定时任务
public class ScheduleTask {

	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	CertController certController;
	@Autowired
	SettingService settingService;
	@Autowired
	ConfController confController;
	@Autowired
	LogService logInfoService;
	
	@PostConstruct
	public void runTest() {
//		logInfoService.buildDataGroup(InitConfig.home + "log/access.2020-06-08_00-00-00.zip");
//		logInfoService.buildDataGroup(InitConfig.home + "log/access.2020-06-09_00-00-00.zip");
	}
	
	
	@Scheduled(cron = "0 0 2 * * ?")
	public void mongodbTasks() {
		List<Cert> certList = sqlHelper.findAll(Cert.class);

		System.out.println("检查需要续签的证书");
		long time = System.currentTimeMillis();
		for (Cert cert : certList) {
			if (cert.getMakeTime() != null && cert.getAutoRenew() == 1 && time - cert.getMakeTime() > 59 * 24 * 60 * 60 * 1000) {
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
			
			
			String date = DateUtil.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000), "yyyy-MM-dd_HH-mm-ss");
			// 分隔日志
			File dist = new File(InitConfig.home + "log/access." + date + ".log");
			FileUtil.move(new File(InitConfig.home + "log/access.log"), dist, true);
			ZipUtil.zip(dist);
			FileUtil.del(dist);
			confController.reload(null, null, null);
			
			// 马上解析分隔出来的日志
			logInfoService.buildDataGroup(InitConfig.home + "log/access." + date + ".zip");
			
		}
		
		// 删掉7天前日志
		Long time = System.currentTimeMillis();
		File dir = new File(InitConfig.home + "log/");
		File[] fileList = dir.listFiles();
		for (File file : fileList) {
			if (file.getName().contains("access") && file.getName().contains(".zip")) {
				DateTime date = DateUtil.parse(file.getName().replace("access.", "").replace(".zip", ""), "yyyy-MM-dd_HH-mm-ss");
				if(time - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
					FileUtil.del(file);
				}
			}
		}

	}

}