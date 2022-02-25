package com.cym;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.noear.solon.Solon;
import org.noear.solon.schedule.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.utils.JarUtil;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;

@EnableScheduling
public class NginxWebUI {
	static Logger logger = LoggerFactory.getLogger(NginxWebUI.class);

	public static void main(String[] args) {
		try {
			// 尝试杀掉旧版本
			killSelf(args);

			// 删掉多余的jar
			removeJar();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		Solon.start(NginxWebUI.class, args, app -> {
			app.onError(e -> logger.info(e.getMessage(), e));

			app.before(c -> {
				String path = c.path();
				while (path.contains("//")) {
					path = path.replace("//", "/");
				}
				c.pathNew(path);
			});

			app.onEvent(freemarker.template.Configuration.class, cfg -> {
				cfg.setSetting("classic_compatible", "true");
				cfg.setSetting("number_format", "0.##");
			});

		});
	}

	public static void killSelf(String[] args) {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String myPid = runtimeMXBean.getName().split("@")[0];

		List<String> list = new ArrayList<String>();
		Set<String> pids = new HashSet<String>();

		if (SystemTool.isWindows()) {
			String port = getPort(args);
			list = RuntimeUtil.execForLines("netstat -aon");
			for (String line : list) {
				if (line.contains(":" + port) && line.contains("LISTENING")) {
					String pid = line.split("LISTENING")[1].trim();
					if (!pid.equals(myPid)) {
						pids.add(pid);
					}
				}
			}
		} else if (SystemTool.isLinux()) {
			list = RuntimeUtil.execForLines("ps -ef");
			for (String line : list) {
				if (line.contains("java") && line.contains("nginxWebUI") && line.contains(".jar")) {
					String pid = line.split("\\s+")[1].trim();
					if (!pid.equals(myPid)) {
						pids.add(pid);
					}
				}
			}
		}

		for (String pid : pids) {
			logger.info("杀掉旧进程:" + pid);
			if (SystemTool.isWindows()) {
				RuntimeUtil.exec("taskkill /im " + pid + " /f");
			} else if (SystemTool.isLinux()) {
				RuntimeUtil.exec("kill -9 " + pid);
			}
		}

	}

	private static String getPort(String[] args) {
		for (String arg : args) {
			if (arg.contains("--server.port")) {
				return arg.split("=")[1];
			}
		}
		return "8080";
	}

	private static void removeJar() {
		File[] list = new File(JarUtil.getCurrentFilePath()).getParentFile().listFiles();
		for (File file : list) {
			if (file.getName().startsWith("nginxWebUI") && file.getName().endsWith(".jar") && !file.getPath().equals(JarUtil.getCurrentFilePath())) {
				FileUtil.del(file);
				logger.info("删除文件:" + file);
			}
		}
	}

}
