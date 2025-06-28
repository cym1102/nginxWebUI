package com.cym.utils;

import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;

import com.cym.config.HomeConfig;
import com.cym.model.Admin;
import com.cym.service.AdminService;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.util.StrUtil;

/**
 * Author: D.Yang Email: koyangslash@gmail.com Date: 16/10/9 Time: 下午1:37
 * Describe: 基础控制器
 */
public class BaseController {
	@Inject
	protected SqlHelper sqlHelper;
	@Inject
	protected AdminService adminService;
	@Inject
	protected MessageUtils m;
	@Inject
	protected HomeConfig homeConfig;

	protected JsonResult renderError() {
		JsonResult result = new JsonResult();
		result.setSuccess(false);
		result.setStatus("500");
		return result;
	}

	protected JsonResult renderAuthError() {
		JsonResult result = new JsonResult();
		result.setSuccess(false);
		result.setStatus("401");
		return result;
	}

	protected JsonResult renderError(String msg) {
		JsonResult result = renderError();
		result.setMsg(msg);
		return result;
	}

	protected JsonResult renderSuccess() {
		JsonResult result = new JsonResult();
		result.setSuccess(true);
		result.setStatus("200");
		return result;
	}

	protected JsonResult renderSuccess(Object obj) {
		JsonResult result = renderSuccess();
		result.setObj(obj);
		return result;
	}

	public void setPage(Page page) {
		String limit = (String) Context.current().session("limit");
		if (StrUtil.isNotEmpty(limit)) {
			page.setLimit(Integer.parseInt(limit));
		}
	}

	public Admin getAdmin() {

		Admin admin = (Admin) Context.current().session("admin");
		if (admin == null) {
			String token = Context.current().header("token");
			admin = adminService.getByToken(token);
		}
		if (admin == null) {
			String creditKey = Context.current().param("creditKey");
			admin = adminService.getByCreditKey(creditKey);
		}

		return admin;
	}
}
