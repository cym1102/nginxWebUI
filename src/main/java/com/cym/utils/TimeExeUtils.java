package com.cym.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TimeExeUtils {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
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

		long start = System.currentTimeMillis() ;
		try {
			if (envs == null) {
				process = Runtime.getRuntime().exec(cmd);
			} else {
				process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd }, envs);
			}

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
						//e.printStackTrace();
					}
				}

				if (System.currentTimeMillis()  - start > timeout) {
					line = "命令执行超时退出";
					
					sbStd.append(line + "\n");
					logger.info(line);
					break;
				}

				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (process != null) {
				process.destroy();
			}
		}

		return sbStd.toString();
	}
}
