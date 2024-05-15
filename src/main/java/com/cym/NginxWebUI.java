package com.cym;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.utils.JarUtil;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;

@EnableScheduling
@SolonMain
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
			app.onError(e -> logger.error(e.getMessage(), e));

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

		List<String> pids = getProcessId();
		for (String pid : pids) {
			if (!pid.equals(myPid)) {
				logger.info("杀掉旧进程:" + pid);
				if (SystemTool.isWindows()) {
					RuntimeUtil.exec("taskkill /F /PID " + pid);
					ThreadUtil.safeSleep(1000); // win下等待1秒,否则文件不会被释放
				} else if (SystemTool.isLinux()) {
					RuntimeUtil.exec("kill -9 " + pid);
				}
			}
		}

	}

	private static List<String> getProcessId() {
		List<String> pids = new ArrayList<>();

		if (SystemTool.isWindows()) {
			List<String> list = RuntimeUtil.execForLines("wmic process where \"CommandLine like '%nginxWebUI%'\" get ProcessId,CommandLine");

			for (String line : list) {
				if (line.contains(".jar")) {
					String[] lines = line.split("\\s+");
					pids.add(lines[lines.length - 1]);
				}
			}
		} else {
			List<String> list = RuntimeUtil.execForLines("/bin/sh", "-c", "ps -ef | grep nginxWebUI");

			for (String line : list) {
				if (line.contains(".jar")) {
					String[] lines = line.split("\\s+");
					pids.add(lines[1]);
				}
			}
		}

		return pids;
	}

	private static void removeJar() {
		File[] list = new File(JarUtil.getCurrentFilePath()).getParentFile().listFiles();
		for (File file : list) {
			logger.info("文件:" + file);
			if (file.getName().startsWith("nginxWebUI") && file.getName().endsWith(".jar") && !file.getPath().equals(JarUtil.getCurrentFilePath())) {
				FileUtil.del(file);
				logger.info("删除文件:" + file);
			}
		}
	}

}
