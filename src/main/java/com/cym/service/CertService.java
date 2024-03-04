package com.cym.service;

import java.io.File;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
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

@Component
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
		try {
			File file = ZipUtil.zip(homeConfig.home + ".acme.sh", homeConfig.home + "temp" + File.separator + "acme.zip");
			String str = Base64.encode(file);
			file.delete();
			return str;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String getCertZipBase64() {
		try {
			File file = ZipUtil.zip(homeConfig.home + "cert", homeConfig.home + "temp" + File.separator + "cert.zip");
			String str = Base64.encode(file);
			file.delete();
			return str;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public void writeAcmeZipBase64(String acmeZip) {
		if (StrUtil.isNotEmpty(acmeZip)) {
			Base64.decodeToFile(acmeZip, new File(homeConfig.home + "temp" + File.separator + "acme.zip"));
			FileUtil.del(homeConfig.home + ".acme.sh/");
			FileUtil.mkdir(homeConfig.home + ".acme.sh/");
			ZipUtil.unzip(homeConfig.home + "temp" + File.separator + "acme.zip", homeConfig.home + ".acme.sh/");
			FileUtil.del(homeConfig.home + "temp" + File.separator + "acme.zip");
		}
	}
	

	public void writeCertZipBase64(String certZip) {
		if (StrUtil.isNotEmpty(certZip)) {
			Base64.decodeToFile(certZip, new File(homeConfig.home + "temp" + File.separator + "cert.zip"));
			FileUtil.del(homeConfig.home + "cert/");
			FileUtil.mkdir(homeConfig.home + "cert/");
			ZipUtil.unzip(homeConfig.home + "temp" + File.separator + "cert.zip", homeConfig.home + "cert/");
			FileUtil.del(homeConfig.home + "temp" + File.separator + "cert.zip");
		}
	}

	public boolean hasName(Cert cert) {
		if (StrUtil.isEmpty(cert.getId())) {
			Long count = sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(Cert::getDomain, cert.getDomain()), Cert.class);
			if (count > 0) {
				return true;
			}
		} else {
			Long count = sqlHelper.findCountByQuery(new ConditionAndWrapper().eq(Cert::getDomain, cert.getDomain()).ne(Cert::getId, cert.getId()), Cert.class);
			if (count > 0) {
				return true;
			}
		}

		return false;
	}


	

}
