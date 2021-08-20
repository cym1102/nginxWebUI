package com.cym.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Message;
import com.cym.service.SettingService;
import com.cym.utils.MessageUtils;
import com.cym.utils.PropertiesUtils;

import cn.hutool.core.util.StrUtil;

@Component
public class FrontInterceptor implements HandlerInterceptor {

	@Value("${spring.application.name}")
	String projectName;

	@Autowired
	VersionConfig versionConfig;

	@Value("${project.version}")
	String currentVersion;

	@Autowired
	PropertiesUtils propertiesUtils;

	@Autowired
	SettingService settingService;
	@Autowired
	MessageUtils m;
	/*
	 * 视图渲染之后的操作
	 */
	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {

	}

	/*
	 * 处理请求完成后视图渲染之前的处理操作
	 */
	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {

	}

	/*
	 * 进入controller层之前拦截请求
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) throws Exception {
		String httpHost = request.getHeader("X-Forwarded-Host");
		String realPort = request.getHeader("X-Forwarded-Port");
		String host = request.getHeader("Host");

		String ctx = AdminInterceptor.getCtx(httpHost, host, realPort);
		
		request.setAttribute("ctx", ctx);

		request.setAttribute("jsrandom", currentVersion);
		request.setAttribute("currentVersion", currentVersion);
		request.setAttribute("projectName", projectName);

		// 显示版本更新
		if (versionConfig.getVersion() != null) {
			request.setAttribute("newVersion", versionConfig.getVersion());

			if (Integer.parseInt(currentVersion.replace(".", "").replace("v", "")) < Integer.parseInt(versionConfig.getVersion().getVersion().replace(".", "").replace("v", ""))) {
				request.setAttribute("hasNewVersion", 1);
			}
		}

		// 读取配置文件
		Properties properties = null;
		String l = request.getParameter("l");
		if (StrUtil.isNotEmpty(l) && l.equals("en_US") || settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
			settingService.set("lang", "en_US");
			properties = m.getPropertiesEN();
		} else {
			settingService.set("lang", "");
			properties = m.getProperties();
		}

		// js国际化
		Set<String> messageHeaders = new HashSet<>();
		List<Message> messages = new ArrayList<>();
		for (String key : properties.stringPropertyNames()) {
			Message message = new Message();
			message.setKey(key);
			message.setValue(properties.getProperty(key));
			messages.add(message);

			messageHeaders.add(key.split("\\.")[0]);
		}

		request.setAttribute("messageHeaders", messageHeaders);
		request.setAttribute("messages", messages);

		// html国际化
		for (String key : messageHeaders) {
			Map<String, String> map = new HashMap<>();
			for (Message message : messages) {
				if (message.getKey().split("\\.")[0].equals(key)) {
					map.put(message.getKey().split("\\.")[1], message.getValue());
				}
			}

			request.setAttribute(key, map);
		}
		
		if (settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
			request.setAttribute("langType", "切换到中文");
		} else {
			request.setAttribute("langType", "Switch to English");
		}

		return true;
	}


	
}