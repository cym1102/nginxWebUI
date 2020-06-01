package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Stream;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class StreamService {
	@Autowired
	SqlHelper sqlHelper;

	public boolean hasName(String name) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name), Stream.class) > 0;
	}
	
	
}
