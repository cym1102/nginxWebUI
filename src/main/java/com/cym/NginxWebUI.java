package com.cym;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.cym.utils.SystemTool;

import cn.hutool.core.util.RuntimeUtil;

@EnableTransactionManagement
@SpringBootApplication
public class NginxWebUI {

	public static void main(String[] args) {

		// 尝试杀掉旧版本
		if (SystemTool.isLinux()) {
			killSelf();
		}

		SpringApplication.run(NginxWebUI.class, args);
	}

	public static void killSelf() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String myPid = runtimeMXBean.getName().split("@")[0];
		List<String> list = RuntimeUtil.execForLines("ps -ef");

		List<String> pids = new ArrayList<String>();
		for (String line : list) {
			if (line.contains("nginxWebUI")) {
				String[] strs = line.split("\\s+");
				if (!strs[1].equals(myPid)) {
					pids.add(strs[1]);
				}
			}
		}

		for (String pid : pids) {
			//System.out.println("杀掉进程:" + pid);
			RuntimeUtil.exec("kill -9 " + pid);
		}

	}

}
