package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.ext.UpstreamExt;
import com.cym.model.Location;
import com.cym.model.Server;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.ParamService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/upstream")
public class UpstreamController extends BaseController {
	@Autowired
	UpstreamService upstreamService;
	@Autowired
	ParamService paramService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page, String keywords) {
		page = upstreamService.search(page, keywords);

		List<UpstreamExt> list = new ArrayList<UpstreamExt>();
		for (Upstream upstream : page.getRecords(Upstream.class)) {
			UpstreamExt upstreamExt = new UpstreamExt();
			upstreamExt.setUpstream(upstream);

			List<String> str = new ArrayList<String>();
			List<UpstreamServer> servers = upstreamService.getUpstreamServers(upstream.getId());
			for (UpstreamServer upstreamServer : servers) {
				str.add(buildStr(upstreamServer, upstream.getProxyType()));
			}

			upstreamExt.setServerStr(StrUtil.join("<br>", str));
			list.add(upstreamExt);
		}
		page.setRecords(list);

		modelAndView.addObject("page", page);
		modelAndView.addObject("keywords", keywords);
		modelAndView.setViewName("/adminPage/upstream/index");
		return modelAndView;
	}

	public String buildStr(UpstreamServer upstreamServer, Integer proxyType) {
		String status = "";
		if (!"none".equals(upstreamServer.getStatus())) {
			status = upstreamServer.getStatus();
		}

		return upstreamServer.getServer() + ":" + upstreamServer.getPort() //
				+ " weight=" + upstreamServer.getWeight() //
				+ " fail_timeout=" + upstreamServer.getFailTimeout() + "s"//
				+ " max_fails=" + upstreamServer.getMaxFails() //
				+ " " + status;

	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(String upstreamJson, String upstreamParamJson, String upstreamServerJson) {
		Upstream upstream = JSONUtil.toBean(upstreamJson, Upstream.class);
		List<UpstreamServer> upstreamServers = JSONUtil.toList( JSONUtil.parseArray(upstreamServerJson), UpstreamServer.class);
		
		
		if (StrUtil.isEmpty(upstream.getId())) {
			Long count = upstreamService.getCountByName(upstream.getName());
			if (count > 0) {
				return renderError("与已有负载均衡重名");
			}
		}

		upstreamService.addOver(upstream, upstreamServers, upstreamParamJson);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {

		UpstreamExt upstreamExt = new UpstreamExt();
		upstreamExt.setUpstream(sqlHelper.findById(id, Upstream.class));
		upstreamExt.setUpstreamServerList(upstreamService.getUpstreamServers(id));

		upstreamExt.setParamJson(paramService.getJsonByTypeId(upstreamExt.getUpstream().getId(), "upstream"));

		return renderSuccess(upstreamExt);
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {

		upstreamService.del(id);

		return renderSuccess();
	}

}
