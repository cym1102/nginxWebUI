package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

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
		sqlHelper.deleteById(id, Cert.class);
		return renderSuccess();
	}

	@RequestMapping("apply")
	@ResponseBody
	public JsonResult apply(String id, String type) {
		if (!SystemTool.isLinux()) {
			return renderError("证书操作只能在linux下进行");
		}

		Cert cert = sqlHelper.findById(id, Cert.class);
		if (cert.getDnsType() == null) {
			return renderError("该证书还未设置DNS服务商信息");
		}

		if (isInApply) {
			return renderError("另一个申请进程正在进行，请稍后再申请");
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
				if(cert.getDnsType().equals("ali")) {
					dnsType = "dns_ali";
				}else if(cert.getDnsType().equals("dp")) {
					dnsType = "dns_dp";
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

		// 申请完后,马上备份.acme.sh,以便在升级docker后可还原
		FileUtil.del(InitConfig.home + ".acme.sh");
		FileUtil.copy("/root/.acme.sh", InitConfig.home, true);

		if (rs.contains("Your cert is in")) {
			try {
				// 讲证书复制到/home/nginxWebUI
				String domain = cert.getDomain().split(",")[0];

				String certDir = "/root/.acme.sh/" + domain + "/";

				String dest = InitConfig.home + "cert/" + domain + ".cer";
				FileUtil.copy(new File(certDir + domain + ".cer"), new File(dest), true);
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
		list.add("USER_PATH='/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/snap/bin'");

		FileUtil.writeLines(list, new File(InitConfig.acmeSh.replace("/acme.sh", "/account.conf")), Charset.defaultCharset());
	}



}
