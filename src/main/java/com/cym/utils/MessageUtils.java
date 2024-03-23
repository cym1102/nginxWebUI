package com.cym.utils;

import java.util.Properties;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import com.cym.service.SettingService;

import cn.hutool.core.util.StrUtil;

/**
 * 国际化工具类
 */
@Component
public class MessageUtils {

	@Inject
	PropertiesUtils propertiesUtils;

	Properties properties = null;
	Properties propertiesEN = null;
	Properties propertiesTW = null;

	@Init
	public void afterInjection() {
		propertiesTW = propertiesUtils.getPropertis("messages_zh_TW.properties");
		propertiesEN = propertiesUtils.getPropertis("messages_en_US.properties");
		properties = propertiesUtils.getPropertis("messages.properties");
	}

	@Inject
	SettingService settingService;

	/**
	 * 获取单个国际化翻译值
	 */
	public String get(String msgKey) {
		String lang = settingService.get("lang");
		if (StrUtil.isEmpty(lang)) {
			return properties.getProperty(msgKey);
		}
		if ("en_US".equals(lang)) {
			return propertiesEN.getProperty(msgKey);
		}
		if ("zh_TW".equals(lang)) {
			return propertiesTW.getProperty(msgKey);
		}

		return "";
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getPropertiesEN() {
		return propertiesEN;
	}

	public void setPropertiesEN(Properties propertiesEN) {
		this.propertiesEN = propertiesEN;
	}

	public PropertiesUtils getPropertiesUtils() {
		return propertiesUtils;
	}

	public void setPropertiesUtils(PropertiesUtils propertiesUtils) {
		this.propertiesUtils = propertiesUtils;
	}

	public Properties getPropertiesTW() {
		return propertiesTW;
	}

	public void setPropertiesTW(Properties propertiesTW) {
		this.propertiesTW = propertiesTW;
	}

	public SettingService getSettingService() {
		return settingService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

}