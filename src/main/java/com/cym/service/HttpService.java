package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Http;
import com.cym.model.Server;
import com.cym.model.Stream;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.CriteriaAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class HttpService {
	@Autowired
	SqlHelper sqlHelper;
	

	public boolean hasName(String name) {
		return sqlHelper.findCountByQuery(new CriteriaAndWrapper().eq("name", name), Http.class) > 0;
	}
	
	
}
