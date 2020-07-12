package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.ext.UpstreamExt;
import com.cym.model.Remote;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.ParamService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.TelnetUtils;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/upstream")
public class UpstreamController extends BaseController {
	@Autowired
	UpstreamService upstreamService;
	@Autowired
	ParamService paramService;
	@Autowired
	SettingService settingService;

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

			upstreamExt.setServerStr(StrUtil.join("", str));
			list.add(upstreamExt);
		}
		page.setRecords(list);


		modelAndView.addObject("page", page);
		modelAndView.addObject("keywords", keywords);
		modelAndView.setViewName("/adminPage/upstream/index");
		return modelAndView;
	}

	public String buildStr(UpstreamServer upstreamServer, Integer proxyType) {
		String status = "无策略";
		if (!"none".equals(upstreamServer.getStatus())) {
			status = upstreamServer.getStatus();
		}

		String monitorStatus = "";

		String upstreamMonitor = settingService.get("upstreamMonitor");
		if ("true".equals(upstreamMonitor)) {
			monitorStatus += "<td>";
			if (upstreamServer.getMonitorStatus() == 1) {
				monitorStatus += "<span class='green'>正常</span>";
			} else {
				monitorStatus += "<span class='red'>异常</span>";
			}
			monitorStatus += "</td>";
		}
		System.err.println(upstreamServer.getServer()+ ":" +upstreamServer.getMonitorStatus()); 

		return "<tr><td>" + upstreamServer.getServer() + ":" + upstreamServer.getPort() + "</td>"//
				+ "<td>weight=" + upstreamServer.getWeight() + "</td>"//
				+ "<td>fail_timeout=" + upstreamServer.getFailTimeout() + "s</td>"//
				+ "<td>max_fails=" + upstreamServer.getMaxFails() + "</td>"//
				+ "<td>" + status + "</td>" //
				+ monitorStatus + "</tr>";

	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(String upstreamJson, String upstreamParamJson, String upstreamServerJson) {
		Upstream upstream = JSONUtil.toBean(upstreamJson, Upstream.class);
		List<UpstreamServer> upstreamServers = JSONUtil.toList(JSONUtil.parseArray(upstreamServerJson), UpstreamServer.class);

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

	@RequestMapping("setMonitor")
	@ResponseBody
	public JsonResult setMonitor(String id, Integer monitor) {
		Upstream upstream = new Upstream();
		upstream.setId(id);
		upstream.setMonitor(monitor);
		sqlHelper.updateById(upstream);

		return renderSuccess();
	}

	@RequestMapping("upstreamStatus")
	@ResponseBody
	public JsonResult upstreamStatus(HttpSession httpSession) {
		Map<String, String> map = new HashMap<>();
		map.put("mail", settingService.get("mail"));

		String upstreamMonitor = settingService.get("upstreamMonitor");
		map.put("upstreamMonitor", upstreamMonitor != null ? upstreamMonitor : "false");

		return renderSuccess(map);
	}

	@RequestMapping("upstreamOver")
	@ResponseBody
	public JsonResult upstreamOver(String mail, String upstreamMonitor) {
		settingService.set("mail", mail);
		settingService.set("upstreamMonitor", upstreamMonitor);

		if (upstreamMonitor.equals("true")) {
			// 马上检测一次
			List<UpstreamServer> upstreamServers = upstreamService.getAllServer();
			for (UpstreamServer upstreamServer : upstreamServers) {
				if (!TelnetUtils.isRunning(upstreamServer.getServer(), upstreamServer.getPort())) {
					upstreamServer.setMonitorStatus(0);
				} else {
					upstreamServer.setMonitorStatus(1);
				}

				sqlHelper.updateById(upstreamServer);
			}
		}
		return renderSuccess();
	}
}
