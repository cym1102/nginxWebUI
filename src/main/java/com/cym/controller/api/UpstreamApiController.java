package com.cym.controller.api;

import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.UpstreamService;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 负载均衡(upstream)接口
 */
@Mapping("/api/upstream")
@Controller
public class UpstreamApiController extends BaseController {
	@Inject
	UpstreamService upstreamService;

	/**
	 * 获取upstream分页列表
	 * 
	 * @param current  当前页数(从1开始)
	 * @param limit    每页数量(默认为10)
	 * @param keywords 查询关键字
	 * 
	 */
	@Mapping("getPage")
	public JsonResult<Page<Upstream>> getPage(Integer current, //
			Integer limit, //
			String keywords) {
		Page page = new Page();
		page.setCurr(current);
		page.setLimit(limit);
		page = upstreamService.search(page, keywords);

		return renderSuccess(page);
	}

	/**
	 * 添加或编辑upstream
	 * 
	 * @param upstream
	 * 
	 */
	@Mapping("insertOrUpdate")
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

	/**
	 * 删除upstream
	 * 
	 * @param id upstream的id
	 */
	@Mapping("delete")
	public JsonResult<?> delete(String id) {
		upstreamService.deleteById(id);

		return renderSuccess();
	}

	/**
	 * 根据upstreamId获取server列表
	 * 
	 * @param upstreamId upstream的id
	 */
	@Mapping("getServerByUpstreamId")
	public JsonResult<List<UpstreamServer>> getServerByUpstreamId(String upstreamId) {
		List<UpstreamServer> list = upstreamService.getUpstreamServers(upstreamId);

		return renderSuccess(list);
	}

	/**
	 * 添加或编辑server
	 * 
	 * @param upstreamServer 负载节点server
	 */
	@Mapping("insertOrUpdateServer")
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

//	/**
//	 * 删除server
//	 * @param id  upstream的id
//	 * 
//	 */
//	@ApiOperation("删除server")
//	@Mapping("deleteServer")
//	public JsonResult deleteServer(String id) {
//		upstreamService.del(id);
//		return renderSuccess();
//	}
}
