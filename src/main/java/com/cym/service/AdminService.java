package com.cym.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Admin;
import com.cym.model.AdminGroup;
import com.cym.model.Credit;
import com.cym.model.Group;
import com.cym.model.Server;
import com.cym.model.Upstream;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.reflection.ReflectionUtil;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

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
		return sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Admin::getToken, token), Admin.class);
	}

	public Admin getByCreditKey(String creditKey) {

		Credit credit = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Credit::getKey, creditKey), Credit.class);
		if (credit != null) {
			Admin admin = sqlHelper.findById(credit.getAdminId(), Admin.class);
			return admin;
		}
		return null;
	}

	public List<String> getGroupIds(String adminId) {

		return sqlHelper.findPropertiesByQuery(new ConditionAndWrapper().eq(AdminGroup::getAdminId, adminId), AdminGroup.class, AdminGroup::getGroupId);
	}

	public void addOver(Admin admin, String[] groupIds) {
		sqlHelper.insertOrUpdate(admin);

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(AdminGroup::getAdminId, admin.getId()), AdminGroup.class);
		if (admin.getType() == 1) {
			for (String id : groupIds) {
				AdminGroup adminGroup = new AdminGroup();
				adminGroup.setAdminId(admin.getId());
				adminGroup.setGroupId(id);
				sqlHelper.insert(adminGroup);
			}
		}
	}

}
