package com.cym.test;

import java.util.ArrayList;
import java.util.List;

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

	@Test
	public void testStartUp() throws InterruptedException {

	}

	
}
