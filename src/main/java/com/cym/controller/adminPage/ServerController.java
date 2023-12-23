package com.cym.controller.adminPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.ext.ServerExt;
import com.cym.model.Cert;
import com.cym.model.Http;
import com.cym.model.Location;
import com.cym.model.Password;
import com.cym.model.Remote;
import com.cym.model.Server;
import com.cym.model.Stream;
import com.cym.model.Upstream;
import com.cym.model.Www;
import com.cym.service.ConfService;
import com.cym.service.ParamService;
import com.cym.service.ServerService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;
import com.cym.utils.TelnetUtils;
import com.cym.utils.ToolUtils;
import com.github.odiszapc.nginxparser.NgxBlock;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxDumper;
import com.github.odiszapc.nginxparser.NgxParam;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/server")
public class ServerController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	ServerService serverService;
	@Inject
	UpstreamService upstreamService;
	@Inject
	ParamService paramService;
	@Inject
	SettingService settingService;
	@Inject
	ConfService confService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page, String keywords) {
		page = serverService.search(page, keywords);

		List<ServerExt> exts = new ArrayList<ServerExt>();
		for (Server server : (List<Server>) page.getRecords()) {
			ServerExt serverExt = new ServerExt();
			if (server.getEnable() == null) {
				server.setEnable(false);
			}

			// 描述回车转<br>
			if (StrUtil.isNotEmpty(server.getDescr())) {
				server.setDescr(server.getDescr().replace("\n", "<br>").replace(" ", "&nbsp;"));
			}

			serverExt.setServer(server);
			if (server.getProxyType() == 0) {
				serverExt.setLocationStr(buildLocationStr(server.getId()));
			} else {
				Upstream upstream = sqlHelper.findById(server.getProxyUpstreamId(), Upstream.class);
				serverExt.setLocationStr(m.get("serverStr.server") + ": " + (upstream != null ? upstream.getName() : ""));
			}

			serverExt.setHref((server.getSsl() == 1 ? "https" : "http") + ("://" + server.getServerName() + ":" + server.getListen()));
			exts.add(serverExt);
		}
		page.setRecords(exts);

		modelAndView.put("page", page);

		List<Upstream> upstreamList = upstreamService.getListByProxyType(0);
		modelAndView.put("upstreamList", upstreamList);
		modelAndView.put("upstreamSize", upstreamList.size());

		List<Upstream> upstreamTcpList = upstreamService.getListByProxyType(1);
		modelAndView.put("upstreamTcpList", upstreamTcpList);
		modelAndView.put("upstreamTcpSize", upstreamTcpList.size());

		List<Cert> certs = sqlHelper.findAll(Cert.class);
		for (Cert cert : certs) {
			if (cert.getType() == 0 || cert.getType() == 2) {
				cert.setDomain(cert.getDomain() + "(" + cert.getEncryption() + ")");
			}
		}
		modelAndView.put("certList", certs);
		modelAndView.put("wwwList", sqlHelper.findAll(Www.class));

		modelAndView.put("passwordList", sqlHelper.findAll(Password.class));

		modelAndView.put("keywords", keywords);
		modelAndView.view("/adminPage/server/index.html");
		return modelAndView;
	}

	private String buildLocationStr(String id) {
		List<String> str = new ArrayList<String>();
		List<Location> locations = serverService.getLocationByServerId(id);

		for (Location location : locations) {
			String descr = m.get("commonStr.descr");
			if (StrUtil.isNotEmpty(location.getDescr())) {
				descr = location.getDescr();
			}

			if (location.getType() == 0) {
				str.add("<span class='path'>" + location.getPath() + "</span>"//
						+ "<a class='descrBtn' href='javascript:editLocationDescr(\"" + location.getId() + "\")'>" + descr + "</a>"//
						+ "<br>"//
						+ "<span class='value'>" + location.getValue() + "</span>");
			} else if (location.getType() == 1) {
				str.add("<span class='path'>" + location.getPath() + "</span>"//
						+ "<a class='descrBtn' href='javascript:editLocationDescr(\"" + location.getId() + "\")'>" + descr + "</a>"//
						+ "<br>"//
						+ "<span class='value'>"//
						+ location.getRootPath() + "</span>");
			} else if (location.getType() == 2) {
				Upstream upstream = sqlHelper.findById(location.getUpstreamId(), Upstream.class);
				if (upstream != null) {
					str.add("<span class='path'>" + location.getPath() + "</span>"//
							+ "<a class='descrBtn' href='javascript:editLocationDescr(\"" + location.getId() + "\")'>" + descr + "</a>"//
							+ "<br>"//
							+ "<span class='value'>http://" + upstream.getName() + (location.getUpstreamPath() != null ? location.getUpstreamPath() : "") + "</span>");
				}
			} else if (location.getType() == 3) {
				str.add("<span class='path'>" + location.getPath() + "</span>" //
						+ "<a class='descrBtn' href='javascript:editLocationDescr(\"" + location.getId() + "\")'>" + descr + "</a>");
			}

		}
		return StrUtil.join("<br>", str);
	}

	@Mapping("addOver")
	public JsonResult addOver(String serverJson, String serverParamJson, String locationJson) {
		Server server = JSONUtil.toBean(serverJson, Server.class);
		List<Location> locations = JSONUtil.toList(JSONUtil.parseArray(locationJson), Location.class);

		if (StrUtil.isEmpty(server.getId())) {
			server.setSeq(SnowFlakeUtils.getId());
		}

		if (server.getProxyType() == 0) {
			serverService.addOver(server, serverParamJson, locations);
		} else {
			serverService.addOverTcp(server, serverParamJson);
		}

		return renderSuccess();
	}

	@Mapping("setEnable")
	public JsonResult setEnable(Server server) {
		sqlHelper.updateById(server);
		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		Server server = sqlHelper.findById(id, Server.class);

		ServerExt serverExt = new ServerExt();
		serverExt.setServer(server);
		List<Location> list = serverService.getLocationByServerId(id);
		for (Location location : list) {
			String json = paramService.getJsonByTypeId(location.getId(), "location");
			location.setLocationParamJson(json);
		}
		serverExt.setLocationList(list);
		String json = paramService.getJsonByTypeId(server.getId(), "server");
		serverExt.setParamJson(json);

		return renderSuccess(serverExt);
	}

	@Mapping("del")
	public JsonResult del(String id) {
		serverService.deleteById(id);

		return renderSuccess();
	}

	@Mapping("importServer")
	public JsonResult importServer(String nginxPath) {

		if (StrUtil.isEmpty(nginxPath) || !FileUtil.exist(nginxPath)) {
			return renderError(m.get("serverStr.fileNotExist"));
		}

		try {
			serverService.importServer(nginxPath);
			return renderSuccess(m.get("serverStr.importSuccess"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return renderError(m.get("serverStr.importFail"));
		}
	}

	@Mapping("testPort")
	public JsonResult testPort() {
		List<Server> servers = sqlHelper.findAll(Server.class);

		List<String> ips = new ArrayList<>();
		for (Server server : servers) {
			String ip = "";
			String port = "";
			if (server.getListen().contains(":")) {
				String[] strArray = server.getListen().split(":");

				port = strArray[strArray.length - 1];
				ip = server.getListen().replace(":" + port, "");
			} else {
				ip = "127.0.0.1";
				port = server.getListen();
			}

			// 如果是范围端口,只检测第一个
			if (port.contains("-")) {
				port = port.split("-")[0];
			}

			try {
				if (TelnetUtils.isRunning(ip, Integer.parseInt(port)) && !ips.contains(server.getListen())) {
					ips.add(server.getListen());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		if (ips.size() == 0) {
			return renderSuccess();
		} else {
			return renderError(m.get("serverStr.portUserdList") + ": " + StrUtil.join(" , ", ips));
		}

	}

	@Mapping("editDescr")
	public JsonResult editDescr(String id, String descr) {
		Server server = new Server();
		server.setId(id);
		server.setDescr(descr);
		sqlHelper.updateById(server);

		return renderSuccess();
	}

	@Mapping("preview")
	public JsonResult preview(String id, String type) {
		NgxBlock ngxBlock = null;
		if (type.equals("server")) {
			Server server = sqlHelper.findById(id, Server.class);
			ngxBlock = confService.bulidBlockServer(server);
		} else if (type.equals("upstream")) {
			Upstream upstream = sqlHelper.findById(id, Upstream.class);
			ngxBlock = confService.buildBlockUpstream(upstream);
		} else if (type.equals("http")) {
			List<Http> httpList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Http.class);
			ngxBlock = new NgxBlock();
			ngxBlock.addValue("http");
			for (Http http : httpList) {
				if (http.getEnable() == null || !http.getEnable()) {
					continue;
				}

				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(http.getName().trim() + " " + http.getValue().trim());
				ngxBlock.addEntry(ngxParam);
			}
		} else if (type.equals("stream")) {
			List<Stream> streamList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Stream.class);
			ngxBlock = new NgxBlock();
			ngxBlock.addValue("stream");
			for (Stream stream : streamList) {
				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(stream.getName() + " " + stream.getValue());
				ngxBlock.addEntry(ngxParam);
			}
		}
		NgxConfig ngxConfig = new NgxConfig();
		ngxConfig.addEntry(ngxBlock);

		String conf = ToolUtils.handleConf(new NgxDumper(ngxConfig).dump());

		return renderSuccess(conf);
	}

	@Mapping("setOrder")
	public JsonResult setOrder(String id, Integer count) {
		serverService.setSeq(id, count);
		return renderSuccess();
	}

	@Mapping("getDescr")
	public JsonResult getDescr(String id) {
		Server server = sqlHelper.findById(id, Server.class);
		return renderSuccess(server.getDescr());
	}

	@Mapping("getLocationDescr")
	public JsonResult getLocationDescr(String id) {
		Location location = sqlHelper.findById(id, Location.class);
		return renderSuccess(location.getDescr());
	}

	@Mapping("setLocationDescr")
	public JsonResult setLocationDescr(String id, String descr) {
		Location location = new Location();
		location.setId(id);
		location.setDescr(descr);
		sqlHelper.updateById(location);

		return renderSuccess();
	}

	@Mapping("upload")
	public JsonResult upload(Context context, UploadedFile file) {
		try {
			File temp = new File(FileUtil.getTmpDir() + File.separator + file.getName().replace("..", ""));
			file.transferTo(temp);

			// 移动文件
			File dest = new File(homeConfig.home + "cert/" + file.getName().replace("..", ""));
			while (FileUtil.exist(dest)) {
				dest = new File(dest.getPath() + "_1");
			}
			FileUtil.move(temp, dest, true);

			String localType = (String) context.session("localType");
			if ("remote".equals(localType)) {
				Remote remote = (Remote) context.session("remote");

				HashMap<String, Object> paramMap = new HashMap<>();
				paramMap.put("file", temp);

				String rs = HttpUtil.post(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/upload", paramMap);
				JsonResult jsonResult = JSONUtil.toBean(rs, JsonResult.class);
				FileUtil.del(temp);
				return jsonResult;
			}

			return renderSuccess(dest.getPath().replace("\\", "/"));
		} catch (IllegalStateException | IOException e) {
			logger.error(e.getMessage(), e);
		}

		return renderError();
	}
}
