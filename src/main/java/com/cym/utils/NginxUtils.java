package com.cym.utils;

import cn.hutool.core.util.RuntimeUtil;

public class NginxUtils {

	/**
	 * 判断nginx是否运行
	 * 
	 * @return
	 */
	public static boolean isRun() {
		boolean isRun = false;
		if (SystemTool.isWindows()) {
			String[] command = { "tasklist" };
			String rs = RuntimeUtil.execForStr(command);
			isRun = rs.toLowerCase().contains("nginx.exe");
		} else {
			String[] command = { "/bin/sh", "-c", "ps -ef|grep nginx" };
			String rs = RuntimeUtil.execForStr(command);
			isRun = rs.contains("nginx: master process") || rs.contains("nginx: worker process");
		}

		return isRun;
	}
}
