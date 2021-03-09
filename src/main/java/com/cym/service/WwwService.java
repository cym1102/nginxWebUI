package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Www;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class WwwService {

	@Autowired
	SqlHelper sqlHelper;

	public Boolean hasDir(String dir, String id) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq("dir", dir);
		if(StrUtil.isNotEmpty(id)) {
			conditionAndWrapper.ne("id", id);
		}
		return sqlHelper.findCountByQuery(conditionAndWrapper, Www.class) > 0;

	}
}
