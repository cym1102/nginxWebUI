package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.CertConfig;
import com.cym.model.Cert;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.system.SystemUtil;

@Controller
@RequestMapping("/adminPage/cert")
public class CertController extends BaseController {
	@Autowired
	CertConfig certConfig;
	@Autowired
	SettingService settingService;

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
		sqlHelper.deleteById(id, Cert.class);

		return renderSuccess();
	}

	@RequestMapping("apply")
	@ResponseBody
	public JsonResult apply(String id) {
		if (SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("win")) {
			return renderError("证书操作只能在linux下进行");
		}

		String nginxPath = settingService.get("nginxPath");
		if (!FileUtil.exist(nginxPath)) {
			return renderError("未找到nginx配置文件:" + nginxPath + ", 请先在【生成conf】模块中设置并读取.");
		}

		Cert cert = sqlHelper.findById(id, Cert.class);
		if (cert.getMakeTime() != null) {
			return renderError("该证书已申请");
		}

		// 替换nginx.conf并重启
		replaceStartNginx(nginxPath, cert.getDomain());

		// 申请
		String cmd = certConfig.acmeSh + " --issue --nginx -d " + cert.getDomain();
		System.out.println(cmd);
		String rs = RuntimeUtil.execForStr(cmd);
		System.out.println(rs);

		// 还原nginx.conf并重启
		backupStartNginx(nginxPath);

		if (rs.contains("Cert success")) {
			String certDir = FileUtil.getUserHomePath() + File.separator + ".acme.sh" + File.separator + cert.getDomain() + File.separator;
			cert.setPem(certDir + cert.getDomain() + ".cer");
			cert.setKey(certDir + cert.getDomain() + ".key");

			cert.setMakeTime(System.currentTimeMillis());
			sqlHelper.updateById(cert);

			return renderSuccess();
		} else {
			return renderError(rs.replace("\n", "<br>"));
		}

	}

	@RequestMapping("renew")
	@ResponseBody
	public JsonResult renew(String id) {
		if (SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("win")) {
			return renderError("证书操作只能在linux下进行");
		}

		String nginxPath = settingService.get("nginxPath");
		if (!FileUtil.exist(nginxPath)) {
			return renderError("未找到nginx配置文件:" + nginxPath + ", 请先在【生成conf】模块中设置并读取.");
		}

		Cert cert = sqlHelper.findById(id, Cert.class);
		if (cert.getMakeTime() == null) {
			return renderError("该证书还未申请");
		}

		// 替换nginx.conf并重启
		replaceStartNginx(nginxPath, cert.getDomain());

		// 续签
		String cmd = certConfig.acmeSh + " --renew --force -d " + cert.getDomain();
		System.out.println(cmd);
		String rs = RuntimeUtil.execForStr(cmd);
		System.out.println(rs);

		// 还原nginx.conf并重启
		backupStartNginx(nginxPath);
				
		if (rs.contains("Cert success")) {
			cert.setMakeTime(System.currentTimeMillis());
			sqlHelper.updateById(cert);
		} else {
			return renderError(rs.replace("\n", "<br>"));
		}
		
		return renderSuccess();
	}

	// 替换nginx.conf并重启
	private void replaceStartNginx(String nginxPath, String domain) {
		String nginxContent = "worker_processes  1; \n" //
				+ "events {worker_connections  1024;} \n" //
				+ "http { \n" //
				+ "   server { \n" //
				+ "	  server_name " + domain + "; \n" //
				+ "	  listen 80; \n" //
				+ "	  root /tmp/www/; \n" //
				+ "   } \n" //
				+ "}" //
		;

		// 替换备份文件
		FileUtil.copy(nginxPath, nginxPath + ".org", true);
		FileUtil.writeString(nginxContent, nginxPath, Charset.defaultCharset());

		// 重启nginx
		RuntimeUtil.execForStr("nginx -s reload");
	}

	// 还原nginx.conf并重启
	private void backupStartNginx(String nginxPath) {

		// 还原备份文件
		FileUtil.copy(nginxPath + ".org", nginxPath, true);
		FileUtil.del(nginxPath + ".org");

		// 重启nginx
		RuntimeUtil.execForStr("nginx -s reload");

	}

}
