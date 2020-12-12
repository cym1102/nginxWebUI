package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.DenyAllow;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class DenyAllowService {
	@Autowired
	SqlHelper sqlHelper;

	public Page search(Page page) {
		page = sqlHelper.findPage(page, DenyAllow.class);

		return page;
	}
}
