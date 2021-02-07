package com.cym.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cym.model.Location;
import com.cym.model.Server;
import com.cym.service.ServerService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "反向代理(server)接口")
@RestController
@RequestMapping("/api/server")
public class ServerApiController extends BaseController {
	@Autowired
	ServerService serverService;

	@SuppressWarnings("unchecked")
	@ApiOperation("获取server分页列表")
	@PostMapping("getPage")
	public JsonResult<Page<Server>> getPage(@ApiParam("当前页数(从1开始)") @RequestParam(defaultValue = "1") Integer current, //
			@ApiParam("每页数量(默认为10)") @RequestParam(defaultValue = "10") Integer limit, //
			@ApiParam("查询关键字") String keywords) {
		Page page = new Page();
		page.setCurr(current);
		page.setLimit(limit);
		page = serverService.search(page, keywords);

		return renderSuccess(page);
	}

	@ApiOperation("添加或编辑server")
	@PostMapping("insertOrUpdate")
	public JsonResult<?> insertOrUpdate(Server server) {
		if (StrUtil.isEmpty(server.getListen())) {
			return renderError("listen" + m.get("apiStr.notFill"));
		}

		if (StrUtil.isEmpty(server.getId())) {
			server.setSeq(SnowFlakeUtils.getId());
		}
		sqlHelper.insertOrUpdate(server);
		return renderSuccess(server);
	}

	@ApiOperation("删除server")
	@PostMapping("delete")
	public JsonResult<?> delete(String id) {
		serverService.deleteById(id);

		return renderSuccess();
	}

	@ApiOperation("根据serverId获取location列表")
	@PostMapping("getLocationByServerId")
	public JsonResult<List<Location>> getLocationByServerId(String serverId) {
		List<Location> locationList = serverService.getLocationByServerId(serverId);

		return renderSuccess(locationList);
	}

	@ApiOperation("添加或编辑location")
	@PostMapping("insertOrUpdateLocation")
	public JsonResult<?> insertOrUpdateLocation(Location location) {
		if (StrUtil.isEmpty(location.getServerId())) {
			return renderError("serverId" + m.get("apiStr.notFill"));
		}
		if (StrUtil.isEmpty(location.getPath())) {
			return renderError("path" + m.get("apiStr.notFill"));
		}
		sqlHelper.insertOrUpdate(location);
		return renderSuccess(location);
	}

	@ApiOperation("删除location")
	@PostMapping("deleteLocation")
	public JsonResult<?> deleteLocation(String id) {
		sqlHelper.deleteById(id, Location.class);

		return renderSuccess();
	}
}
