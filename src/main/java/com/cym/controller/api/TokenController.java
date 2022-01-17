package com.cym.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import com.cym.model.Admin;
import com.cym.service.AdminService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

/**
 * 获取token
 */
@Mapping("token")
@Controller
public class TokenController extends BaseController {
	@Inject
	AdminService adminService;

	/**
	 * 获取Token
	 * 
	 * @param name 用户名
	 * @param pass 密码
	 */
	@Mapping("getToken")
	public JsonResult getToken(String name, String pass) {

		// 用户名密码
		Admin admin = adminService.login(name, pass);
		if (admin == null) {
			return renderError(m.get("loginStr.backError2")); // 用户名密码错误
		}
		if (!admin.getApi()) {
			return renderError(m.get("loginStr.backError7")); // 无接口权限
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("token", adminService.makeToken(admin.getId()));

		return renderSuccess(map);
	}
}
