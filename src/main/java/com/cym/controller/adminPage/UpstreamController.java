package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.ext.UpstreamExt;
import com.cym.model.Server;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.ParamService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/upstream")
public class UpstreamController extends BaseController {
	@Inject
	UpstreamService upstreamService;
	@Inject
	ParamService paramService;
	@Inject
	SettingService settingService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page, String keywords) {

		page = upstreamService.search(page, keywords);

		List<UpstreamExt> list = new ArrayList<UpstreamExt>();
		for (Upstream upstream : (List<Upstream>) page.getRecords()) {
			UpstreamExt upstreamExt = new UpstreamExt();
			upstreamExt.setUpstream(upstream);

			List<String> str = new ArrayList<String>();
			List<UpstreamServer> servers = upstreamService.getUpstreamServers(upstream.getId());
			for (UpstreamServer upstreamServer : servers) {
				str.add(buildStr(upstreamServer, upstream.getProxyType()));
			}

			// 描述回车转<br>
			if (StrUtil.isNotEmpty(upstream.getDescr())) {
				upstream.setDescr(upstream.getDescr().replace("\n", "<br>"));
			}

			upstreamExt.setServerStr(StrUtil.join("", str));
			list.add(upstreamExt);
		}
		page.setRecords(list);

		modelAndView.put("page", page);
		modelAndView.put("keywords", keywords);

		modelAndView.put("upstreamMonitor", settingService.get("upstreamMonitor"));
		modelAndView.view("/adminPage/upstream/index.html");
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

		if (upstreamServer.getWeight() != null) {
			html += "weight=" + upstreamServer.getWeight() + " ";
		}
		if (upstreamServer.getFailTimeout() != null) {
			html += "fail_timeout=" + upstreamServer.getFailTimeout() + "s ";
		}
		if (upstreamServer.getMaxFails() != null) {
			html += "max_fails=" + upstreamServer.getMaxFails() + " ";
		}
		if (upstreamServer.getMaxConns() != null) {
			html += "max_conns=" + upstreamServer.getMaxConns() + " ";
		}
		html += "</td><td class='short50'>" + status + "</td>" + monitorStatus + "</tr>";
		return html;
	}

	@Mapping("addOver")
	public JsonResult addOver(String upstreamJson, String upstreamParamJson, String upstreamServerJson) {
		Upstream upstream = JSONUtil.toBean(upstreamJson, Upstream.class);
		List<UpstreamServer> upstreamServers = JSONUtil.toList(JSONUtil.parseArray(upstreamServerJson), UpstreamServer.class);

		if (StrUtil.isEmpty(upstream.getId())) {
			upstream.setSeq(SnowFlakeUtils.getId());
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

	@Mapping("detail")
	public JsonResult detail(String id) {

		UpstreamExt upstreamExt = new UpstreamExt();
		upstreamExt.setUpstream(sqlHelper.findById(id, Upstream.class));
		upstreamExt.setUpstreamServerList(upstreamService.getUpstreamServers(id));

		upstreamExt.setParamJson(paramService.getJsonByTypeId(upstreamExt.getUpstream().getId(), "upstream"));

		return renderSuccess(upstreamExt);
	}

	@Mapping("del")
	public JsonResult del(String id) {

		upstreamService.deleteById(id);

		return renderSuccess();
	}

	@Mapping("setMonitor")
	public JsonResult setMonitor(String id, Integer monitor) {
		Upstream upstream = new Upstream();
		upstream.setId(id);
		upstream.setMonitor(monitor);
		sqlHelper.updateById(upstream);

		return renderSuccess();
	}

	@Mapping("upstreamStatus")
	public JsonResult upstreamStatus() {
		Map<String, String> map = new HashMap<>();
		map.put("mail", settingService.get("mail"));

		String upstreamMonitor = settingService.get("upstreamMonitor");
		map.put("upstreamMonitor", upstreamMonitor != null ? upstreamMonitor : "false");

		return renderSuccess(map);
	}

	@Mapping("upstreamOver")
	public JsonResult upstreamOver(String mail, String upstreamMonitor) {
		settingService.set("mail", mail);
		settingService.set("upstreamMonitor", upstreamMonitor);

		if (upstreamMonitor.equals("true")) {
			upstreamService.resetMonitorStatus();
		}

		return renderSuccess();
	}

	@Mapping("setOrder")
	public JsonResult setOrder(String id, Integer count) {
		upstreamService.setSeq(id, count);
		return renderSuccess();
	}

	@Mapping("getDescr")
	public JsonResult getDescr(String id) {
		Upstream upstream = sqlHelper.findById(id, Upstream.class);

		return renderSuccess(upstream.getDescr());
	}

	@Mapping("editDescr")
	public JsonResult editDescr(String id, String descr) {
		Upstream upstream = new Upstream();
		upstream.setId(id);
		upstream.setDescr(descr);
		sqlHelper.updateById(upstream);

		return renderSuccess();
	}
}
