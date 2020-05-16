package com.cym.config;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.cym.controller.adminPage.CertController;
import com.cym.model.Cert;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.date.DateUtil;

@Configuration // 1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling // 2.开启定时任务
public class ScheduleTask {

	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	CertController certController;

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



}