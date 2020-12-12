package com.cym.controller.adminPage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.InitConfig;
import com.cym.model.Cert;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Controller
@RequestMapping("/adminPage/cert")
public class CertController extends BaseController {
	@Autowired
	SettingService settingService;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Boolean isInApply = false;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {
		List<Cert> certs = sqlHelper.findAll(Cert.class);

		modelAndView.addObject("certs", certs);
		modelAndView.setViewName("/adminPage/cert/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Cert cert) {
		sqlHelper.insertOrUpdate(cert);
		return renderSuccess();
	}

	@RequestMapping("setAutoRenew")
	@ResponseBody
	public JsonResult setAutoRenew(Cert cert) {
		sqlHelper.updateById(cert);
		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Cert.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		Cert cert = sqlHelper.findById(id, Cert.class);
		if (cert.getKey() != null) {
			FileUtil.del(cert.getKey());
		}
		if (cert.getPem() != null) {
			FileUtil.del(cert.getPem());
		}
		
		FileUtil.del(InitConfig.acmeShDir + cert.getDomain());
		sqlHelper.deleteById(id, Cert.class);
		return renderSuccess();
	}

	@RequestMapping("apply")
	@ResponseBody
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
		try {
			// 设置dns账号
			setEnv(cert);

			String cmd = "";
			if (type.equals("issue") || StrUtil.isEmpty(cert.getPem())) {
				// 申请
				String dnsType = "";
				if (cert.getDnsType().equals("ali")) {
					dnsType = "dns_ali";
				} else if (cert.getDnsType().equals("dp")) {
					dnsType = "dns_dp";
				} else if (cert.getDnsType().equals("cf")) {
					dnsType = "dns_cf";
				} else if (cert.getDnsType().equals("gd")) {
					dnsType = "dns_gd";
				}

				cmd = InitConfig.acmeSh + " --issue --dns " + dnsType + " -d " + cert.getDomain();
			} else if (type.equals("renew")) {
				// 续签,以第一个域名为证书名
				String domain = cert.getDomain().split(",")[0];
				cmd = InitConfig.acmeSh + " --renew --force -d " + domain;
			}
			logger.info(cmd);

			rs = RuntimeUtil.execForStr(cmd);
			logger.info(rs);

		} catch (Exception e) {
			e.printStackTrace();
			rs = e.getMessage();
		}

		if (rs.contains("Your cert is in")) {
			try {
				// 将证书复制到/home/nginxWebUI
				String domain = cert.getDomain().split(",")[0];
				String certDir = InitConfig.acmeShDir + domain + "/";

				String dest = InitConfig.home + "cert/" + domain + ".fullchain.cer";
				FileUtil.copy(new File(certDir + "fullchain.cer"), new File(dest), true);
				cert.setPem(dest);

				dest = InitConfig.home + "cert/" + domain + ".key";
				FileUtil.copy(new File(certDir + domain + ".key"), new File(dest), true);
				cert.setKey(dest);

				cert.setMakeTime(System.currentTimeMillis());
				sqlHelper.updateById(cert);
			} catch (Exception e) {
				e.printStackTrace();
			}
			isInApply = false;
			return renderSuccess();
		} else {

			isInApply = false;
			return renderError(rs.replace("\n", "<br>"));
		}
	}

	private void setEnv(Cert cert) {
		List<String> list = new ArrayList<>();
		list.add("UPGRADE_HASH='" + UUID.randomUUID().toString().replace("-", "") + "'");
		if (cert.getDnsType().equals("ali")) {
			list.add("SAVED_Ali_Key='" + cert.getAliKey() + "'");
			list.add("SAVED_Ali_Secret='" + cert.getAliSecret() + "'");
		}
		if (cert.getDnsType().equals("dp")) {
			list.add("SAVED_DP_Id='" + cert.getDpId() + "'");
			list.add("SAVED_DP_Key='" + cert.getDpKey() + "'");
		}
		if (cert.getDnsType().equals("cf")) {
			list.add("SAVED_CF_Email='" + cert.getCfEmail() + "'");
			list.add("SAVED_CF_Key='" + cert.getCfKey() + "'");
		}
		if (cert.getDnsType().equals("gd")) {
			list.add("SAVED_GD_Key='" + cert.getGdKey() + "'");
			list.add("SAVED_GD_Secret='" + cert.getGdSecret() + "'");
		}
		
		list.add("USER_PATH='/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/snap/bin'");

		FileUtil.writeLines(list, new File(InitConfig.acmeSh.replace("/acme.sh", "/account.conf")), Charset.defaultCharset());
	}

	@RequestMapping("download")
	public void download(String id, HttpServletResponse response) throws IOException {
		Cert cert = sqlHelper.findById(id, Cert.class);
		if (StrUtil.isNotEmpty(cert.getPem()) && StrUtil.isNotEmpty(cert.getKey())) {
			String dir = InitConfig.home + "/temp/cert";
			FileUtil.del(dir);
			FileUtil.del(dir + ".zip");
			FileUtil.mkdir(dir);

			File pem = new File(cert.getPem());
			File key = new File(cert.getKey());
			FileUtil.copy(pem, new File(dir + File.separator + pem.getName()), true);
			FileUtil.copy(key, new File(dir + File.separator + key.getName()), true);

			ZipUtil.zip(dir);
			FileUtil.del(dir);
			
			handleStream(response, dir + ".zip");
		}
	}
	
	

	private void handleStream(HttpServletResponse response, String path) throws IOException {

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
		response.setHeader("Content-Disposition", "attachment;filename=cert.zip");
		byte[] buffer = new byte[1024];
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(path);
			bis = new BufferedInputStream(fis);
			OutputStream os = response.getOutputStream();
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer, 0, i);
				i = bis.read(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
