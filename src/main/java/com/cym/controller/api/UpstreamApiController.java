package com.cym.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "负载均衡(upstream)接口")
@RestController
@RequestMapping("/api/upstream")
public class UpstreamApiController extends BaseController {
	@Autowired
	UpstreamService upstreamService;

	@SuppressWarnings("unchecked")
	@ApiOperation("获取upstream分页列表")
	@PostMapping("getPage")
	public JsonResult<Page<Upstream>> getPage(@ApiParam("当前页数(从1开始)") @RequestParam(defaultValue = "1") Integer current, //
			@ApiParam("每页数量(默认为10)") @RequestParam(defaultValue = "10") Integer limit, //
			@ApiParam("查询关键字") String keywords) {
		Page page = new Page();
		page.setCurr(current);
		page.setLimit(limit);
		page = upstreamService.search(page, keywords);

		return renderSuccess(page);
	}

	@ApiOperation("添加或编辑upstream")
	@PostMapping("insertOrUpdate")
	public JsonResult<?> insertOrUpdate(Upstream upstream) {
		if (StrUtil.isEmpty(upstream.getName())) {
			return renderError("name" + m.get("apiStr.notFill"));
		}

		if (StrUtil.isEmpty(upstream.getId())) {
			Long count = upstreamService.getCountByName(upstream.getName());
			if (count > 0) {
				return renderError(m.get("upstreamStr.sameName"));
			}
		} else {
			Long count = upstreamService.getCountByNameWithOutId(upstream.getName(), upstream.getId());
			if (count > 0) {
				return renderError(m.get("upstreamStr.sameName"));
			}
		}
		if (StrUtil.isEmpty(upstream.getId())) {
			upstream.setSeq(SnowFlakeUtils.getId());
		}
		sqlHelper.insertOrUpdate(upstream);
		return renderSuccess(upstream);
	}

	@ApiOperation("删除upstream")
	@PostMapping("delete")
	public JsonResult<?> delete(String id) {
		upstreamService.deleteById(id);

		return renderSuccess();
	}

	@ApiOperation("根据upstreamId获取server列表")
	@PostMapping("getServerByUpstreamId")
	public JsonResult<List<UpstreamServer>> getServerByUpstreamId(String upstreamId) {
		List<UpstreamServer> list = upstreamService.getUpstreamServers(upstreamId);

		return renderSuccess(list);
	}

	@ApiOperation("添加或编辑server")
	@PostMapping("insertOrUpdateServer")
	public JsonResult insertOrUpdateServer(UpstreamServer upstreamServer) {
		if (StrUtil.isEmpty(upstreamServer.getUpstreamId())) {
			return renderError("upstreamId" + m.get("apiStr.notFill"));
		}
		if (null == upstreamServer.getPort()) {
			return renderError("port" + m.get("apiStr.notFill"));
		}
		if (StrUtil.isEmpty(upstreamServer.getServer())) {
			return renderError("server" + m.get("apiStr.notFill"));
		}
		
		sqlHelper.insertOrUpdate(upstreamServer);
		return renderSuccess(upstreamServer);
	}

	@ApiOperation("删除server")
	@PostMapping("deleteServer")
	public JsonResult deleteServer(String id) {
		upstreamService.del(id);
		return renderSuccess();
	}
}
