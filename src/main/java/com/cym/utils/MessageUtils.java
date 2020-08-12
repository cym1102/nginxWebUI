package com.cym.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 国际化工具类
 */
@Component
public class MessageUtils {

	private final MessageSource messageSource;

	public MessageUtils(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * 获取单个国际化翻译值
	 */
	public String get(String msgKey) {
		try {
			return messageSource.getMessage(msgKey, null, LocaleContextHolder.getLocale());
		} catch (Exception e) {
			return msgKey;
		}
	}
}