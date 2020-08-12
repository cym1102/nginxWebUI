package com.cym.config;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import com.cym.service.SettingService;

@Component
public class MyLocaleResolver implements LocaleResolver {
	@Autowired
	SettingService settingService;
	
	@Override
	public Locale resolveLocale(HttpServletRequest httpServletRequest) {

		if (settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
			return new Locale("en","US");
		} 
		return Locale.getDefault();
	}

	@Override
	public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {

	}
}