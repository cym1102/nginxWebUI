package com.cym.utils;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

@Component
public class UpdateUtils {
	@Inject("${server.port}")
	String port;
	@Inject("${project.home}")
	String home;
	@Inject("${knife4j.production:}")
	String production;

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

		String cmd = "mv " + path + " " + path.replace(".update", "");
		LOG.info(cmd);
		RuntimeUtil.exec(cmd);

		String param = " --server.port=" + port //
				+ " --project.home=" + home;
		
		if (StrUtil.isNotEmpty(production)) {
			param += " --knife4j.production=" + production;
		}
		
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
