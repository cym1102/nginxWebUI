package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Ssh;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class SshService {
	@Autowired
	SqlHelper sqlHelper;

	public Page search(Page page) {
		page = sqlHelper.findPage(page, Ssh.class);

		return page;
	}


}
