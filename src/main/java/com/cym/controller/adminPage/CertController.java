package com.cym.controller.adminPage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/cert")
public class CertController extends BaseController {
	@Inject
	SettingService settingService;
	@Inject
	CertService certService;
	@Inject
	TimeExeUtils timeExeUtils;
	@Inject
	ConfController confController;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Boolean isInApply = false;

	String acmeDnsAuth = "http://auth.nginxwebui.cn";

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page, String keywords) {
		page = certService.getPage(keywords, page);

		for (Cert cert : (List<Cert>) page.getRecords()) {
			if (cert.getType() == 0 || cert.getType() == 2) {
				cert.setDomain(cert.getDomain() + "(" + cert.getEncryption() + ")");
			}

			if (cert.getMakeTime() != null && cert.getType() != 1) {
				cert.setEndTime(cert.getMakeTime() + 90 * 24 * 60 * 60 * 1000l);
			}
		}

		modelAndView.put("keywords", keywords);
		modelAndView.put("page", page);
		modelAndView.view("/adminPage/cert/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(Cert cert, String[] domains, String[] types, String[] values) {

		// 检查是否重名
		if (certService.hasName(cert)) {
			return renderError(m.get("certStr.nameRepetition"));
		}

		Integer type = cert.getType();
		if (type == null && StrUtil.isNotEmpty(cert.getId())) {
			Cert certOrg = sqlHelper.findById(cert.getId(), Cert.class);
			type = certOrg.getType();
		}

		String domain = cert.getDomain();
		if (StrUtil.isEmpty(domain) && StrUtil.isNotEmpty(cert.getId())) {
			Cert certOrg = sqlHelper.findById(cert.getId(), Cert.class);
			domain = certOrg.getDomain();
		}

		if (type != null && type == 1) {
			// 手动上传
			String dir = homeConfig.home + "cert/" + domain + "/";

			// windows下不允许*作为文件路径
			if (SystemTool.isWindows()) {
				dir = dir.replace("*", "_");
			}

			if (cert.getKey().contains(FileUtil.getTmpDir().toString().replace("\\", "/"))) {
				String keyName = new File(cert.getKey()).getName();
				FileUtil.move(new File(cert.getKey()), new File(dir + keyName), true);
				cert.setKey(dir + keyName);
			}

			if (cert.getPem().contains(FileUtil.getTmpDir().toString().replace("\\", "/"))) {
				String pemName = new File(cert.getPem()).getName();
				FileUtil.move(new File(cert.getPem()), new File(dir + pemName), true);
				cert.setPem(dir + pemName);
			}

			// 计算到期时间
			if (cert.getEndTime() == null) {
				try {
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					FileInputStream in = new FileInputStream(cert.getPem());
					X509Certificate certFile = (X509Certificate) cf.generateCertificate(in);
					Date effDate = certFile.getNotBefore();
					Date expDate = certFile.getNotAfter();
					cert.setMakeTime(effDate.getTime());
					cert.setEndTime(expDate.getTime());
				} catch (Exception e) {
					logger.info(e.getMessage(), e);
				}
			}
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
		Cert cert = sqlHelper.findById(id, Cert.class);
		return renderSuccess(cert);
	}

	@Mapping("del")
	public JsonResult del(String id) {
		String[] ids = id.split(",");

		for (String i : ids) {
			Cert cert = sqlHelper.findById(i, Cert.class);

			if (cert.getType() == 1) {
				// 手动上传
				if (cert.getPem().contains(homeConfig.home + "cert/")) {
					FileUtil.del(cert.getPem());
				}
				if (cert.getKey().contains(homeConfig.home + "cert/")) {
					FileUtil.del(cert.getKey());
				}
			} else {
				// 申请获得
				String domain = cert.getDomain().split(",")[0];
				String path = homeConfig.home + File.separator + ".acme.sh" + File.separator + domain;

				if ("ECC".equals(cert.getEncryption())) {
					path += "_ecc";
				}
				if (FileUtil.exist(path)) {
					FileUtil.del(path);
				}
			}

			sqlHelper.deleteById(i, Cert.class);
		}
		return renderSuccess();
	}

	@Mapping("apply")
	public JsonResult apply(String id, String type) {
		if (!SystemTool.isLinux()) {
			return renderError(m.get("certStr.error2"));
		}

		if (isInApply) {
			return renderError(m.get("certStr.error4"));
		}
		isInApply = true;

		Cert cert = sqlHelper.findById(id, Cert.class);
		String keylength = " --keylength 2048 "; // RSA模式
		String ecc = "";
		if ("ECC".equals(cert.getEncryption())) { // ECC模式
			keylength = " --keylength ec-256 ";
			ecc = " --ecc";
		}

		String cmd = "sh ";
		// 设置dns账号
		String[] envs = getEnv(cert);

		String[] split = cert.getDomain().split(",");
		if (type.equals("issue")) {
			StringBuffer sb = new StringBuffer();
			Arrays.stream(split).forEach(s -> sb.append(" -d ").append(s));
			String domain = sb.toString();
			// 申请
			if (cert.getType() == 0) {
				// DNS API申请
				String dnsType = "";
				if (cert.getDnsType().equals("ali")) {
					dnsType = "dns_ali";
				} else if (cert.getDnsType().equals("dp")) {
					dnsType = "dns_dp";
				} else if (cert.getDnsType().equals("tencent")) {
					dnsType = "dns_tencent";
				} else if (cert.getDnsType().equals("cf")) {
					dnsType = "dns_cf";
				} else if (cert.getDnsType().equals("gd")) {
					dnsType = "dns_gd";
				} else if (cert.getDnsType().equals("hw")) {
					dnsType = "dns_huaweicloud";
				} else if (cert.getDnsType().equals("aws")) {
					dnsType = "dns_aws";
				} else if (cert.getDnsType().equals("ipv64")) {
					dnsType = "dns_ipv64";
				}
				cmd += homeConfig.acmeSh + " --issue --dns " + dnsType + domain + keylength + " --server letsencrypt";
			} else if (cert.getType() == 2) {
				// AcmeDNS验证
				if (StrUtil.isEmpty(settingService.get("fulldomain"))) { // 查看是否获取了DNS记录
					isInApply = false;
					return renderError(m.get("certStr.error6"));
				}

				cmd += homeConfig.acmeSh + " --issue --dns dns_acmedns" + domain + keylength + " --server letsencrypt";
			}
		} else if (type.equals("renew")) {
			// 续签,以第一个域名为证书名
			String domain = split[0];

			cmd += homeConfig.acmeSh + " --renew --force " + ecc + " -d " + domain;
		}

		String rs = timeExeUtils.execCMD(cmd, envs, 3 * 60 * 1000);

		if (rs.contains("Your cert is in")) {
			// 申请成功, 定位证书
			cert.setPem(getPem(rs));
			cert.setKey(getKey(rs));

			cert.setMakeTime(System.currentTimeMillis());
			sqlHelper.updateById(cert);

			// 续签,重载nginx使证书生效
			if (type.equals("renew")) {
				confController.reload(null, null, null);
			}

			isInApply = false;
			return renderSuccess();
		} else {
			isInApply = false;
			return renderError("<span class='blue'>" + cmd + "</span><br>" + m.get("certStr.applyFail") + "<br>" + rs.replace("\n", "<br>"));
		}
	}

	private String getKey(String rs) {
		String[] lines = rs.split("\n");
		for (String line : lines) {
			if (line.contains("Your cert key is in:")) {
				return line.split("Your cert key is in:")[1].trim().replace("\\", "/").replace("//", "/");
			}
		}

		return null;
	}

	private String getPem(String rs) {
		String[] lines = rs.split("\n");
		for (String line : lines) {
			if (line.contains("And the full chain certs is there:")) {
				return line.split("And the full chain certs is there:")[1].trim().replace("\\", "/").replace("//", "/");
			}
		}

		return null;
	}

	private String[] getEnv(Cert cert) {
		List<String> list = new ArrayList<>();
		list.add("HOME=" + homeConfig.home); // 指定acme证书存放目录

		if (cert.getType() == 0) {
			if (cert.getDnsType().equals("ali")) {
				list.add("Ali_Key=" + cert.getAliKey());
				list.add("Ali_Secret=" + cert.getAliSecret());
			}
			if (cert.getDnsType().equals("dp")) {
				list.add("DP_Id=" + cert.getDpId());
				list.add("DP_Key=" + cert.getDpKey());
			}
			if (cert.getDnsType().equals("tencent")) {
				list.add("Tencent_SecretId=" + cert.getTencentSecretId());
				list.add("Tencent_SecretKey=" + cert.getTencentSecretKey());
			}
			if (cert.getDnsType().equals("aws")) {
				list.add("AWS_ACCESS_KEY_ID=" + cert.getAwsAccessKeyId());
				list.add("AWS_SECRET_ACCESS_KEY=" + cert.getAwsSecretAccessKey());
			}
			if (cert.getDnsType().equals("ipv64")) {
				list.add("IPv64_Token=" + cert.getIpv64Token());
			}
			if (cert.getDnsType().equals("cf")) {
				list.add("CF_Email=" + cert.getCfEmail());
				if (cert.getCfToken() != null && !cert.getCfToken().isEmpty()) {
					list.add("CF_Token=" + cert.getCfToken());
				}
				if (cert.getCfKey() != null && !cert.getCfKey().isEmpty()) {
					list.add("CF_Key=" + cert.getCfKey());
				}
			}
			if (cert.getDnsType().equals("gd")) {
				list.add("GD_Key=" + cert.getGdKey());
				list.add("GD_Secret=" + cert.getGdSecret());
			}
			if (cert.getDnsType().equals("hw")) {
				list.add("HUAWEICLOUD_Username=" + cert.getHwUsername());
				list.add("HUAWEICLOUD_Password=" + cert.getHwPassword());
				list.add("HUAWEICLOUD_DomainName=" + cert.getHwDomainName());
			}
		} else if (cert.getType() == 2) {
			list.add("ACMEDNS_BASE_URL=" + acmeDnsAuth);
			list.add("ACMEDNS_USERNAME=" + settingService.get("username"));
			list.add("ACMEDNS_PASSWORD=" + settingService.get("password"));
			list.add("ACMEDNS_SUBDOMAIN=" + settingService.get("subdomain"));
		}

		return list.toArray(new String[] {});
	}

	@Mapping("getTxtValue")
	public JsonResult getTxtValue(String id) {
		Cert cert = sqlHelper.findById(id, Cert.class);

		if (StrUtil.isEmpty(settingService.get("fulldomain"))) {
			// 从acme-dns服务器获取TXT
			try {

				Map<String, Object> paramMap = new HashMap<>();
				String rs = HttpUtil.post(acmeDnsAuth + "/register", paramMap);
				logger.info(rs);

				JSONObject jsonObject = JSONUtil.parseObj(rs);
				settingService.set("username", jsonObject.getStr("username"));
				settingService.set("password", jsonObject.getStr("password"));
				settingService.set("fulldomain", jsonObject.getStr("fulldomain"));
				settingService.set("subdomain", jsonObject.getStr("subdomain"));

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return renderError(m.get("certStr.error7"));
			}
		}

		List<CertCode> certCodes = new ArrayList<CertCode>();

		CertCode certCode = new CertCode();
		certCode.setDomain(buildDomain(cert.getDomain()));
		certCode.setType("CNAME");
		certCode.setValue(settingService.get("fulldomain"));
		certCodes.add(certCode);

		return renderSuccess(certCodes);

	}

	private static String buildDomain(String domain) {
		domain = domain.replace("*", "");
		if (domain.startsWith(".")) {
			domain = domain.substring(1);
		}

		return "_acme-challenge." + domain;
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

			DownloadedFile downloadedFile = new DownloadedFile("application/octet-stream", Files.newInputStream(Paths.get(dir + ".zip")), "cert.zip");
			return downloadedFile;
		}

		return null;
	}
}
