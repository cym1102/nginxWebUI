package com.cym.controller.api;

import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import com.cym.model.Param;
import com.cym.service.ParamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.util.StrUtil;

/**
 * 额外参数接口
 */
@Mapping("/api/param")
@Controller
public class ParamApiController extends BaseController {

	@Inject
	ParamService paramService;

	/**
	 * 根据项目获取参数列表
	 * 
	 * @param serverId   所属反向代理id
	 * @param locationId 所属代理目标id
	 * @param upstreamId 所属负载均衡id
	 * 
	 */
	@Mapping("getList")
	public JsonResult<List<Param>> getList(String serverId, //
			String locationId, //
			String upstreamId) {
		if (StrUtil.isEmpty(serverId) && StrUtil.isEmpty(locationId) && StrUtil.isEmpty(upstreamId)) {
			return renderError(m.get("apiStr.paramError"));
		}

		List<Param> list = paramService.getList(serverId, locationId, upstreamId);
		return renderSuccess(list);
	}

	/**
	 * 添加或编辑参数
	 * 
	 * @param param 额外参数
	 * 
	 */
	@Mapping("insertOrUpdate")
	public JsonResult<?> insertOrUpdate(Param param) {
		int count = 0;
		if (StrUtil.isNotEmpty(param.getLocationId())) {
			count++;
		}
		if (StrUtil.isNotEmpty(param.getServerId())) {
			count++;
		}
		if (StrUtil.isNotEmpty(param.getUpstreamId())) {
			count++;
		}

		if (count != 1) {
			return renderError(m.get("apiStr.paramError"));
		}

		sqlHelper.insertOrUpdate(param);

		return renderSuccess(param);
	}

	/**
	 * 删除额外参数
	 * 
	 * @param id 参数id
	 * 
	 */
	@Mapping("del")
	public JsonResult<?> del(String id) {
		sqlHelper.deleteById(id, Param.class);

		return renderSuccess();
	}

}
