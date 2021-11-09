package com.cym.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cym.NginxWebUI;
import com.cym.controller.adminPage.ConfController;
import com.cym.utils.MessageUtils;
import com.cym.utils.TimeExeUtils;

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
		String code = "Wb7cQZCHt3oXYXpCLG9V7nifzqAOdf5jnhJoTOCT8vk";
	}

}
