package com.cym.service;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.model.Credit;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

@Service
public class CreditService {
	@Inject
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
