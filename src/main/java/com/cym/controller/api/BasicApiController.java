package com.cym.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cym.model.Basic;
import com.cym.model.Http;
import com.cym.model.Stream;
import com.cym.service.BasicService;
import com.cym.service.HttpService;
import com.cym.service.StreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "基础参数接口")
@RestController
@RequestMapping("/api/basic")
public class BasicApiController extends BaseController {
	@Autowired
	HttpService httpService;
	@Autowired
	BasicService basicService;
	@Autowired
	StreamService streamService;

	@ApiOperation("获取Http参数")
	@PostMapping("getHttp")
	public JsonResult<List<Http>> getHttp() {
		return renderSuccess(httpService.findAll());
	}

	@ApiOperation("添加或编辑Http参数")
	@PostMapping("insertOrUpdateHttp")
	public JsonResult<Http> insertOrUpdateHttp(Http http) {
		if (StrUtil.isEmpty(http.getName()) || StrUtil.isEmpty(http.getValue())) {
			return renderError(m.get("apiStr.noContent"));
		}

		if (StrUtil.isEmpty(http.getId())) {
			http.setSeq(SnowFlakeUtils.getId());
		}
		sqlHelper.insertOrUpdate(http);
		return renderSuccess(http);
	}

	@ApiOperation("删除Http参数")
	@PostMapping("delHttp")
	public JsonResult delHttp(String id) {
		sqlHelper.deleteById(id, Http.class);
		return renderSuccess();
	}

	@ApiOperation("获取基础参数")
	@PostMapping("getBasic")
	public JsonResult<List<Basic>> getBasic() {
		return renderSuccess(basicService.findAll());
	}

	@ApiOperation("添加或编辑基础参数")
	@PostMapping("insertOrUpdateBasic")
	public JsonResult<Basic> insertOrUpdateBasic(Basic basic) {
		if (StrUtil.isEmpty(basic.getName()) || StrUtil.isEmpty(basic.getValue())) {
			return renderError(m.get("apiStr.noContent"));
		}
		
		if (StrUtil.isEmpty(basic.getId())) {
			basic.setSeq(SnowFlakeUtils.getId());
		}
		sqlHelper.insertOrUpdate(basic);
		return renderSuccess(basic);
	}

	@ApiOperation("删除基础参数")
	@PostMapping("delBasic")
	public JsonResult delBasic(String id) {
		sqlHelper.deleteById(id, Basic.class);
		return renderSuccess();
	}

	@ApiOperation("获取Stream参数")
	@PostMapping("getStream")
	public JsonResult<List<Stream>> getStream() {
		return renderSuccess(streamService.findAll());
	}

	@ApiOperation("添加或编辑Stream参数")
	@PostMapping("insertOrUpdateStream")
	public JsonResult<Stream> insertOrUpdateStream(Stream stream) {
		if (StrUtil.isEmpty(stream.getName()) || StrUtil.isEmpty(stream.getValue())) {
			return renderError(m.get("apiStr.noContent"));
		}
		if (StrUtil.isEmpty(stream.getId())) {
			stream.setSeq(SnowFlakeUtils.getId());
		}
		sqlHelper.insertOrUpdate(stream);
		return renderSuccess(stream);
	}

	@ApiOperation("删除Stream参数")
	@PostMapping("delStream")
	public JsonResult delStream(String id) {
		sqlHelper.deleteById(id, Stream.class);
		return renderSuccess();
	}
}
