package com.cym.test;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cym.NginxWebUI;
import com.cym.controller.adminPage.ConfController;
import com.cym.utils.TimeExeUtils;
import com.cym.utils.MessageUtils;

import cn.craccd.sqlHelper.utils.SqlHelper;

@SpringBootTest(classes = NginxWebUI.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	MessageUtils m;
	@Autowired
	ConfController confController;
	@Autowired
	TimeExeUtils exeUtils;

	@Test
	public void testStartUp() throws InterruptedException, IOException {

		String rs = exeUtils.execCMD("ping www.baidu.com", null, 10000);
		
		System.err.println(rs);
	}

}
