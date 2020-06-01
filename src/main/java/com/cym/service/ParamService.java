package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Param;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.json.JSONUtil;

@Service
public class ParamService {

	@Autowired
	SqlHelper sqlHelper;

	public String getJsonByTypeId(String id, String type) {

		List<Param> list = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(type + "Id", id), Param.class);

		return JSONUtil.toJsonStr(list);
	}

	
	public List<Param> getListByTypeId(String id, String type) {

		List<Param> list = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(type + "Id", id), Param.class);

		return list;
	}
}
