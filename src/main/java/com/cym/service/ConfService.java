package com.cym.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.HomeConfig;
import com.cym.ext.AsycPack;
import com.cym.ext.ConfExt;
import com.cym.ext.ConfFile;
import com.cym.model.Bak;
import com.cym.model.BakSub;
import com.cym.model.Basic;
import com.cym.model.Cert;
import com.cym.model.CertCode;
import com.cym.model.Http;
import com.cym.model.Location;
import com.cym.model.Param;
import com.cym.model.Password;
import com.cym.model.Server;
import com.cym.model.Stream;
import com.cym.model.Template;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.SystemTool;
import com.cym.utils.ToolUtils;
import com.github.odiszapc.nginxparser.NgxBlock;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxDumper;
import com.github.odiszapc.nginxparser.NgxEntry;
import com.github.odiszapc.nginxparser.NgxParam;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@Component
public class ConfService {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	UpstreamService upstreamService;
	@Inject
	SettingService settingService;
	@Inject
	ServerService serverService;
	@Inject
	LocationService locationService;
	@Inject
	ParamService paramService;
	@Inject
	SqlHelper sqlHelper;
	@Inject
	TemplateService templateService;
	@Inject
	OperateLogService operateLogService;
	@Inject
	HomeConfig homeConfig;
	@Inject
	CertService certService;

	public synchronized ConfExt buildConf(Boolean decompose, Boolean check) {
		ConfExt confExt = new ConfExt();
		confExt.setFileList(new ArrayList<>());

		String nginxPath = settingService.get("nginxPath");
		if (check) {
			nginxPath = homeConfig.home + "temp/nginx.conf";
		}
		try {

			NgxConfig ngxConfig = new NgxConfig();

			// 获取基本参数
			List<Basic> basicList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Basic.class);
			for (Basic basic : basicList) {
				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(basic.getName().trim() + " " + basic.getValue().trim());
				ngxConfig.addEntry(ngxParam);
			}

			// 获取http
			List<Http> httpList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Http.class);
			boolean hasHttp = false;
			NgxBlock ngxBlockHttp = new NgxBlock();
			ngxBlockHttp.addValue("http");
			for (Http http : httpList) {
				if (http.getEnable() == null || !http.getEnable()) {
					continue;
				}

				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(http.getName().trim() + " " + http.getValue().trim());
				ngxBlockHttp.addEntry(ngxParam);

				hasHttp = true;
			}

			// 添加upstream
			NgxParam ngxParam;
			List<Upstream> upstreams = upstreamService.getListByProxyType(0);

			for (Upstream upstream : upstreams) {
				NgxBlock ngxBlockServer = new NgxBlock();
				ngxBlockServer.addValue("upstream " + upstream.getName().trim());

				if (StrUtil.isNotEmpty(upstream.getDescr())) {
					String[] descrs = upstream.getDescr().split("\n");
					for (String d : descrs) {
						ngxParam = new NgxParam();
						ngxParam.addValue("# " + d);
						ngxBlockServer.addEntry(ngxParam);
					}

				}

				if (StrUtil.isNotEmpty(upstream.getTactics())) {
					ngxParam = new NgxParam();
					ngxParam.addValue(upstream.getTactics());
					ngxBlockServer.addEntry(ngxParam);
				}

				List<UpstreamServer> upstreamServers = upstreamService.getUpstreamServers(upstream.getId());
				for (UpstreamServer upstreamServer : upstreamServers) {
					ngxParam = new NgxParam();
					ngxParam.addValue("server " + buildNodeStr(upstreamServer));
					ngxBlockServer.addEntry(ngxParam);
				}

				// 自定义参数
				List<Param> paramList = paramService.getListByTypeId(upstream.getId(), "upstream");
				for (Param param : paramList) {
					setSameParam(param, ngxBlockServer);
				}

				hasHttp = true;

				if (decompose) {
					String filename = addConfFile(nginxPath, confExt, "upstreams." + upstream.getName() + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + filename);
					ngxBlockHttp.addEntry(ngxParam);

				} else {
					ngxBlockHttp.addEntry(ngxBlockServer);
				}

			}

