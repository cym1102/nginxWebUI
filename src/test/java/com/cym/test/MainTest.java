package com.cym.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.cym.NginxWebUI;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@SpringBootTest(classes = NginxWebUI.class)
public class MainTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SqlHelper sqlHelper;
	@Value("${project.version}")
	String version;

	@BeforeAll
	static void before() {
		System.out.println("--------------测试开始----------");
	}

	@Test
	public void testStartUp() {
		
	}

	@AfterAll
	static void after() {
		System.out.println("--------------测试结束----------");
	}

	
	public static void main(String[] args) {
		FileUtil.copy("D:\\fileserver", "C:\\", true);
	}
}
