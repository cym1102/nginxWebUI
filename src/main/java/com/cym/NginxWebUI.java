package com.cym;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.core.util.LogUtil;
import org.noear.solon.logging.utils.LogUtilToSlf4j;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.utils.JarUtil;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
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
			LogUtil.globalSet(new LogUtilToSlf4j());

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
			
			app.router().caseSensitive(true);
		});
	}

	public static void killSelf(String[] args) {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String myPid = runtimeMXBean.getName().split("@")[0];

		List<String> list = new ArrayList<String>();

		list = RuntimeUtil.execForLines("jps");
		for (String line : list) {
			if (line.contains("nginxWebUI") && line.contains(".jar")) {
				String pid = line.split("\\s+")[0].trim();
				if (!pid.equals(myPid)) {
					logger.info("杀掉旧进程:" + pid);
					if (SystemTool.isWindows()) {
						RuntimeUtil.exec("taskkill /im " + pid + " /f");
					} else if (SystemTool.isLinux()) {
						RuntimeUtil.exec("kill -9 " + pid);
					}
				}
			}
		}

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
