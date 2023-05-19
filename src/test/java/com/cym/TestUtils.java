package com.cym;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit4ClassRunner;
import org.noear.solon.test.SolonTest;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;

@RunWith(SolonJUnit4ClassRunner.class)
@SolonTest(NginxWebUI.class)
public class TestUtils extends HttpTester {
	@Test
	@Ignore
	public void test() throws Exception {

		StringBuilder pass = new StringBuilder(1000);
		for (int i = 0; i < 2000000; i++) {
			pass.append("abcde12345");
		}

		Map<String, Object> map = new HashMap<>();
		map.put("name", "admin");
		map.put("pass", pass.toString());

		System.out.println("pass.length: " + pass.length());

		try {
			String rs = path("/adminPage/login/login").data(map).post();
			System.err.println(rs);
			assert false;
		} catch (IOException e) {
			assert true;
		}
	}

	public static void main(String[] args) {
		StringBuilder pass = new StringBuilder(1000);
		for (int i = 0; i < 2000000; i++) {
			pass.append("abcde12345");
		}

		Map<String, Object> map = new HashMap<>();
		map.put("name", "admin");
		map.put("pass", pass.toString());

		String rs = HttpUtil.post("http://127.0.0.1:8080/adminPage/login/login", map);
		System.err.println(rs);
	}
}