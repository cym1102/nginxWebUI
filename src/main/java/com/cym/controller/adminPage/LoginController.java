package com.cym.controller.adminPage;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
import com.wf.captcha.utils.CaptchaUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 登录页
 * 
 * @author Administrator
 *
 */
@RequestMapping("/adminPage/login")
@Controller
public class LoginController extends BaseController {
	@Autowired
	AdminService adminService;
	@Autowired
	CreditService creditService;
	@Autowired
	VersionConfig versionConfig;
	@Autowired
	AuthUtils authUtils;
	@Value("${project.version}")
	String currentVersion;

	@Autowired
	SettingService settingService;

	@RequestMapping("")
	public ModelAndView admin(ModelAndView modelAndView, HttpServletRequest request, HttpSession httpSession, String adminId) {
		modelAndView.addObject("adminCount", sqlHelper.findAllCount(Admin.class));
		modelAndView.setViewName("/adminPage/login/index");
		return modelAndView;
	}

	@RequestMapping("loginOut")
	public ModelAndView loginOut(ModelAndView modelAndView, HttpSession httpSession, HttpServletRequest request) {

		httpSession.removeAttribute("isLogin");
		modelAndView.setViewName("/adminPage/index");
		return modelAndView;
	}

	@RequestMapping("noServer")
	public ModelAndView noServer(ModelAndView modelAndView) {
		modelAndView.setViewName("/adminPage/login/noServer");
		return modelAndView;
	}

	@RequestMapping("login")
	@ResponseBody
	public JsonResult submitLogin(String name, String pass, String code, String authCode, String remember, HttpSession httpSession,HttpServletRequest httpServletRequest) {

		// 验证码
		if (!CaptchaUtil.ver(code, httpServletRequest)) {
			CaptchaUtil.clear(httpServletRequest); // 销毁验证码
			return renderError(m.get("loginStr.backError1")); // 验证码不正确
		}

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
		httpSession.setAttribute("localType", "local");
		httpSession.setAttribute("isLogin", true);
		httpSession.setAttribute("admin", admin);
		httpSession.removeAttribute("imgCode"); // 立刻销毁验证码

		// 检查更新
		versionConfig.getNewVersion();

		return renderSuccess(admin);
	}

	@RequestMapping("autoLogin")
	@ResponseBody
	public JsonResult autoLogin(String adminId, HttpSession httpSession) {

		// 用户名密码
		Admin admin = sqlHelper.findById(adminId, Admin.class);
		if (admin != null) {
			// 登录成功
			httpSession.setAttribute("localType", "local");
			httpSession.setAttribute("isLogin", true);
			httpSession.setAttribute("admin", admin);
			httpSession.removeAttribute("imgCode"); // 立刻销毁验证码

			// 检查更新
			versionConfig.getNewVersion();

			return renderSuccess(admin);
		} else {
			return renderError();
		}

	}

	@ResponseBody
	@RequestMapping("getAuth")
	public JsonResult getAuth(String name, String pass, String code, Integer remote, HttpSession httpSession, HttpServletRequest httpServletRequest) {
		// 验证码
		if (remote == null) {
			if (!CaptchaUtil.ver(code, httpServletRequest)) {
				CaptchaUtil.clear(httpServletRequest); // 销毁验证码
				return renderError(m.get("loginStr.backError1")); // 验证码不正确
			}
		}

		// 用户名密码
		Admin admin = adminService.login(name, pass);
		if (admin == null) {
			return renderError(m.get("loginStr.backError2")); // 用户名密码错误
		}

		Admin ad = new Admin();
		ad.setAuth(admin.getAuth());
		ad.setKey(admin.getKey());

		return renderSuccess(ad);
	}

	@ResponseBody
	@RequestMapping("getCredit")
	public JsonResult getCredit(String name, String pass, String code, String auth) {
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

	@ResponseBody
	@RequestMapping("getLocalType")
	public JsonResult getLocalType(HttpSession httpSession) {
		String localType = (String) httpSession.getAttribute("localType");
		if (StrUtil.isNotEmpty(localType)) {
			if ("local".equals(localType)) {
				return renderSuccess(m.get("remoteStr.local"));
			} else {
				Remote remote = (Remote) httpSession.getAttribute("remote");
				if (StrUtil.isNotEmpty(remote.getDescr())) {
					return renderSuccess(remote.getDescr());
				}

				return renderSuccess(remote.getIp() + ":" + remote.getPort());
			}
		}

		return renderSuccess("");
	}

	@RequestMapping("addAdmin")
	@ResponseBody
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

	@RequestMapping("/getCode")
	public void getCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		SpecCaptcha specCaptcha = new SpecCaptcha(100, 40, 4);
		specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
		CaptchaUtil.out(specCaptcha, httpServletRequest, httpServletResponse);
	}

	@RequestMapping("/getRemoteCode")
	public void getRemoteCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		SpecCaptcha specCaptcha = new SpecCaptcha(100, 40, 4);
		specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
		settingService.set("remoteCode", specCaptcha.text());
		specCaptcha.out(httpServletResponse.getOutputStream());
	}

	@ResponseBody
	@RequestMapping("/changeLang")
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
