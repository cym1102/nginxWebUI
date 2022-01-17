package com.cym.controller.adminPage;

import java.util.HashMap;
import java.util.Map;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.config.VersionConfig;
import com.cym.model.Admin;
import com.cym.model.Remote;
import com.cym.service.AdminService;
import com.cym.service.CreditService;
import com.cym.service.SettingService;
import com.cym.utils.AuthUtils;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.PwdCheckUtil;
import com.cym.utils.SystemTool;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;

/**
 * 登录页
 * 
 * @author Administrator
 *
 */
@Mapping("/adminPage/login")
@Controller
public class LoginController extends BaseController {
	@Inject
	AdminService adminService;
	@Inject
	CreditService creditService;
	@Inject
	VersionConfig versionConfig;
	@Inject
	AuthUtils authUtils;

	@Inject
	SettingService settingService;

	@Mapping("")
	public ModelAndView admin(ModelAndView modelAndView,  String adminId) {
		modelAndView.put("adminCount", sqlHelper.findAllCount(Admin.class));
		modelAndView.view("/adminPage/login/index.html");
		return modelAndView;
	}

	@Mapping("loginOut")
	public ModelAndView loginOut(ModelAndView modelAndView) {
		
		Context.current().sessionRemove(("isLogin"));;
		modelAndView.view("/adminPage/index.html");
		return modelAndView;
	}

	@Mapping("noServer")
	public ModelAndView noServer(ModelAndView modelAndView) {
		modelAndView.view("/adminPage/login/noServer.html");
		return modelAndView;
	}

	@Mapping("login")
	public JsonResult submitLogin(String name, String pass, String code, String authCode, String remember) {
		// 解码
		if (StrUtil.isNotEmpty(name)) {
			name = Base64.decodeStr(Base64.decodeStr(name));
		}
		if (StrUtil.isNotEmpty(pass)) {
			pass = Base64.decodeStr(Base64.decodeStr(pass));
		}
		if (StrUtil.isNotEmpty(code)) {
			code = Base64.decodeStr(Base64.decodeStr(code));
		}
		if (StrUtil.isNotEmpty(authCode)) {
			authCode = Base64.decodeStr(Base64.decodeStr(authCode));
		}
			
		
		// 验证码
		String captcha = (String) Context.current().session("captcha");
		if (!code.equals(captcha)) {
			Context.current().sessionRemove("captcha"); // 销毁验证码
			return renderError(m.get("loginStr.backError1")); // 验证码不正确
		}
		Context.current().sessionRemove("captcha"); // 销毁验证码

		// 用户名密码
		Admin admin = adminService.login(name, pass);
		if (admin == null) {
			return renderError(m.get("loginStr.backError2")); // 用户名密码错误
		}

		// 两步验证
		if (admin.getAuth() && !authUtils.testKey(admin.getKey(), authCode)) {
			return renderError(m.get("loginStr.backError6")); // 身份码不正确
		}

		// 登录成功
		Context.current().sessionSet("localType", "local");
		Context.current().sessionSet("isLogin", true);
		Context.current().sessionSet("admin", admin);
		Context.current().sessionRemove("imgCode"); // 立刻销毁验证码

		// 检查更新
		versionConfig.getNewVersion();

		return renderSuccess(admin);
	}

