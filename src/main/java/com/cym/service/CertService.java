package com.cym.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.HomeConfig;
import com.cym.model.Cert;
import com.cym.model.CertCode;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Service
public class CertService {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Inject
	SqlHelper sqlHelper;
	@Inject
	HomeConfig homeConfig;

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

	public String getAcmeZipBase64() {
		File file = ZipUtil.zip(homeConfig.home + ".acme.sh", homeConfig.home + "temp" + File.separator + "cert.zip");
		String str = Base64.encode(file);
		file.delete();
		return str;
	}

	public void writeAcmeZipBase64(String acmeZip) {

		Base64.decodeToFile(acmeZip, new File(homeConfig.home + "acme.zip"));
		FileUtil.mkdir(homeConfig.acmeShDir);
		ZipUtil.unzip(homeConfig.home + "acme.zip", homeConfig.acmeShDir);
		FileUtil.del(homeConfig.home + "acme.zip");

	}

}
