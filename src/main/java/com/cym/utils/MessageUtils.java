package com.cym.utils;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cym.service.SettingService;

/**
 * 国际化工具类
 */
@Component
public class MessageUtils {

	@Autowired
	PropertiesUtils propertiesUtils;
	
	Properties properties = null;
	Properties propertiesEN = null;
	
	@PostConstruct
	private void ini() {
		propertiesEN = propertiesUtils.getPropertis("messages_en_US.properties");
		properties = propertiesUtils.getPropertis("messages.properties");
	}
	
	
	@Autowired
	SettingService settingService;
	
	/**
	 * 获取单个国际化翻译值
	 */
	public String get(String msgKey) {
		if (settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
			return propertiesEN.getProperty(msgKey);
		} else {
			return properties.getProperty(msgKey);
		}
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
	
	
}