package com.cym.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Admin;
import com.cym.model.Server;
import com.cym.model.Upstream;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.reflection.ReflectionUtil;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class AdminService {
	@Autowired
	SqlHelper sqlHelper;

	public Admin login(String name, String pass) {
		return sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Admin::getName, name).eq(Admin::getPass, pass), Admin.class);
	}

	public Page search(Page page) {
		page = sqlHelper.findPage(page, Admin.class);

		return page;
	}

	public Long getCountByName(String name) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(Admin::getName, name), Admin.class);
	}

	public Long getCountByNameWithOutId(String name, String id) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(Admin::getName, name).ne(Admin::getId, id), Admin.class);
	}

	public Admin getOneByName(String name) {
		return sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Admin::getName, name), Admin.class);
	}

	public String makeToken(String id) {
		String token = UUID.randomUUID().toString();
		Admin admin = new Admin();
		admin.setId(id);
		admin.setToken(token);
		sqlHelper.updateById(admin);

		return token;
	}

	public Admin getByToken(String token) {
		return sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("token", token), Admin.class);
	}

}
