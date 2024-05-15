package com.cym.utils;

import java.io.File;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.system.SystemUtil;

@Component
public class UpdateUtils {
	@Inject("${server.port}")
	String port;
	@Inject("${project.home}")
	String home;

	@Inject("${spring.database.type:}")
	String type;
	@Inject("${spring.datasource.url:}")
	String url;
	@Inject("${spring.datasource.username:}")
	String username;
	@Inject("${spring.datasource.password:}")
	String password;

	private static final Logger LOG = LoggerFactory.getLogger(UpdateUtils.class);

	public void run(String path) {
		ThreadUtil.safeSleep(2000);
		
		// linux更新,去掉版本号并覆盖源文件
		if(!SystemUtil.getOsInfo().isWindows()) {
			String jarPath = JarUtil.getCurrentFile().getParent() + File.separator + "nginxWebUI.jar";
			FileUtil.rename(new File(path), jarPath, true);
			path = jarPath;
		}

		String param = " --server.port=" + port + " --project.home=" + home;

		if ("mysql".equalsIgnoreCase(type)) {
			param += " --spring.database.type=" + type //
					+ " --spring.datasource.url=" + url //
					+ " --spring.datasource.username=" + username //
					+ " --spring.datasource.password=" + password;
		}

		String cmd = null;
		if (SystemTool.isWindows()) {
			cmd = "java -jar -Dfile.encoding=UTF-8 " + path + param;
		} else {
			cmd = "nohup java -jar -Dfile.encoding=UTF-8 " + path + param + " > /dev/null &";
		}

		LOG.info(cmd);
		RuntimeUtil.exec(cmd);
	}

}
