package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

import com.cym.model.Cert;
import com.cym.model.CertCode;

import cn.craccd.sqlHelper.bean.Page;
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

	public List<CertCode> getCertCodes(String certId) {

		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq(CertCode::getCertId, certId), CertCode.class);
	}

	public void insertOrUpdate(Cert cert, String[] domain, String[] type, String[] value) {
		sqlHelper.insertOrUpdate(cert);

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(CertCode::getCertId, cert.getId()), CertCode.class);

		if (domain != null && type != null && value != null) {
			for (int i = 0; i < domain.length; i++) {
				CertCode certCode = new CertCode();
				certCode.setCertId(cert.getId());
				certCode.setDomain(domain[i]);
				certCode.setType(type[i]);
				certCode.setValue(value[i]);
				sqlHelper.insert(certCode);
			}
		}

	}

	public Page getPage(String keywords, Page page) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();
		if (StrUtil.isNotEmpty(keywords)) {
			conditionAndWrapper.like(Cert::getDomain, keywords);
		}

		return sqlHelper.findPage(conditionAndWrapper, page, Cert.class);
	}

	public void saveCertCode(String certId, List<CertCode> mapList) {
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(CertCode::getCertId, certId), CertCode.class);
		for (CertCode certCode : mapList) {
			certCode.setCertId(certId);
			sqlHelper.insert(certCode);
		}

	}

	public boolean hasCode(String certId) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(CertCode::getCertId, certId), CertCode.class) > 0;
	}
}
