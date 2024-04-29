package com.cym.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		logger.info(cmd);

		Process process = null;
		StringBuilder sbStd = new StringBuilder();

		long start = System.currentTimeMillis();
		try {

			ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", cmd);
			Map<String, String> environmentMap = processBuilder.environment();
			for (String env : envs) {
				environmentMap.put(env.split("=")[0], env.split("=")[1]);
			}
			processBuilder.redirectErrorStream(true);// 将错误流中的数据合并到输入流
			process = processBuilder.start();

			// 输出正常信息
			BufferedReader brStd = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			while (true) {
				if (brStd.ready()) {
					line = brStd.readLine();
					sbStd.append(line).append("\n");
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

					sbStd.append(line).append("\n");
					logger.info(line);
					break;
				}

				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}

		logger.info(sbStd.toString());
		return sbStd.toString();
	}
}