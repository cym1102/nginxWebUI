package com.cym.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import cn.hutool.core.util.RuntimeUtil;

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

		List<String> list = RuntimeUtil.execForLines("wmic process get commandline,ProcessId /value");
		List<String> pids = new ArrayList<String>();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).contains("java") && list.get(i).contains("nginxWebUI") && list.get(i).contains(".jar")) {
				String pid = list.get(i + 2).split("=")[1];
				pids.add(pid);
			}
		}
		
		for (String pid : pids) {
			logger.info("杀掉进程:" + pid);
			RuntimeUtil.exec("taskkill /im " + pid + " /f");
		}
		
	}

}
