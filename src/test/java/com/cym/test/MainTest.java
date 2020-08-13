package com.cym.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cym.NginxWebUI;
import com.cym.service.MonitorService;
import com.cym.service.SettingService;
import com.cym.utils.MessageUtils;
import com.cym.utils.SendMailUtils;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@SpringBootTest(classes = NginxWebUI.class)
public class MainTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SqlHelper sqlHelper;
	@Value("${project.version}")
	String version;
	@Autowired
	MonitorService monitorService;
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	SendMailUtils sendMailUtils;
	@Autowired
	SettingService settingService;
	@Autowired
	MessageUtils m;
	
	@BeforeAll
	static void before() {
		System.out.println("--------------测试开始----------");
	}

	@Test
	public void testStartUp() throws InterruptedException {
//		for(int i=0;i<100;i++) {
//			LogInfo logInfo = new LogInfo();
//			logInfo.setRemoteAddr("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
//		
//			sqlHelper.insert(logInfo);
//		}
		
//		sqlHelper.deleteByQuery(new ConditionAndWrapper(), LogInfo.class);
//		jdbcTemplate.execute("vacuum;");
		
		String mail = settingService.get("mail");
		sendMailUtils.sendMailSmtp(mail, m.get("mailStr.upstreamFail"), m.get("mailStr.upstreamFail") + StrUtil.join(" ", "127.0.0.1"));
	}

	@AfterAll
	static void after() {
		System.out.println("--------------测试结束----------");
	}



}
