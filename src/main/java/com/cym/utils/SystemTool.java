package com.cym.utils;

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



}
