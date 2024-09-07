package com.cym.utils;

import cn.hutool.core.util.StrUtil;

public class ToolUtils {

	/**
	 * 处理conf一些语法问题
	 * 
	 * @param path
	 * @return
	 */
	public static String handleConf(String path) {
		if (StrUtil.isEmpty(path)) {
			return path;
		}
		return path.replace("};", "  }");
	}

	/**
	 * 处理路径的斜杠和空格
	 * 
	 * @param path
	 * @return
	 */
	public static String handlePath(String path) {
		if (StrUtil.isEmpty(path)) {
			return path;
		}
		return path.replace("\\", "/") // 替换反斜杠
				.replace("//", "/") // 替换双斜杠
				.replaceAll("[\\s?<>|\"#&;'`]", ""); // 删除空格和特殊字符
	}

	/**
	 * 处理目录最后的斜杠
	 * 
	 * @param path
	 * @return
	 */
	public static String endDir(String path) {
		if (StrUtil.isEmpty(path)) {
			return path;
		}
		if (!path.endsWith("/")) {
			path += "/";
		}

		return path;
	}
}
