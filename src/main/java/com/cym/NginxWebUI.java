package com.cym;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

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

	public static void main(String[] args) {

//		if (!SystemTool.hasRoot()) {
//			System.err.println("请使用root用户运行该程序。 Please use root to run this program ");
//			return;
//		}

		if (SystemTool.isLinux()) {
			// 尝试杀掉旧版本
			killSelf();

			// 删掉多余的jar
			removeJar();
		}

		SpringApplication.run(NginxWebUI.class, args);
	}

	public static void killSelf() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String myPid = runtimeMXBean.getName().split("@")[0];
		List<String> list = RuntimeUtil.execForLines("ps -ef");

		List<String> pids = new ArrayList<String>();
		for (String line : list) {
			if (line.contains("java") && line.contains("nginxWebUI") && line.contains(".jar") ) {
				String[] strs = line.split("\\s+");
				if (!strs[1].equals(myPid)) {
					pids.add(strs[1]);
				}
			}
		}

		for (String pid : pids) {
			// System.out.println("杀掉进程:" + pid);
			RuntimeUtil.exec("kill -9 " + pid);
		}

	}

	private static void removeJar() {
		ApplicationHome home = new ApplicationHome(NginxWebUI.class);
		File jar = home.getSource();

		File[] list = jar.getParentFile().listFiles();
		for (File file : list) {
			System.out.println(file);
			if (file.getName().startsWith("nginxWebUI") && file.getName().endsWith(".jar") && !file.getName().equals(jar.getName())) {
				boolean rs = FileUtil.del(file);
				System.err.println("del " + file + " " + rs);
			}
		}
	}

}
