package com.cym.service;

import java.util.List;
import java.util.UUID;

import org.noear.solon.annotation.Inject;
import org.noear.solon.aspect.annotation.Service;

import com.cym.model.Admin;
import com.cym.model.AdminGroup;
import com.cym.model.Credit;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.EncodePassUtils;

@Service
public class AdminService {
	@Inject
	SqlHelper sqlHelper;

	public Admin login(String name, String pass) {
		return sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Admin::getName, name).eq(Admin::getPass, EncodePassUtils.encode(pass)), Admin.class);
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
		admin.setPass(EncodePassUtils.encode(admin.getPass()));  
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
