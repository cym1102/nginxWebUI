package com.cym.utils;

import java.util.List;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;

public class SystemTool {

	public static String getSystem() {

		if (SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("win")) {
			return "Windows";
		} else {
			return "Linux";
		}

	}

	public static Boolean isWindows() {
		return getSystem().equals("Windows");
	}

	public static Boolean isLinux() {
		return getSystem().equals("Linux");
	}

	public static Boolean hasNginx() {
		// 寻找nginx执行文件
		if (SystemTool.isLinux()) {
			String rs = RuntimeUtil.execForStr("which nginx");
			if (StrUtil.isEmpty(rs)) {
				// 没有安装，查找是否有编译版
				return false;
			}
		}

		return true;
	}

}
