package com.cym.controller.adminPage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.InitConfig;
import com.cym.model.Cert;
import com.cym.model.CertCode;
import com.cym.service.CertService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;
import com.cym.utils.TimeExeUtils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Controller
@Mapping("/adminPage/cert")
public class CertController extends BaseController {
	@Inject
	SettingService settingService;
	@Inject
	CertService certService;
	@Inject
	TimeExeUtils timeExeUtils;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Boolean isInApply = false;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page, String keywords) {
		page = certService.getPage(keywords, page);

		modelAndView.put("keywords", keywords);
		modelAndView.put("page", page);
		modelAndView.view("/adminPage/cert/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(Cert cert, String[] domains, String[] types, String[] values) {
		if (certService.hasSame(cert)) {
			return renderError(m.get("certStr.same"));
		}

		certService.insertOrUpdate(cert, domains, types, values);

		return renderSuccess(cert);
	}

	@Mapping("setAutoRenew")
	public JsonResult setAutoRenew(Cert cert) {
		sqlHelper.updateById(cert);
		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Cert.class));
	}

	@Mapping("del")
	public JsonResult del(String id) {
		Cert cert = sqlHelper.findById(id, Cert.class);
		String path = homeConfig.acmeShDir + cert.getDomain();
		if (FileUtil.exist(path)) {
			FileUtil.del(path);
		}
		sqlHelper.deleteById(id, Cert.class);
		return renderSuccess();
	}

	@Mapping("apply")
	public JsonResult apply(String id, String type) {
		if (!SystemTool.isLinux()) {
			return renderError(m.get("certStr.error2"));
		}

		Cert cert = sqlHelper.findById(id, Cert.class);
		if (cert.getDnsType() == null) {
			return renderError(m.get("certStr.error3"));
		}

		if (isInApply) {
			return renderError(m.get("certStr.error4"));
		}
		isInApply = true;

		String rs = "";
		String cmd = "";
		// 设置dns账号
		String[] env = getEnv(cert);

		if (type.equals("issue")) {

			// 申请
			if (cert.getType() == 0) {
				String dnsType = "";
				if (cert.getDnsType().equals("ali")) {
					dnsType = "dns_ali";
				} else if (cert.getDnsType().equals("dp")) {
					dnsType = "dns_dp";
				} else if (cert.getDnsType().equals("cf")) {
					dnsType = "dns_cf";
				} else if (cert.getDnsType().equals("gd")) {
					dnsType = "dns_gd";
				} else if (cert.getDnsType().equals("hw")) {
					dnsType = "dns_huaweicloud";
				}

				cmd = homeConfig.acmeSh + " --issue --force --dns " + dnsType + " -d " + cert.getDomain() + " --server letsencrypt";
			} else if (cert.getType() == 2) {
				if (certService.hasCode(cert.getId())) {
					cmd = homeConfig.acmeSh + " --renew --force --dns -d " + cert.getDomain() + " --server letsencrypt --yes-I-know-dns-manual-mode-enough-go-ahead-please";
				} else {
					cmd = homeConfig.acmeSh + " --issue --force --dns -d " + cert.getDomain() + " --server letsencrypt --yes-I-know-dns-manual-mode-enough-go-ahead-please";
				}

			}
		} else if (type.equals("renew")) {
			// 续签,以第一个域名为证书名
			if (cert.getType() == 0) {
				String domain = cert.getDomain().split(",")[0];
				cmd = homeConfig.acmeSh + " --renew --force -d " + domain;
			} else if (cert.getType() == 2) {
				cmd = homeConfig.acmeSh + " --renew --force -d " + cert.getDomain() + " --server letsencrypt --yes-I-know-dns-manual-mode-enough-go-ahead-please";
			}
		}
		logger.info(cmd);

		rs = timeExeUtils.execCMD(cmd, env, 2 * 60 * 1000);
		logger.info(rs);

		if (rs.contains("Your cert is in")) {
			// 申请成功, 将证书复制到/home/nginxWebUI
			String domain = cert.getDomain().split(",")[0];
			String certDir = homeConfig.acmeShDir + domain + "/";

			String dest = homeConfig.home + "cert/" + domain + ".fullchain.cer";
			FileUtil.copy(new File(certDir + "fullchain.cer"), new File(dest), true);
			cert.setPem(dest);

			dest = homeConfig.home + "cert/" + domain + ".key";
			FileUtil.copy(new File(certDir + domain + ".key"), new File(dest), true);
			cert.setKey(dest);

			cert.setMakeTime(System.currentTimeMillis());
			sqlHelper.updateById(cert);
			isInApply = false;
			return renderSuccess();
		} else if (rs.contains("TXT value")) {
			// 获取到dns配置txt, 显示出来, 并保存到数据库
			List<CertCode> mapList = new ArrayList<>();

			CertCode map1 = null;
			CertCode map2 = null;
			for (String str : rs.split("\n")) {
				logger.info(str);
				if (str.contains("Domain:")) {
					map1 = new CertCode();
					map1.setDomain(str.split("'")[1]);
					map1.setType("TXT");

					map2 = new CertCode();
					map2.setDomain(map1.getDomain().replace("_acme-challenge.", ""));
					map2.setType(m.get("certStr.any"));
				}

				if (str.contains("TXT value:")) {
					map1.setValue(str.split("'")[1]);
					mapList.add(map1);

					map2.setValue(m.get("certStr.any"));
					mapList.add(map2);
				}
			}
			certService.saveCertCode(id, mapList);
			isInApply = false;
			return renderSuccess(mapList);
		} else {
			isInApply = false;
			return renderError("<span class='blue'>" + cmd + "</span><br>" + m.get("certStr.applyFail") + "<br>" + rs.replace("\n", "<br>"));
		}
	}

	private String[] getEnv(Cert cert) {
		List<String> list = new ArrayList<>();
		if (cert.getDnsType().equals("ali")) {
			list.add("Ali_Key=" + cert.getAliKey());
			list.add("Ali_Secret=" + cert.getAliSecret());
		}
		if (cert.getDnsType().equals("dp")) {
			list.add("DP_Id=" + cert.getDpId());
			list.add("DP_Key=" + cert.getDpKey());
		}
		if (cert.getDnsType().equals("cf")) {
			list.add("CF_Email=" + cert.getCfEmail());
			list.add("CF_Key=" + cert.getCfKey());
		}
		if (cert.getDnsType().equals("gd")) {
			list.add("GD_Key=" + cert.getGdKey());
			list.add("GD_Secret=" + cert.getGdSecret());
		}
		if (cert.getDnsType().equals("hw")) {
			list.add("HUAWEICLOUD_Username=" + cert.getHwUsername());
			list.add("HUAWEICLOUD_Password=" + cert.getHwPassword());
			list.add("HUAWEICLOUD_ProjectID=" + cert.getHwProjectID());
		}

		return list.toArray(new String[] {});
	}

	@Mapping("getTxtValue")
	public JsonResult getTxtValue(String id) {

		List<CertCode> certCodes = certService.getCertCodes(id);
		return renderSuccess(certCodes);
	}

	@Mapping("download")
	public DownloadedFile download(String id) throws IOException {
		Cert cert = sqlHelper.findById(id, Cert.class);
		if (StrUtil.isNotEmpty(cert.getPem()) && StrUtil.isNotEmpty(cert.getKey())) {
			String dir = homeConfig.home + "/temp/cert";
			FileUtil.del(dir);
			FileUtil.del(dir + ".zip");
			FileUtil.mkdir(dir);

			File pem = new File(cert.getPem());
			File key = new File(cert.getKey());
			FileUtil.copy(pem, new File(dir + "/" + pem.getName()), true);
			FileUtil.copy(key, new File(dir + "/" + key.getName()), true);

			ZipUtil.zip(dir);
			FileUtil.del(dir);

			DownloadedFile downloadedFile = new DownloadedFile("application/octet-stream", new FileInputStream(dir + ".zip"), "cert.zip");
			return downloadedFile;
		}

		return null;
	}

}
