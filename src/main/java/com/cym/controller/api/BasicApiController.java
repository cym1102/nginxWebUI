package com.cym.controller.api;

import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

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

/**
 * 基础参数接口
 * 
 */
@Mapping("/api/basic")
@Controller
public class BasicApiController extends BaseController {
	@Inject
	HttpService httpService;
	@Inject
	BasicService basicService;
	@Inject
	StreamService streamService;

	/**
	 * 获取Http参数
	 * 
	 */
	@Mapping("getHttp")
	public JsonResult<List<Http>> getHttp() {
		return renderSuccess(httpService.findAll());
	}

	/**
	 * 添加或编辑Http参数
	 * 
	 * @param http Http参数
	 *
	 */
	@Mapping("insertOrUpdateHttp")
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

	/**
	 * 删除Http参数
	 * 
	 * @param id http参数id
	 * 
	 */
	@Mapping("delHttp")
	public JsonResult delHttp(String id) {
		sqlHelper.deleteById(id, Http.class);
		return renderSuccess();
	}

	/**
	 * 获取基础参数
	 * 
	 */
	@Mapping("getBasic")
	public JsonResult<List<Basic>> getBasic() {
		return renderSuccess(basicService.findAll());
	}

	/**
	 * 添加或编辑基础参数
	 * 
	 * @param basic 基础参数
	 * 
	 */
	@Mapping("insertOrUpdateBasic")
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

	/**
	 * 删除基础参数
	 * 
	 * @param id 基础参数id
	 * 
	 */
	@Mapping("delBasic")
	public JsonResult delBasic(String id) {
		sqlHelper.deleteById(id, Basic.class);
		return renderSuccess();
	}

	/**
	 * 获取Stream参数
	 * 
	 */
	@Mapping("getStream")
	public JsonResult<List<Stream>> getStream() {
		return renderSuccess(streamService.findAll());
	}

	/**
	 * 添加或编辑Stream参数
	 * 
	 * @param stream Stream参数
	 * 
	 */
	@Mapping("insertOrUpdateStream")
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

	/**
	 * 删除Stream参数
	 * 
	 * @param id Stream参数id
	 * 
	 */
	@Mapping("delStream")
	public JsonResult delStream(String id) {
		sqlHelper.deleteById(id, Stream.class);
		return renderSuccess();
	}
}
