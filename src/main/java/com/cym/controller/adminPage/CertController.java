package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.Charset;
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
import cn.hutool.core.util.RuntimeUtil;

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
			File file = new File(cert.getKey());

			FileUtil.del(file.getParent());
		}

		sqlHelper.deleteById(id, Cert.class);
		return renderSuccess();
	}

	@RequestMapping("apply")
	@ResponseBody
	public JsonResult apply(String id) {
		if (SystemTool.getSystem().equals("Windows")) {
			return renderError("证书操作只能在linux下进行");
		}
		if (!SystemTool.hasNginx()) {
			return renderError("系统中未安装nginx命令，如果是编译安装nginx，请尝试在系统中执行ln -s [nginx执行文件路径] /usr/bin建立命令链接");
		}

		String nginxPath = settingService.get("nginxPath");
		if (!FileUtil.exist(nginxPath)) {
			return renderError("未找到nginx配置文件:" + nginxPath + ", 请先在【生成conf】模块中设置并读取.");
		}

		Cert cert = sqlHelper.findById(id, Cert.class);
		if (cert.getMakeTime() != null) {
			return renderError("该证书已申请");
		}

		if(isInApply) {
			return renderError("另一个申请进程正在进行，请稍后再申请");
		}
		isInApply = true;
		
		// 替换nginx.conf并重启
		replaceStartNginx(nginxPath, cert.getDomain());
		String rs = "";
		try {
			// 申请
			String cmd = InitConfig.acmeSh + " --issue --nginx -d " + cert.getDomain();
			logger.info(cmd);
			rs = RuntimeUtil.execForStr(cmd);
			logger.info(rs);
			
		} catch (Exception e) {
			e.printStackTrace();
			rs = e.getMessage();
		}

		backupStartNginx(nginxPath);
		if (rs.contains("Cert success")) {
			String certDir = "/root/.acme.sh/" + cert.getDomain() + "/";

			String dest = InitConfig.home + "cert/" + cert.getDomain() + ".cer";
			FileUtil.copy(new File(certDir + cert.getDomain() + ".cer"), new File(dest), true);
			cert.setPem(dest);

			dest = InitConfig.home + "cert/" + cert.getDomain() + ".key";
			FileUtil.copy(new File(certDir + cert.getDomain() + ".key"), new File(dest), true);
			cert.setKey(dest);

			cert.setMakeTime(System.currentTimeMillis());
			sqlHelper.updateById(cert);

			isInApply = false;
			return renderSuccess();
		} else {
			
			isInApply = false;
			return renderError(rs.replace("\n", "<br>"));
		}

	}

	@RequestMapping("renew")
	@ResponseBody
	public JsonResult renew(String id) {
		if (SystemTool.isWindows()) {
			return renderError("证书操作只能在linux下进行");
		}
		if (!SystemTool.hasNginx()) {
			return renderError("系统中未安装nginx命令，如果是编译安装nginx，请尝试在系统中执行ln -s [nginx执行文件路径] /usr/bin建立命令链接");
		}

		String nginxPath = settingService.get("nginxPath");
		if (!FileUtil.exist(nginxPath)) {
			return renderError("未找到nginx配置文件:" + nginxPath + ", 请先在【生成conf】模块中设置并读取.");
		}

		Cert cert = sqlHelper.findById(id, Cert.class);
		if (cert.getMakeTime() == null) {
			return renderError("该证书还未申请");
		}

		if(isInApply) {
			return renderError("另一个申请进程正在进行，请稍后再申请");
		}
		isInApply = true;
		
		// 替换nginx.conf并重启
		replaceStartNginx(nginxPath, cert.getDomain());
		String rs = "";
		try {
			// 续签
			String cmd = InitConfig.acmeSh + " --renew --force -d " + cert.getDomain();
			logger.info(cmd);
			rs = RuntimeUtil.execForStr(cmd);
			logger.info(rs);
		} catch (Exception e) {
			e.printStackTrace();
			rs = e.getMessage();
		}

		backupStartNginx(nginxPath);
		if (rs.contains("Cert success")) {
			String certDir = "/root/.acme.sh/" + cert.getDomain() + "/";

			String dest = InitConfig.home + "cert/" + cert.getDomain() + ".cer";
			FileUtil.copy(new File(certDir + cert.getDomain() + ".cer"), new File(dest), true);
			cert.setPem(dest);

			dest = InitConfig.home + "cert/" + cert.getDomain() + ".key";
			FileUtil.copy(new File(certDir + cert.getDomain() + ".key"), new File(dest), true);
			cert.setKey(dest);

			cert.setMakeTime(System.currentTimeMillis());
			sqlHelper.updateById(cert);

			isInApply = false;
			return renderSuccess();
		} else {
			
			isInApply = false;
			return renderError(rs.replace("\n", "<br>"));
		}

	}

	// 替换nginx.conf并重启
	private void replaceStartNginx(String nginxPath, String domain) {
		logger.info("替换nginx.conf并重启");
		String nginxContent = "worker_processes  auto; \n" //
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
		FileUtil.writeString(nginxContent, nginxPath, Charset.forName("UTF-8"));

		// 重启nginx
		RuntimeUtil.exec("nginx -s reload");
	}

	// 还原nginx.conf并重启
	private void backupStartNginx(String nginxPath) {
		logger.info("还原nginx.conf并重启");
		// 还原备份文件
		FileUtil.copy(nginxPath + ".org", nginxPath, true);
		FileUtil.del(nginxPath + ".org");

		// 重启nginx
		RuntimeUtil.exec("nginx -s reload");

	}

}
