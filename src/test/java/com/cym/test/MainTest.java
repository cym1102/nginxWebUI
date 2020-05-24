package com.cym.test;

import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cym.NginxWebUI;
import com.cym.model.Version;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NginxWebUI.class)
public class MainTest {
	@Autowired
	SqlHelper sqlHelper;
	@Value("${project.version}")
	String version;

	@Before
	public void before() {
		System.out.println("--------------测试开始----------");
	}

	@Test
	public void testStartUp() {
		Version version = new Version();
		version.setVersion(this.version.replace("v", ""));
		version.setUrl("http://craccd.oss-cn-beijing.aliyuncs.com/nginxWebUI-" + version.getVersion() + ".jar");

		FileUtil.writeString(JSONUtil.toJsonStr(version), "d:/version.json", Charset.defaultCharset());
	}

	@After
	public void after() {
		System.out.println("--------------测试结束----------");
	}

}
