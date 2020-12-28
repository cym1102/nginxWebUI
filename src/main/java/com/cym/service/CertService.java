package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

import com.cym.model.Cert;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class CertService {
	@Autowired
	SqlHelper sqlHelper;

	public boolean hasSame(Cert cert) {
		if (StrUtil.isEmpty(cert.getId())) {
			// 添加
			if (sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("domain", cert.getDomain()), Cert.class) > 0) {
				return true;
			}
		} else {
			// 编辑
			if (sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("domain", cert.getDomain()).ne("id", cert.getId()), Cert.class) > 0) {
				return true;
			}
		}

		return false;
	}
}
