package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Setting;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class SettingService {
	@Autowired
	SqlHelper sqlHelper;

	public void set(String key, String value) {
		Setting setting = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("key", key), Setting.class);
		if (setting == null) {
			setting = new Setting();
		}

		setting.setKey(key);
		setting.setValue(value);

		sqlHelper.insertOrUpdate(setting);
	}

	public String get(String key) {
		Setting setting = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("key", key), Setting.class);

		if (setting == null) {
			return null;
		} else {
			return setting.getValue();
		}
	}

	public void remove(String key) {
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("key", key), Setting.class);

	}
}
