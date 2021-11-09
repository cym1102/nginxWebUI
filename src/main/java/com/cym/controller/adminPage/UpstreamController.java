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
import com.cym.model.Server;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.ParamService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;
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
		for (Upstream upstream :(List<Upstream>)  page.getRecords()) {
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
		
		modelAndView.addObject("upstreamMonitor", settingService.get("upstreamMonitor"));
		modelAndView.setViewName("/adminPage/upstream/index");
		return modelAndView;
	}

	public String buildStr(UpstreamServer upstreamServer, Integer proxyType) {
		String status = m.get("upstreamStr.noStatus");
		if (!"none".equals(upstreamServer.getStatus())) {
			status = upstreamServer.getStatus();
		}

		String monitorStatus = "";

		String upstreamMonitor = settingService.get("upstreamMonitor");
		if ("true".equals(upstreamMonitor)) {
			monitorStatus += "<td class='short50'>";
			if (upstreamServer.getMonitorStatus() == -1) {
				monitorStatus += "<span class='gray'>" + m.get("upstreamStr.gray") + "</span>";
			} else if (upstreamServer.getMonitorStatus() == 1) {
				monitorStatus += "<span class='green'>" + m.get("upstreamStr.green") + "</span>";
			} else {
				monitorStatus += "<span class='red'>" + m.get("upstreamStr.red") + "</span>";
			}
			monitorStatus += "</td>";
		}

		if (upstreamServer.getServer().contains(":")) {
			upstreamServer.setServer("[" + upstreamServer.getServer() + "]");
		}

		String html = "<tr><td class='short100'>" + upstreamServer.getServer() + ":" + upstreamServer.getPort() + "</td><td>";
				
		if(upstreamServer.getWeight()!=null) {
			html += "weight=" + upstreamServer.getWeight() + " ";
		}
		if(upstreamServer.getFailTimeout()!=null) {
			html += "fail_timeout=" + upstreamServer.getFailTimeout() + "s ";
		}
		if(upstreamServer.getMaxFails()!=null) {
			html += "max_fails=" + upstreamServer.getMaxFails() + " ";
		}
		if(upstreamServer.getMaxConns()!=null) {
			html += "max_conns=" + upstreamServer.getMaxConns() + " ";
		}
		html+=  "</td><td class='short50'>" + status + "</td>" + monitorStatus + "</tr>";
		return html;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(String upstreamJson, String upstreamParamJson, String upstreamServerJson) {
		Upstream upstream = JSONUtil.toBean(upstreamJson, Upstream.class);
		List<UpstreamServer> upstreamServers = JSONUtil.toList(JSONUtil.parseArray(upstreamServerJson), UpstreamServer.class);


		if (StrUtil.isEmpty(upstream.getId())) {
			upstream.setSeq( SnowFlakeUtils.getId());
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
			upstreamService.resetMonitorStatus();
		}

		return renderSuccess();
	}
	
	@RequestMapping("setOrder")
	@ResponseBody
	public JsonResult setOrder(String id, Integer count) {
		upstreamService.setSeq(id, count);
		return renderSuccess();
	}

}
