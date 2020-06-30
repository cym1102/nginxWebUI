package com.cym.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;

public class SystemTool {

	public static String getSystem() {

		if (SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("windows")) {
			return "Windows";
		} else if (SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("mac os")) {
			return "Mac OS";
		} else {
			return "Linux";
		}

	}

	public static Boolean isWindows() {
		return getSystem().equals("Windows");
	}

	public static Boolean isMacOS() {
		return getSystem().equals("Mac OS");
	}

	public static Boolean isLinux() {
		return getSystem().equals("Linux");
	}

	public static Boolean hasNginx() {
		// 寻找nginx执行文件
		if (SystemTool.isLinux() || SystemTool.isMacOS()) {
			String rs = RuntimeUtil.execForStr("which nginx");
			if (StrUtil.isEmpty(rs)) {
				return false;
			}
		}

		return true;
	}


}
