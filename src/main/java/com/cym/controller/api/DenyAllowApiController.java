package com.cym.controller.api;

import java.io.IOException;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import com.cym.controller.adminPage.DenyAllowController;
import com.cym.model.DenyAllow;
import com.cym.service.DenyAllowService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.io.FileUtil;

/**
 * IP黑白名单接口
 */
@Mapping("/api/denyAllow")
@Controller
public class DenyAllowApiController extends BaseController {
	@Inject
	DenyAllowService denyAllowService;
	@Inject
	DenyAllowController denyAllowController;

	/**
	 * 获取全部IP黑白名单列表
	 * 
	 */
	@Mapping("getList")
	public JsonResult<List<DenyAllow>> getList() {
		List<DenyAllow> list = sqlHelper.findAll(DenyAllow.class);
		return renderSuccess(list);
	}

	/**
	 * 添加或编辑IP黑白名单
	 * 
	 * @param denyAllow IP黑白名单
	 * 
	 */
	@Mapping("insertOrUpdate")
	public JsonResult<?> insertOrUpdate(DenyAllow denyAllow){
		return renderSuccess(denyAllowController.addOver(denyAllow));
	}

	/**
	 * 删除IP黑白名单
	 * 
	 * @param id IP黑白名单id
	 * 
	 */
	@Mapping("del")
	public JsonResult<?> del(String id) {
		sqlHelper.deleteById(id, DenyAllow.class);

		return renderSuccess();
	}

}
