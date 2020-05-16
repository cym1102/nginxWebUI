package com.cym.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cym.NginxWebUI;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.RuntimeUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NginxWebUI.class)
public class MainTest {
	@Autowired
	SqlHelper sqlHelper;
	@Before
	public void before() {
		System.out.println("--------------测试开始----------");
	}

	@Test
	public void testStartUp()  {
		
	}

	@After
	public void after() {
		System.out.println("--------------测试结束----------");
	}
	
	
	public static void main(String[] args) {
		String command = "cmd /c powershell cd E:\\nginx-1.17.10; ./nginx.exe -t;";
		//PowerShellResponse response = PowerShell.executeSingleCommand(command);
		String rs = RuntimeUtil.execForStr(command);
		System.err.println(rs);
	}

}
