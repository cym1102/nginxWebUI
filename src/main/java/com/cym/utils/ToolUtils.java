package com.cym.utils;

public class ToolUtils {

	/**
	 * 处理conf一些语法问题
	 * 
	 * @param path
	 * @return
	 */
	public static String handleConf(String path) {
		return path.replace("};", "  }");
	}
	
	/**
	 * 处理路径的斜杠
	 * @param path
	 * @return
	 */
	public static String handlePath(String path) {
		return path.replace("\\", "/");
	}
}
