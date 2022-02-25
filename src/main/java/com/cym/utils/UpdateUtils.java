package com.cym.utils;

import java.io.File;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;

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

		String newPath = path.replace(".update", "");
		FileUtil.rename(new File(path), newPath, true);

		String param = " --server.port=" + port + " --project.home=" + home;

		if ("mysql".equals(type.toLowerCase())) {
			param += " --spring.database.type=" + type //
					+ " --spring.datasource.url=" + url //
					+ " --spring.datasource.username=" + username //
					+ " --spring.datasource.password=" + password;
		}

		String cmd = null;
		if (SystemTool.isWindows()) {
			cmd = "java -jar -Dfile.encoding=UTF-8 " + newPath + param;
		} else {
			cmd = "nohup java -jar -Dfile.encoding=UTF-8 " + newPath + param + " > /dev/null &";
		}

		LOG.info(cmd);
		RuntimeUtil.exec(cmd);
	}

}
