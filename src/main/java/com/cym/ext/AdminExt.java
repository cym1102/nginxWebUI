package com.cym.ext;

import java.util.List;

import com.cym.model.Admin;

public class AdminExt {
	Admin admin;
	List<String> groupIds;

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<String> groupIds) {
		this.groupIds = groupIds;
	}

}
