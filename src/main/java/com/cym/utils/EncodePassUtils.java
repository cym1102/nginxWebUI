package com.cym.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;

public class EncodePassUtils {

	public static String salt = "nginxWebUI";

	public static String encode(String pass) {

		if (StrUtil.isNotEmpty(pass)) {
			pass = SecureUtil.md5(pass) + SecureUtil.md5(salt);
		}

		return pass;
	}

//	public static String encodeDefaultPass() {
//		return SecureUtil.md5(defaultPass) + SecureUtil.md5(defaultPass);
//	}

}
