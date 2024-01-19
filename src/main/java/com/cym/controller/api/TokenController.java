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
	 * @apiNote 其他接口调用需要在http请求header中添加token, 其中token的获取需要先在管理员管理中, 打开用户的接口调用权限,
	 *          然后通过用户名密码调用获取token接口, 才能得到token
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
			return renderError(m.get("loginStr.notPermission")); // 无接口权限
		}

		admin = adminService.makeToken(admin.getId());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("token", admin.getToken());
		map.put("tokenTimeout", admin.getTokenTimeout());
		return renderSuccess(map);
	}
}
