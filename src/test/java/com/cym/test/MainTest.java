package com.cym.test;

import java.io.File;

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
import com.cym.model.Version;
import com.cym.service.MonitorService;
import com.cym.service.SettingService;
import com.cym.utils.MessageUtils;
import com.cym.utils.SendMailUtils;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

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

	public static void main(String[] args) {
		Process ps = RuntimeUtil.exec(new String[] {}, new File("d:/"), "tasklist");
		String rs = RuntimeUtil.getResult(ps);
		System.out.println(rs);
	}
}
