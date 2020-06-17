package com.cym.test;

import cn.craccd.sqlHelper.utils.SqlHelper;
import com.cym.NginxWebUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NginxWebUI.class)
public class MainTest {
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

}
