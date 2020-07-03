package com.cym.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;

@Component
public class AsyncUtils {
	@Value("${server.port}")
	String port;
	@Value("${project.home}")
	String home;
	private static final Logger LOG = LoggerFactory.getLogger(AsyncUtils.class);

	@Async
	public void run(String path) {
		ThreadUtil.safeSleep(2000);
		String cmd = "mv " + path + " " + path.replace(".update", "");
		LOG.info(cmd);
		RuntimeUtil.exec(cmd);
		
		cmd = "nohup java -jar -Xmx64m " + path.replace(".update", "") + " --server.port=" + port + " --project.home=" + home + " > /dev/null &";
		LOG.info(cmd);
		RuntimeUtil.exec(cmd);
	}
}
