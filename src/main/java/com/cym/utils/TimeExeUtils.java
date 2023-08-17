package com.cym.utils;

import cn.hutool.core.util.ArrayUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Component
public class TimeExeUtils {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Inject
	MessageUtils m;

	/**
	 * 命令执行
	 * 
	 * @param cmd
	 * @param envs
	 * @param timeout
	 * @return
	 */
	public String execCMD(String cmd, String[] envs, long timeout) {
		Process process = null;
		StringBuilder sbStd = new StringBuilder();

		String[] allEnvs = ArrayUtil.addAll(System.getenv() //
				.entrySet()//
				.stream()//
				.map(r -> String.format("%s=%s", r.getKey(), r.getValue()))//
				.toArray(String[]::new), envs);

		long start = System.currentTimeMillis();
		try {
			process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd }, allEnvs);

			BufferedReader brStd = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			while (true) {
				if (brStd.ready()) {
					line = brStd.readLine();
					sbStd.append(line + "\n");
					logger.info(line);
					continue;
				}

				if (process != null) {
					try {
						process.exitValue();
						break;
					} catch (IllegalThreadStateException e) {
						//System.err.println(e.getMessage());
					}
				}

				if (System.currentTimeMillis() - start > timeout) {
					line = m.get("certStr.timeout");

					sbStd.append(line + "\n");
					logger.info(line);
					break;
				}

				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					//System.err.println(e.getMessage());
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}

		return sbStd.toString();
	}
}