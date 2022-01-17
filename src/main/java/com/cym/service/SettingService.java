package com.cym.service;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.model.Setting;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

@Service
public class SettingService {
	@Inject
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
