package com.cym.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;

@Component
public class UpdateUtils {
	@Value("${server.port}")
	String port;
	@Value("${project.home}")
	String home;

	@Value("${spring.database.type:}")
	String type;
	@Value("${spring.datasource.url:}")
	String url;
	@Value("${spring.datasource.username:}")
	String username;
	@Value("${spring.datasource.password:}")
	String password;

	private static final Logger LOG = LoggerFactory.getLogger(UpdateUtils.class);

	public void run(String path) {
		ThreadUtil.safeSleep(2000);

		String cmd = "mv " + path + " " + path.replace(".update", "");
		LOG.info(cmd);
		RuntimeUtil.exec(cmd);

		String param = " --server.port=" + port + " --project.home=" + home;
		if (!"sqlite".equals(type)) {
			param += " --spring.database.type=" + type //
					+ " --spring.datasource.url=" + url //
					+ " --spring.datasource.username=" + username //
					+ " --spring.datasource.password=" + password;
		}

		cmd = "nohup java -jar -Xmx64m " + path.replace(".update", "") + param + " > /dev/null &";
		LOG.info(cmd);
		RuntimeUtil.exec(cmd);

	}

}