	@Mapping("autoLogin")
	public JsonResult autoLogin(String adminId) {

		// 用户名密码
		Admin admin = sqlHelper.findById(adminId, Admin.class);
		if (admin != null) {
			// 登录成功
			Context.current().sessionSet("localType", "local");
			Context.current().sessionSet("isLogin", true);
			Context.current().sessionSet("admin", admin);
			Context.current().sessionRemove("imgCode"); // 立刻销毁验证码

			// 检查更新
			versionConfig.getNewVersion();

			return renderSuccess(admin);
		} else {
			return renderError();
		}

	}

	
	@Mapping("getAuth")
	public JsonResult getAuth(String name, String pass, String code, Integer remote) {

		// 解码
		if (StrUtil.isNotEmpty(name)) {
			name = Base64.decodeStr(Base64.decodeStr(name));
		}
		if (StrUtil.isNotEmpty(pass)) {
			pass = Base64.decodeStr(Base64.decodeStr(pass));
		}
		if (StrUtil.isNotEmpty(code)) {
			code = Base64.decodeStr(Base64.decodeStr(code));
		}

		// 验证码
		if (remote == null) {
			String captcha = (String) Context.current().session("captcha");
			if (!code.equals(captcha)) {
				Context.current().sessionRemove("captcha"); // 销毁验证码
				return renderError(m.get("loginStr.backError1")); // 验证码不正确
			}
		}

		Admin admin = adminService.login(name, pass);
		if (admin == null) {
			return renderError(m.get("loginStr.backError2")); // 用户名密码错误
		}

		Admin ad = new Admin();
		ad.setAuth(admin.getAuth());
		ad.setKey(admin.getKey());

		return renderSuccess(ad);
	}

	
	@Mapping("getCredit")
	public JsonResult getCredit(String name, String pass, String code, String auth) {
		// 解码
		if (StrUtil.isNotEmpty(name)) {
			name = Base64.decodeStr(Base64.decodeStr(name));
		}
		if (StrUtil.isNotEmpty(pass)) {
			pass = Base64.decodeStr(Base64.decodeStr(pass));
		}
		if (StrUtil.isNotEmpty(code)) {
			code = Base64.decodeStr(Base64.decodeStr(code));
		}

		// 用户名密码
		Admin admin = adminService.login(name, pass);
		if (admin == null) {
			return renderError(m.get("loginStr.backError2")); // 用户名密码错误
		}

		if (!admin.getAuth()) {
			String imgCode = settingService.get("remoteCode");
			if (StrUtil.isEmpty(imgCode) || StrUtil.isNotEmpty(imgCode) && !imgCode.equalsIgnoreCase(code)) {
				return renderError(m.get("loginStr.backError1")); // 验证码不正确
			}
		} else {
			if (!authUtils.testKey(admin.getKey(), auth)) {
				return renderError(m.get("loginStr.backError6")); // 身份码不正确
			}
		}

		settingService.remove("remoteCode"); // 立刻销毁验证码

		Map<String, String> map = new HashMap<String, String>();
		map.put("creditKey", creditService.make(admin.getId()));
		map.put("system", SystemTool.getSystem());
		return renderSuccess(map);

	}

	
	@Mapping("getLocalType")
	public JsonResult getLocalType() {
		String localType = (String) Context.current().session("localType");
		if (StrUtil.isNotEmpty(localType)) {
			if ("local".equals(localType)) {
				return renderSuccess(m.get("remoteStr.local"));
			} else {
				Remote remote = (Remote) Context.current().session("remote");
				if (StrUtil.isNotEmpty(remote.getDescr())) {
					return renderSuccess(remote.getDescr());
				}

				return renderSuccess(remote.getIp() + ":" + remote.getPort());
			}
		}

		return renderSuccess("");
	}

	@Mapping("addAdmin")
	
	public JsonResult addAdmin(String name, String pass) {

		Long adminCount = sqlHelper.findAllCount(Admin.class);
		if (adminCount > 0) {
			return renderError(m.get("loginStr.backError4"));
		}

		if (!(PwdCheckUtil.checkContainUpperCase(pass) && PwdCheckUtil.checkContainLowerCase(pass) && PwdCheckUtil.checkContainDigit(pass) && PwdCheckUtil.checkPasswordLength(pass, "8", "100"))) {
			return renderError(m.get("loginStr.tips"));
		}

		Admin admin = new Admin();
		admin.setName(name);
		admin.setPass(pass);
		admin.setAuth(false);
		admin.setType(0);

		sqlHelper.insert(admin);

		return renderSuccess();
	}

	@Mapping("/getCode")
	public void getCode() throws Exception {
		Context.current().headerAdd("Pragma", "No-cache");
		Context.current().headerAdd("Cache-Control", "no-cache");
		Context.current().headerAdd("Expires", "0");
		Context.current().contentType("image/gif");

		SpecCaptcha specCaptcha = new SpecCaptcha(100, 40, 4);
		specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
		Context.current().sessionSet("captcha", specCaptcha.text().toLowerCase());
		specCaptcha.out(Context.current().outputStream());
	}

	@Mapping("/getRemoteCode")
	public void getRemoteCode() throws Exception {
		Context.current().headerAdd("Pragma", "No-cache");
		Context.current().headerAdd("Cache-Control", "no-cache");
		Context.current().headerAdd("Expires", "0");
		Context.current().contentType("image/gif");
		
		SpecCaptcha specCaptcha = new SpecCaptcha(100, 40, 4);
		specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
		settingService.set("remoteCode", specCaptcha.text());
		specCaptcha.out(Context.current().outputStream());
	}

	
	@Mapping("/changeLang")
	public JsonResult changeLang() {
		Long adminCount = sqlHelper.findAllCount(Admin.class);
		if (adminCount == 0) {
			// 只有初始化时允许修改语言
			if (settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
				settingService.set("lang", "");
			} else {
				settingService.set("lang", "en_US");
			}
		}

		return renderSuccess();
	}
	
}