			// 添加server
			List<Server> servers = serverService.getListByProxyType(new String[] { "0" });
			for (Server server : servers) {
				if (server.getEnable() == null || !server.getEnable()) {
					continue;
				}

				NgxBlock ngxBlockServer = bulidBlockServer(server);
				hasHttp = true;

				// 是否需要分解
				if (decompose) {
					String name = "all";

					if (StrUtil.isNotEmpty(server.getServerName())) {
						name = server.getServerName();
					}

					String filename = addConfFile(nginxPath, confExt, name + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + filename);

					if (noContain(ngxBlockHttp, ngxParam)) {
						ngxBlockHttp.addEntry(ngxParam);
					}

				} else {
					ngxBlockHttp.addEntry(ngxBlockServer);
				}

			}
			if (hasHttp) {
				ngxConfig.addEntry(ngxBlockHttp);
			}

			// TCP/UDP转发
			// 创建stream
			List<Stream> streamList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Stream.class);
			boolean hasStream = false;
			NgxBlock ngxBlockStream = new NgxBlock();
			ngxBlockStream.addValue("stream");
			for (Stream stream : streamList) {
				ngxParam = new NgxParam();
				ngxParam.addValue(stream.getName() + " " + stream.getValue());
				ngxBlockStream.addEntry(ngxParam);

				hasStream = true;
			}

			// 添加upstream
			upstreams = upstreamService.getListByProxyType(1);
			for (Upstream upstream : upstreams) {
				NgxBlock ngxBlockServer = buildBlockUpstream(upstream);

				if (decompose) {
					String filename = addConfFile(nginxPath, confExt, "upstreams." + upstream.getName() + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + filename);
					ngxBlockStream.addEntry(ngxParam);
				} else {
					ngxBlockStream.addEntry(ngxBlockServer);
				}

				hasStream = true;
			}

			// 添加server
			servers = serverService.getListByProxyType(new String[] { "1", "2" });
			for (Server server : servers) {
				if (server.getEnable() == null || !server.getEnable()) {
					continue;
				}

				NgxBlock ngxBlockServer = bulidBlockServer(server);

				if (decompose) {
					String type = "";
					if (server.getProxyType() == 0) {
						type = "http";
					} else if (server.getProxyType() == 1) {
						type = "tcp";
					} else if (server.getProxyType() == 2) {
						type = "udp";
					}

					String filename = addConfFile(nginxPath, confExt, type + "." + server.getListen() + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + filename);
					ngxBlockStream.addEntry(ngxParam);
				} else {
					ngxBlockStream.addEntry(ngxBlockServer);
				}

				hasStream = true;
			}

			if (hasStream) {
				ngxConfig.addEntry(ngxBlockStream);
			}

			String conf = ToolUtils.handleConf(new NgxDumper(ngxConfig).dump());
			// 将多个;替换成单一;
			while (conf.contains(";;")) {
				conf = conf.replaceAll(";;", ";");
			}

			confExt.setConf(conf);

			return confExt;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	public NgxBlock buildBlockUpstream(Upstream upstream) {
		NgxParam ngxParam = null;

		NgxBlock ngxBlockServer = new NgxBlock();

		ngxBlockServer.addValue("upstream " + upstream.getName());

		if (StrUtil.isNotEmpty(upstream.getDescr())) {
			String[] descrs = upstream.getDescr().split("\n");
			for (String d : descrs) {
				ngxParam = new NgxParam();
				ngxParam.addValue("# " + d);
				ngxBlockServer.addEntry(ngxParam);
			}

		}

		if (StrUtil.isNotEmpty(upstream.getTactics())) {
			ngxParam = new NgxParam();
			ngxParam.addValue(upstream.getTactics());
			ngxBlockServer.addEntry(ngxParam);
		}

		List<UpstreamServer> upstreamServers = upstreamService.getUpstreamServers(upstream.getId());
		for (UpstreamServer upstreamServer : upstreamServers) {
			ngxParam = new NgxParam();
			ngxParam.addValue("server " + buildNodeStr(upstreamServer));
			ngxBlockServer.addEntry(ngxParam);
		}

		// 自定义参数
		List<Param> paramList = paramService.getListByTypeId(upstream.getId(), "upstream");
		for (Param param : paramList) {
			setSameParam(param, ngxBlockServer);
		}

		return ngxBlockServer;
	}

	public NgxBlock bulidBlockServer(Server server) {
		NgxParam ngxParam = null;

		NgxBlock ngxBlockServer = new NgxBlock();
		if (server.getProxyType() == 0) {
			ngxBlockServer.addValue("server");

			if (StrUtil.isNotEmpty(server.getDescr())) {
				String[] descrs = server.getDescr().split("\n");
				for (String d : descrs) {
					ngxParam = new NgxParam();
					ngxParam.addValue("# " + d);
					ngxBlockServer.addEntry(ngxParam);
				}
			}

			// 监听域名
			if (StrUtil.isNotEmpty(server.getServerName())) {
				ngxParam = new NgxParam();
				ngxParam.addValue("server_name " + server.getServerName());
				ngxBlockServer.addEntry(ngxParam);
			}

			// 监听端口
			ngxParam = new NgxParam();
			String value = "listen " + server.getListen();
			if (server.getDef() == 1) {
				value += " default";
			}
			if (server.getProxyProtocol() == 1) {
				value += " proxy_protocol";
			}

			if (server.getSsl() != null && server.getSsl() == 1) {
				value += " ssl";
				if (server.getHttp2() != null && server.getHttp2() == 1) {
					value += " http2";
				}
			}
			ngxParam.addValue(value);
			ngxBlockServer.addEntry(ngxParam);

			// 密码配置
			if (StrUtil.isNotEmpty(server.getPasswordId())) {
				Password password = sqlHelper.findById(server.getPasswordId(), Password.class);

				if (password != null) {
					ngxParam = new NgxParam();
					ngxParam.addValue("auth_basic \"" + password.getDescr() + "\"");
					ngxBlockServer.addEntry(ngxParam);

					ngxParam = new NgxParam();
					ngxParam.addValue("auth_basic_user_file " + password.getPath());
					ngxBlockServer.addEntry(ngxParam);
				}
			}

			// ssl配置
			setServerSsl(server, ngxBlockServer);

			// 自定义参数
			String type = "server";
			if (server.getProxyType() != 0) {
				type += server.getProxyType();
			}
			List<Param> paramList = paramService.getListByTypeId(server.getId(), type);
			for (Param param : paramList) {
				setSameParam(param, ngxBlockServer);
			}

			List<Location> locationList = serverService.getLocationByServerId(server.getId());

			// location参数配置
			for (Location location : locationList) {
				NgxBlock ngxBlockLocation = new NgxBlock();
				ngxBlockLocation.addValue("location");
				ngxBlockLocation.addValue(location.getPath());

				if (StrUtil.isNotEmpty(location.getDescr())) {
					String[] descrs = location.getDescr().split("\n");
					for (String d : descrs) {
						ngxParam = new NgxParam();
						ngxParam.addValue("# " + d);
						ngxBlockLocation.addEntry(ngxParam);
					}
				}

				if (location.getType() == 0 || location.getType() == 2) { // location或负载均衡

					if (location.getType() == 0) {
						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_pass " + location.getValue());
						ngxBlockLocation.addEntry(ngxParam);
					} else if (location.getType() == 2) {
						Upstream upstream = sqlHelper.findById(location.getUpstreamId(), Upstream.class);
						if (upstream != null) {
							ngxParam = new NgxParam();
							ngxParam.addValue("proxy_pass " + location.getUpstreamType() + "://" + upstream.getName() + (location.getUpstreamPath() != null ? location.getUpstreamPath() : ""));
							ngxBlockLocation.addEntry(ngxParam);
						}
					}

					if (location.getHeader() == 1) { // 设置header参数
						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header Host " + location.getHeaderHost());
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Real-IP $remote_addr");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Forwarded-Host $http_host");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Forwarded-Port $server_port");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Forwarded-Proto $scheme");
						ngxBlockLocation.addEntry(ngxParam);
					}

					if (location.getWebsocket() == 1) { // 设置websocket
						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_http_version 1.1");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header Upgrade $http_upgrade");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header Connection \"upgrade\"");
						ngxBlockLocation.addEntry(ngxParam);
					}

					if (location.getCros() == 1) { // 设置跨域
						ngxParam = new NgxParam();
						ngxParam.addValue("add_header Access-Control-Allow-Origin *");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("add_header Access-Control-Allow-Methods *");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("add_header Access-Control-Allow-Headers *");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("add_header Access-Control-Allow-Credentials true");
						ngxBlockLocation.addEntry(ngxParam);

						NgxBlock ngxBlock = new NgxBlock();
						ngxBlock.addValue("if ($request_method = 'OPTIONS')");
						ngxParam = new NgxParam();

						ngxParam.addValue("return 204");
						ngxBlock.addEntry(ngxParam);

						ngxBlockLocation.addEntry(ngxBlock);

					}

					if (server.getSsl() == 1 && server.getRewrite() == 1) { // redirect http转https
						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_redirect http:// https://");
						ngxBlockLocation.addEntry(ngxParam);
					}

				} else if (location.getType() == 1) { // 静态html
					if (location.getRootType() != null && location.getRootType().equals("alias")) {
						ngxParam = new NgxParam();
						ngxParam.addValue("alias " + ToolUtils.handlePath(location.getRootPath()));
						ngxBlockLocation.addEntry(ngxParam);
					} else {
						ngxParam = new NgxParam();
						ngxParam.addValue("root " + ToolUtils.handlePath(location.getRootPath()));
						ngxBlockLocation.addEntry(ngxParam);
					}

					if (StrUtil.isNotEmpty(location.getRootPage())) {
						ngxParam = new NgxParam();
						ngxParam.addValue("index " + location.getRootPage());
						ngxBlockLocation.addEntry(ngxParam);
					}

				} else if (location.getType() == 3) { // 空白location

				}

				// 自定义参数
				paramList = paramService.getListByTypeId(location.getId(), "location");
				for (Param param : paramList) {
					setSameParam(param, ngxBlockLocation);
				}

				ngxBlockServer.addEntry(ngxBlockLocation);

			}

		} else {
			ngxBlockServer.addValue("server");

			// 监听端口
			ngxParam = new NgxParam();
			String value = "listen " + server.getListen();
			if (server.getProxyProtocol() == 1) {
				value += " proxy_protocol";
			}
			if (server.getProxyType() == 2) {
				value += " udp";
			}
			if (server.getSsl() != null && server.getSsl() == 1) {
				value += " ssl";
			}

			ngxParam.addValue(value);
			ngxBlockServer.addEntry(ngxParam);

			// 指向负载均衡
			Upstream upstream = sqlHelper.findById(server.getProxyUpstreamId(), Upstream.class);
			if (upstream != null) {
				ngxParam = new NgxParam();
				ngxParam.addValue("proxy_pass " + upstream.getName());
				ngxBlockServer.addEntry(ngxParam);
			}

			// ssl配置
			setServerSsl(server, ngxBlockServer);

			// 自定义参数
			String type = "server";
			if (server.getProxyType() != 0) {
				type += server.getProxyType();
			}
			List<Param> paramList = paramService.getListByTypeId(server.getId(), type);
			for (Param param : paramList) {
				setSameParam(param, ngxBlockServer);
			}
		}

		return ngxBlockServer;
	}

	/**
	 * 配置ssl
	 * 
	 * @param server
	 * @param ngxBlockServer
	 */
	private void setServerSsl(Server server, NgxBlock ngxBlockServer) {
		NgxParam ngxParam = null;
		if (server.getSsl() == 1) {
			if (StrUtil.isNotEmpty(server.getPem()) && StrUtil.isNotEmpty(server.getKey())) {
				ngxParam = new NgxParam();
				ngxParam.addValue("ssl_certificate " + ToolUtils.handlePath(server.getPem()));
				ngxBlockServer.addEntry(ngxParam);

				ngxParam = new NgxParam();
				ngxParam.addValue("ssl_certificate_key " + ToolUtils.handlePath(server.getKey()));
				ngxBlockServer.addEntry(ngxParam);

				if (StrUtil.isNotEmpty(server.getProtocols())) {
					ngxParam = new NgxParam();
					ngxParam.addValue("ssl_protocols " + server.getProtocols());
					ngxBlockServer.addEntry(ngxParam);
				}

			}

			// https添加80端口重写
			if (server.getProxyType() == 0 && server.getRewrite() == 1) {
				if (StrUtil.isNotEmpty(server.getRewriteListen())) {
					ngxParam = new NgxParam();
					String reValue = "listen " + server.getRewriteListen();
					if (server.getDef() == 1) {
						reValue += " default";
					}
					if (server.getProxyProtocol() == 1) {
						reValue += " proxy_protocol";
					}
					ngxParam.addValue(reValue);
					ngxBlockServer.addEntry(ngxParam);
				}

				String port = "";
				if (server.getListen().contains(":")) {
					port = server.getListen().split(":")[1];
				} else {
					port = server.getListen();
				}

				NgxBlock ngxBlock = new NgxBlock();
				ngxBlock.addValue("if ($scheme = http)");
				ngxParam = new NgxParam();

				ngxParam.addValue("return 301 https://$host:" + port + "$request_uri");
				ngxBlock.addEntry(ngxParam);

				ngxBlockServer.addEntry(ngxBlock);

			}
		}
	}

	/**
	 * include防止重复
	 * 
	 * @param ngxBlockHttp
	 * @param ngxParam
	 * @return
	 */
	private boolean noContain(NgxBlock ngxBlockHttp, NgxParam ngxParam) {
		for (NgxEntry ngxEntry : ngxBlockHttp.getEntries()) {
			if (ngxEntry.toString().equals(ngxParam.toString())) {
				return false;
			}
		}

		return true;
	}

	public String buildNodeStr(UpstreamServer upstreamServer) {

		if (upstreamServer.getServer().contains(":")) {
			upstreamServer.setServer("[" + upstreamServer.getServer() + "]");
		}

		String conf = upstreamServer.getServer() + ":" + upstreamServer.getPort();
		if (upstreamServer.getWeight() != null) {
			conf += " weight=" + upstreamServer.getWeight();
		}
		if (upstreamServer.getFailTimeout() != null) {
			conf += " fail_timeout=" + upstreamServer.getFailTimeout() + "s";
		}
		if (upstreamServer.getMaxFails() != null) {
			conf += " max_fails=" + upstreamServer.getMaxFails();
		}
		if (upstreamServer.getMaxConns() != null) {
			conf += " max_conns=" + upstreamServer.getMaxConns();
		}
		if (!"none".equals(upstreamServer.getStatus())) {
			conf += " " + upstreamServer.getStatus();
		}
		if (upstreamServer.getParam() != null) {
			conf += " " + upstreamServer.getParam();
		}
		return conf;
	}

	private void setSameParam(Param param, NgxBlock ngxBlock) {
		if (StrUtil.isEmpty(param.getTemplateValue())) {
			NgxParam ngxParam = new NgxParam();
			if (StrUtil.isNotEmpty(param.getName().trim())) {
				param.setName(param.getName().trim() + " ");
			}

			ngxParam.addValue(param.getName() + param.getValue().trim());
			ngxBlock.addEntry(ngxParam);
		} else {
			List<Param> params = templateService.getParamList(param.getTemplateValue());
			for (Param paramSub : params) {
				NgxParam ngxParam = new NgxParam();
				if (StrUtil.isNotEmpty(paramSub.getName().trim())) {
					paramSub.setName(paramSub.getName().trim() + " ");
				}

				ngxParam.addValue(paramSub.getName() + paramSub.getValue().trim());
				ngxBlock.addEntry(ngxParam);
			}
		}
	}

	private String addConfFile(String nginxPath, ConfExt confExt, String name, NgxBlock ngxBlockServer) {
		name = name.replace(" ", "_").replaceAll("[!@#$%^&*()_+=\\{\\}\\[\\]\"<>,/;':\\\\|`~]+", "_");

		boolean hasSameName = false;
		for (ConfFile confFile : confExt.getFileList()) {
			if (confFile.getName().equals(name)) {
				confFile.setConf(confFile.getConf() + "\n" + buildStr(ngxBlockServer));
				hasSameName = true;
			}
		}

		if (!hasSameName) {
			ConfFile confFile = new ConfFile();
			confFile.setName(name);
			confFile.setConf(buildStr(ngxBlockServer));
			confExt.getFileList().add(confFile);
		}

		return new File(nginxPath).getParent().replace("\\", "/") + "/conf.d/" + name;
	}

	private String buildStr(NgxBlock ngxBlockServer) {

		NgxConfig ngxConfig = new NgxConfig();
		ngxConfig.addEntry(ngxBlockServer);

		return ToolUtils.handleConf(new NgxDumper(ngxConfig).dump());
	}

	public void replace(String nginxPath, String nginxContent, List<String> subContent, List<String> subName, Boolean isReplace, String adminName) {

		String beforeConf = null;
		if (isReplace) {
			// 先读取已有的配置
			beforeConf = FileUtil.readString(nginxPath, StandardCharsets.UTF_8);
		}

		String confd = new File(nginxPath).getParent().replace("\\", "/") + "/conf.d/";
		// 删除conf.d下全部文件
		FileUtil.del(confd);
		FileUtil.mkdir(confd);

		// 写入主文件
		FileUtil.writeString(nginxContent, nginxPath.replace(" ", "_"), StandardCharsets.UTF_8);
		String decompose = settingService.get("decompose");

		if ("true".equals(decompose)) {
			// 写入conf.d文件
			if (subContent != null) {
				for (int i = 0; i < subContent.size(); i++) {
					String tagert = (new File(nginxPath).getParent().replace("\\", "/") + "/conf.d/" + subName.get(i)).replace(" ", "_");
					FileUtil.writeString(subContent.get(i), tagert, StandardCharsets.UTF_8); // 清空
				}
			}
		}

		// 备份文件
		if (isReplace) {
			Bak bak = new Bak();
			bak.setTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
			bak.setContent(nginxContent);
			sqlHelper.insert(bak);

			// 备份子文件
			for (int i = 0; i < subContent.size(); i++) {
				BakSub bakSub = new BakSub();
				bakSub.setBakId(bak.getId());

				bakSub.setName(subName.get(i));
				bakSub.setContent(subContent.get(i));
				sqlHelper.insert(bakSub);
			}

			// 写入操作日志
			if (StrUtil.isNotEmpty(adminName)) {
				operateLogService.addLog(beforeConf, nginxContent, adminName);
			}
		}
	}

	public AsycPack getAsycPack(String[] asycData) {
		AsycPack asycPack = new AsycPack();
		if (hasStr(asycData, "basic") || hasStr(asycData, "all")) {
			asycPack.setBasicList(sqlHelper.findAll(Basic.class));
		}

		if (hasStr(asycData, "http") || hasStr(asycData, "all")) {
			asycPack.setHttpList(sqlHelper.findAll(Http.class));
		}

		if (hasStr(asycData, "server") || hasStr(asycData, "all")) {
			List<Server> serverList = sqlHelper.findAll(Server.class);
			for (Server server : serverList) {
				if (StrUtil.isNotEmpty(server.getPem()) && FileUtil.exist(server.getPem())) {
					server.setPemStr(FileUtil.readString(server.getPem(), StandardCharsets.UTF_8));
				}

				if (StrUtil.isNotEmpty(server.getKey()) && FileUtil.exist(server.getKey())) {
					server.setKeyStr(FileUtil.readString(server.getKey(), StandardCharsets.UTF_8));
				}
			}
			asycPack.setServerList(serverList);
			asycPack.setLocationList(sqlHelper.findAll(Location.class));
		}

		if (hasStr(asycData, "password") || hasStr(asycData, "all")) {
			List<Password> passwordList = sqlHelper.findAll(Password.class);
			for (Password password : passwordList) {
				if (StrUtil.isNotEmpty(password.getPath()) && FileUtil.exist(password.getPath())) {
					password.setPathStr(FileUtil.readString(password.getPath(), StandardCharsets.UTF_8));
				}

			}
			asycPack.setPasswordList(passwordList);
		}

		if (hasStr(asycData, "upstream") || hasStr(asycData, "all")) {
			asycPack.setUpstreamList(sqlHelper.findAll(Upstream.class));
			asycPack.setUpstreamServerList(sqlHelper.findAll(UpstreamServer.class));
		}

		if (hasStr(asycData, "stream") || hasStr(asycData, "all")) {
			asycPack.setStreamList(sqlHelper.findAll(Stream.class));
		}

		if (hasStr(asycData, "param") || hasStr(asycData, "all")) {
			asycPack.setTemplateList(sqlHelper.findAll(Template.class));
			asycPack.setParamList(sqlHelper.findAll(Param.class));
		}

		if (hasStr(asycData, "cert") || hasStr(asycData, "all")) {
			asycPack.setCertList(sqlHelper.findAll(Cert.class));
			asycPack.setCertCodeList(sqlHelper.findAll(CertCode.class));
			asycPack.setAcmeZip(certService.getAcmeZipBase64());
		}

		return asycPack;
	}

	private boolean hasStr(String[] asycData, String data) {
		for (String str : asycData) {
			if (str.equals(data)) {
				return true;
			}
		}
		return false;
	}

	public void setAsycPack(AsycPack asycPack) {
		// 不要同步Cert表
		try {

			if (asycPack.getBasicList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Basic.class);
				sqlHelper.insertAll(asycPack.getBasicList());
			}

			if (asycPack.getHttpList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Http.class);
				sqlHelper.insertAll(asycPack.getHttpList());
			}

			if (asycPack.getServerList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Server.class);
				sqlHelper.insertAll(asycPack.getServerList());
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Location.class);
				sqlHelper.insertAll(asycPack.getLocationList());

				for (Server server : asycPack.getServerList()) {
					try {
						if (StrUtil.isNotEmpty(server.getPem()) && StrUtil.isNotEmpty(server.getPemStr())) {
							String pemPath = SystemTool.isWindows() ? server.getPem().replace("*", "_") : server.getPem();
							FileUtil.writeString(server.getPemStr(), pemPath, StandardCharsets.UTF_8);
						}
						if (StrUtil.isNotEmpty(server.getKey()) && StrUtil.isNotEmpty(server.getKeyStr())) {
							String keyPath = SystemTool.isWindows() ? server.getKey().replace("*", "_") : server.getKey();
							FileUtil.writeString(server.getKeyStr(), keyPath, StandardCharsets.UTF_8);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (asycPack.getUpstreamList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Upstream.class);
				sqlHelper.insertAll(asycPack.getUpstreamList());
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), UpstreamServer.class);
				sqlHelper.insertAll(asycPack.getUpstreamServerList());
			}

			if (asycPack.getStreamList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Stream.class);
				sqlHelper.insertAll(asycPack.getStreamList());
			}

			if (asycPack.getTemplateList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Template.class);
				sqlHelper.insertAll(asycPack.getTemplateList());
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Param.class);
				sqlHelper.insertAll(asycPack.getParamList());
			}

			if (asycPack.getPasswordList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Password.class);
				sqlHelper.insertAll(asycPack.getPasswordList());

				for (Password password : asycPack.getPasswordList()) {
					if (StrUtil.isNotEmpty(password.getPath()) && StrUtil.isNotEmpty(password.getPathStr())) {
						FileUtil.writeString(password.getPathStr(), password.getPath(), StandardCharsets.UTF_8);
					}
				}
			}

			// 导入证书
			if (asycPack.getCertList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Cert.class);
				sqlHelper.insertAll(asycPack.getCertList());
			}
			if (asycPack.getCertCodeList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), CertCode.class);
				sqlHelper.insertAll(asycPack.getCertCodeList());
			}

			certService.writeAcmeZipBase64(asycPack.getAcmeZip());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public List<Cert> getApplyCerts() {
		List<Cert> certs = sqlHelper.findListByQuery(new ConditionAndWrapper().ne(Cert::getType, 1), Cert.class);
		return certs;
	}

}
