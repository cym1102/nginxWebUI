package com.cym.utils;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Component
public class AuthUtils {

	GoogleAuthenticator gAuth;

	@PostConstruct
	public void init() {
		gAuth = new GoogleAuthenticator();
	}

	public Boolean testKey(String key, String code) {
		try {
			Integer value = Integer.parseInt(code);

			// 用户登录时使用
			// 根据用户密钥和用户输入的密码，验证是否一致。（近3个密码都有效：前一个，当前，下一个）
			GoogleAuthenticator gAuth = new GoogleAuthenticator();
			return gAuth.authorize(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String makeKey() {
		// 用户注册时使用
		// 获取一个新的密钥，默认16位，该密钥与用户绑定
		final GoogleAuthenticatorKey key = gAuth.createCredentials();
		String key1 = key.getKey();
		System.out.println(key1);
		return key1;

	}

	public int getCode(String key) {
		// 根据密钥，获取最新密码（后台用不到，用来开发 谷歌身份验证器 客户端）
		GoogleAuthenticator gAuth = new GoogleAuthenticator();
		int code = gAuth.getTotpPassword(key);
		System.out.println(code);
		return code;
	}
}