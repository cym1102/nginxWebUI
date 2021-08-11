package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Credit;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

@Service
public class CreditService {
	@Autowired
	SqlHelper sqlHelper;

	
	public String make(String adminId) {
		Credit credit = new Credit();
		credit.setKey(UUID.randomUUID().toString());
		credit.setAdminId(adminId); 
		sqlHelper.insertOrUpdate(credit);
		
		return credit.getKey();
	}

	public boolean check(String key) {
		if(StrUtil.isEmpty(key)) {
			return false;
		}
		
		Credit credit = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("key", key), Credit.class);

		if (credit == null) {
			return false;
		} else {
			return true;
		}
	}
}
