package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Admin;
import com.cym.model.Server;
import com.cym.model.Upstream;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class AdminService {
	@Autowired
	SqlHelper sqlHelper;

	public boolean login(String name, String pass) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name).eq("pass", pass), Admin.class) > 0;
	}

	public Page search(Page page) {
		page = sqlHelper.findPage(page, Admin.class);

		return page;
	}

	public Long getCountByName(String name) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name), Admin.class);
	}

	public Long getCountByNameWithOutId(String name, String id) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name).ne("id", id), Admin.class);
	}

}
