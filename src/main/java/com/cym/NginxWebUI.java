package com.cym;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@EnableTransactionManagement
@SpringBootApplication
public class NginxWebUI {
	static Logger logger = LoggerFactory.getLogger(NginxWebUI.class);

	public static void main(String[] args) {

		// 尝试杀掉旧版本
		killSelf();

		// 删掉多余的jar
		removeJar();

		// 启动springboot
		SpringApplication.run(NginxWebUI.class, args);
	}

	public static void killSelf() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String myPid = runtimeMXBean.getName().split("@")[0];

		List<String> list = new ArrayList<String>();
		List<String> pids = new ArrayList<String>();

		if (SystemTool.isWindows()) {
			list = RuntimeUtil.execForLines("wmic process get commandline,ProcessId /value");
			pids = new ArrayList<String>();

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).contains("java") && list.get(i).contains("nginxWebUI") && list.get(i).contains(".jar")) {
					String pid = list.get(i + 2).split("=")[1];
					if (!pid.equals(myPid)) {
						pids.add(pid);
					}
				}
			}
		} else {
			list = RuntimeUtil.execForLines("ps -ef");
			for (String line : list) {
				if (line.contains("java") && line.contains("nginxWebUI") && line.contains(".jar")) {
					String[] strs = line.split("\\s+");
					String pid = strs[1];

					if (!pid.equals(myPid)) {
						pids.add(pid);
					}
				}
			}
		}

		for (String pid : pids) {
			logger.info("杀掉进程:" + pid);
			if (SystemTool.isWindows()) {
				RuntimeUtil.exec("taskkill /im " + pid + " /f");
			} else {
				RuntimeUtil.exec("kill -9 " + pid);
			}
		}

	}

	private static void removeJar() {
		ApplicationHome home = new ApplicationHome(NginxWebUI.class);
		File jar = home.getSource();

		File[] list = jar.getParentFile().listFiles();
		for (File file : list) {
			if (file.getName().startsWith("nginxWebUI") && file.getName().endsWith(".jar") && !file.getName().equals(jar.getName())) {
				FileUtil.del(file);
				logger.info("删除文件:" + file);
			}
		}
	}

}
