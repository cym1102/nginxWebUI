package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.ext.ServerExt;
import com.cym.model.Cert;
import com.cym.model.Location;
import com.cym.model.Server;
import com.cym.model.Upstream;
import com.cym.model.Www;
import com.cym.service.ParamService;
import com.cym.service.ServerService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.TelnetUtils;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/server")
public class ServerController extends BaseController {
	@Autowired
	ServerService serverService;
	@Autowired
	UpstreamService upstreamService;
	@Autowired
	ParamService paramService;
	@Autowired
	SettingService settingService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page, String sort, String direction, String keywords) {
		page = serverService.search(page, sort, direction, keywords);

		List<ServerExt> exts = new ArrayList<ServerExt>();
		for (Server server : page.getRecords(Server.class)) {
			ServerExt serverExt = new ServerExt();
			if (server.getEnable() == null) {
				server.setEnable(false);
			}

			serverExt.setServer(server);
			if (server.getProxyType() == 0) {
				serverExt.setLocationStr(buildLocationStr(server.getId()));
			} else {
				Upstream upstream = sqlHelper.findById(server.getProxyUpstreamId(), Upstream.class);
				serverExt.setLocationStr("负载均衡: " + (upstream != null ? upstream.getName() : ""));
			}

			exts.add(serverExt);
		}
		page.setRecords(exts);

		modelAndView.addObject("page", page);

		List<Upstream> upstreamList = upstreamService.getListByProxyType(0);
		modelAndView.addObject("upstreamList", upstreamList);
		modelAndView.addObject("upstreamSize", upstreamList.size());

		List<Upstream> upstreamTcpList = upstreamService.getListByProxyType(1);
		modelAndView.addObject("upstreamTcpList", upstreamTcpList);
		modelAndView.addObject("upstreamTcpSize", upstreamTcpList.size());

		modelAndView.addObject("certList", sqlHelper.findAll(Cert.class));
		modelAndView.addObject("wwwList", sqlHelper.findAll(Www.class));
		modelAndView.addObject("sort", sort);
		modelAndView.addObject("direction", direction);

		modelAndView.addObject("keywords", keywords);
		modelAndView.setViewName("/adminPage/server/index");
		return modelAndView;
	}

	private String buildLocationStr(String id) {
		List<String> str = new ArrayList<String>();
		List<Location> locations = serverService.getLocationByServerId(id);
		for (Location location : locations) {
			if (location.getType() == 0) {
				str.add("<span class='path'>" + location.getPath() + "</span><span class='value'>" + location.getValue() + "</span>");
			} else if (location.getType() == 1) {
				str.add("<span class='path'>" + location.getPath() + "</span><span class='value'>" + location.getRootPath() + "</span>");
			} else if (location.getType() == 2) {
				Upstream upstream = sqlHelper.findById(location.getUpstreamId(), Upstream.class);
				if (upstream != null) {
					str.add("<span class='path'>" + location.getPath() + "</span><span class='value'>http://" + upstream.getName()
							+ (location.getUpstreamPath() != null ? location.getUpstreamPath() : "") + "</span>");
				}
			} else if (location.getType() == 3) {
				str.add("<span class='path'>" + location.getPath() + "</span>");
			}

		}
		return StrUtil.join("<br>", str);
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(String serverJson, String serverParamJson, String locationJson) {
		Server server = JSONUtil.toBean(serverJson, Server.class);
		List<Location> locations = JSONUtil.toList(JSONUtil.parseArray(locationJson), Location.class);

		if (server.getProxyType() == 0) {
			try {
				serverService.addOver(server, serverParamJson, locations);
			} catch (Exception e) {
				return renderError(e.getMessage());
			}
		} else {
			serverService.addOverTcp(server, serverParamJson);
		}

		return renderSuccess();
	}

	@RequestMapping("setEnable")
	@ResponseBody
	public JsonResult setEnable(Server server) {
		sqlHelper.updateById(server);
		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		Server server = sqlHelper.findById(id, Server.class);

		ServerExt serverExt = new ServerExt();
		serverExt.setServer(server);
		List<Location> list = serverService.getLocationByServerId(id);
		for (Location location : list) {
			String json = paramService.getJsonByTypeId(location.getId(), "location");
			location.setLocationParamJson(json != null ? json : null);
		}
		serverExt.setLocationList(list);
		String json = paramService.getJsonByTypeId(server.getId(), "server");
		serverExt.setParamJson(json != null ? json : null);

		return renderSuccess(serverExt);
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		serverService.deleteById(id);

		return renderSuccess();
	}

	@RequestMapping("clone")
	@ResponseBody
	public JsonResult clone(String id) {
		serverService.clone(id);

		return renderSuccess();
	}

	@RequestMapping("importServer")
	@ResponseBody
	public JsonResult importServer(String nginxPath) {

		if (!FileUtil.exist(nginxPath)) {
			return renderError("目标文件不存在");
		}

		try {
			serverService.importServer(nginxPath);
			return renderSuccess("导入成功");
		} catch (Exception e) {
			e.printStackTrace();

			return renderError("导入失败：" + e.getMessage());
		}
	}

	@RequestMapping("testPort")
	@ResponseBody
	public JsonResult testPort() {
		List<Server> servers = sqlHelper.findAll(Server.class);

		List<String> ips = new ArrayList<>();
		for (Server server : servers) {
			String ip = "";
			String port = "";
			if (server.getListen().contains(":")) {
				ip = server.getListen().split(":")[0];
				port = server.getListen().split(":")[1];
			} else {
				ip = "127.0.0.1";
				port = server.getListen();
			}

			if (TelnetUtils.isRunning(ip, Integer.parseInt(port)) && !ips.contains(server.getListen())) {
				ips.add(server.getListen());
			}
		}

		if (ips.size() == 0) {
			return renderSuccess();
		} else {
			return renderError("以下端口被占用: " + StrUtil.join(" , ", ips));
		}

	}

}
