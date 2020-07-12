package com.cym.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cym.service.SettingService;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;

@Component
public class SendMailUtils {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	SettingService settingService;

	public void sendMailSmtp(String to, String title, String msg) {

		MailAccount account = new MailAccount();
		account.setHost(settingService.get("mail_host"));
		if (settingService.get("mail_port") != null) {
			account.setPort(Integer.parseInt(settingService.get("mail_port")));
		}
		account.setAuth(true);
		account.setFrom(settingService.get("mail_from"));
		account.setUser(settingService.get("mail_user"));
		account.setPass(settingService.get("mail_pass"));
		if (settingService.get("mail_ssl") != null) {
			account.setSslEnable(Boolean.parseBoolean(settingService.get("mail_ssl")));
		}

		MailUtil.send(account, to, title, msg, false); 
		logger.info("发送邮件: " + to);
	}
}
