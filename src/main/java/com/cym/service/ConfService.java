package com.cym.service;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.controller.adminPage.UpstreamController;
import com.cym.ext.AsycPack;
import com.cym.ext.ConfExt;
import com.cym.ext.ConfFile;
import com.cym.model.Cert;
import com.cym.model.Http;
import com.cym.model.Location;
import com.cym.model.Param;
import com.cym.model.Server;
import com.cym.model.Stream;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.utils.RuntimeTool;
import com.cym.utils.SystemTool;
import com.github.odiszapc.nginxparser.NgxBlock;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxDumper;
import com.github.odiszapc.nginxparser.NgxEntry;
import com.github.odiszapc.nginxparser.NgxParam;

import cn.craccd.sqlHelper.utils.CriteriaAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Service
public class ConfService {
	@Autowired
	UpstreamController upstreamController;
	@Autowired
	UpstreamService upstreamService;
	@Autowired
	SettingService settingService;
	@Autowired
	ServerService serverService;
	@Autowired
	LocationService locationService;
	@Autowired
	ParamService paramService;
	@Autowired
	SqlHelper sqlHelper;

	public ConfExt buildConf(Boolean decompose) {
		ConfExt confExt = new ConfExt();
		confExt.setFileList(new ArrayList<ConfFile>());

		String nginxPath = settingService.get("nginxPath");
		try {
			ClassPathResource resource = new ClassPathResource("nginxOrg.conf");
			InputStream inputStream = resource.getInputStream();

			NgxConfig ngxConfig = NgxConfig.read(inputStream);

			// 获取http
			List<Http> httpList = sqlHelper.findAll(Http.class);
			NgxBlock ngxBlockHttp = ngxConfig.findBlock("http");
			for (Http http : httpList) {
				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(http.getName() + " " + http.getValue());
				ngxBlockHttp.addEntry(ngxParam);
			}

			boolean hasHttp = false;
			// 添加upstream
			NgxParam ngxParam = null;
			List<Upstream> upstreams = upstreamService.getListByProxyType(0);

			for (Upstream upstream : upstreams) {
				NgxBlock ngxBlockServer = new NgxBlock();
				ngxBlockServer.addValue("upstream " + upstream.getName());

				if (StrUtil.isNotEmpty(upstream.getTactics())) {
					ngxParam = new NgxParam();
					ngxParam.addValue(upstream.getTactics());
					ngxBlockServer.addEntry(ngxParam);
				}

				List<UpstreamServer> upstreamServers = upstreamService.getUpstreamServers(upstream.getId());
				for (UpstreamServer upstreamServer : upstreamServers) {
					ngxParam = new NgxParam();
					ngxParam.addValue("server " + upstreamController.buildStr(upstreamServer, upstream.getProxyType()));
					ngxBlockServer.addEntry(ngxParam);
				}
				hasHttp = true;

				if (decompose) {
					addConfFile(confExt, "upstreams." + upstream.getName() + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + nginxPath.replace("nginx.conf", "conf.d/upstreams." + upstream.getName() + ".conf"));
					ngxBlockHttp.addEntry(ngxParam);

				} else {
					ngxBlockHttp.addEntry(ngxBlockServer);
				}

			}

			// 添加server
			List<Server> servers = serverService.getListByProxyType(0);
			for (Server server : servers) {
				NgxBlock ngxBlockServer = new NgxBlock();
				ngxBlockServer.addValue("server");

				// 监听域名
				if (StrUtil.isNotEmpty(server.getServerName())) {
					ngxParam = new NgxParam();
					ngxParam.addValue("server_name " + server.getServerName());
					ngxBlockServer.addEntry(ngxParam);
				}

				// 监听端口
				ngxParam = new NgxParam();
				String value = "listen " + server.getListen();
				if (server.getSsl() == 1) {
					value += " ssl";
				}
				ngxParam.addValue(value);
				ngxBlockServer.addEntry(ngxParam);

				// ssl配置
				if (server.getSsl() == 1) {
					ngxParam = new NgxParam();
					ngxParam.addValue("ssl_certificate " + server.getPem());
					ngxBlockServer.addEntry(ngxParam);

					ngxParam = new NgxParam();
					ngxParam.addValue("ssl_certificate_key " + server.getKey());
					ngxBlockServer.addEntry(ngxParam);

					ngxParam = new NgxParam();
					ngxParam.addValue("ssl_protocols TLSv1 TLSv1.1 TLSv1.2 TLSv1.3");
					ngxBlockServer.addEntry(ngxParam);

				}

				// 自定义参数
				List<Param> paramList = paramService.getListByTypeId(server.getId(), "server");
				for (Param param : paramList) {
					setSameParam(param, ngxBlockServer);
				}

				List<Location> locationList = serverService.getLocationByServerId(server.getId());

				// http转发配置
				for (Location location : locationList) {
					NgxBlock ngxBlockLocation = new NgxBlock();
					if (location.getType() == 0 || location.getType() == 2) { // http或负载均衡
						// 添加location
						ngxBlockLocation.addValue("location");
						ngxBlockLocation.addValue(location.getPath());

						if (location.getType() == 0) {
							ngxParam = new NgxParam();
							ngxParam.addValue("proxy_pass " + location.getValue());
							ngxBlockLocation.addEntry(ngxParam);
						} else if (location.getType() == 2) {
							Upstream upstream = sqlHelper.findById(location.getUpstreamId(), Upstream.class);
							if (upstream != null) {
								ngxParam = new NgxParam();
								ngxParam.addValue("proxy_pass http://" + upstream.getName());
								ngxBlockLocation.addEntry(ngxParam);
							}
						}

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header Host $host");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Real-IP $remote_addr");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for");
						ngxBlockLocation.addEntry(ngxParam);

						ngxParam = new NgxParam();
						ngxParam.addValue("proxy_set_header X-Forwarded-Proto $scheme");
						ngxBlockLocation.addEntry(ngxParam);

					} else if (location.getType() == 1) { // 静态html
						ngxBlockLocation.addValue("location");
						ngxBlockLocation.addValue(location.getPath());

						if (location.getPath().equals("/")) {
							ngxParam = new NgxParam();
							ngxParam.addValue("root " + location.getValue());
							ngxBlockLocation.addEntry(ngxParam);
						} else {
							ngxParam = new NgxParam();
							ngxParam.addValue("alias " + location.getValue());
							ngxBlockLocation.addEntry(ngxParam);
						}

						ngxParam = new NgxParam();
						ngxParam.addValue("index index.html");
						ngxBlockLocation.addEntry(ngxParam);
					}

					// 自定义参数
					paramList = paramService.getListByTypeId(location.getId(), "location");
					for (Param param : paramList) {
						setSameParam(param, ngxBlockLocation);
					}

					ngxBlockServer.addEntry(ngxBlockLocation);

				}
				hasHttp = true;

				// 是否需要分解
				if (decompose) {
					String name = "all";
					if (StrUtil.isNotEmpty(server.getServerName())) {
						name = server.getServerName();
					}

					addConfFile(confExt, name + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + nginxPath.replace("nginx.conf", "conf.d/" + name + ".conf"));
					ngxBlockHttp.addEntry(ngxParam);

				} else {
					ngxBlockHttp.addEntry(ngxBlockServer);
				}

				// https添加80端口重写
				if (server.getSsl() == 1 && server.getRewrite() == 1) {
					ngxBlockServer = new NgxBlock();
					ngxBlockServer.addValue("server");

					if (StrUtil.isNotEmpty(server.getServerName())) {
						ngxParam = new NgxParam();
						ngxParam.addValue("server_name " + server.getServerName());
						ngxBlockServer.addEntry(ngxParam);
					}
					ngxParam = new NgxParam();
					ngxParam.addValue("listen 80");
					ngxBlockServer.addEntry(ngxParam);

					ngxParam = new NgxParam();
					ngxParam.addValue("rewrite ^(.*)$ https://${server_name}$1 permanent");
					ngxBlockServer.addEntry(ngxParam);

					hasHttp = true;

					// 是否需要分解
					if (decompose) {
						addConfFile(confExt, server.getServerName() + ".conf", ngxBlockServer);

					} else {
						ngxBlockHttp.addEntry(ngxBlockServer);
					}
				}

			}
			if (!hasHttp) {
				ngxConfig.remove(ngxBlockHttp);
			}

			// TCP转发
			// 创建stream
			List<Stream> streamList = sqlHelper.findAll(Stream.class);
			boolean hasStream = false;
			NgxBlock ngxBlockStream = ngxConfig.findBlock("stream");
			for (Stream stream : streamList) {
				ngxParam = new NgxParam();
				ngxParam.addValue(stream.getName() + " " + stream.getValue());
				ngxBlockStream.addEntry(ngxParam);

				hasStream = true;
			}

			// 添加upstream
			upstreams = upstreamService.getListByProxyType(1);
			for (Upstream upstream : upstreams) {
				NgxBlock ngxBlockServer = new NgxBlock();
				ngxBlockServer.addValue("upstream " + upstream.getName());

				if (StrUtil.isNotEmpty(upstream.getTactics())) {
					ngxParam = new NgxParam();
					ngxParam.addValue(upstream.getTactics());
					ngxBlockServer.addEntry(ngxParam);
				}

				List<UpstreamServer> upstreamServers = upstreamService.getUpstreamServers(upstream.getId());
				for (UpstreamServer upstreamServer : upstreamServers) {
					ngxParam = new NgxParam();
					ngxParam.addValue("server " + upstreamController.buildStr(upstreamServer, upstream.getProxyType()));
					ngxBlockServer.addEntry(ngxParam);
				}

				if (decompose) {
					addConfFile(confExt, "upstreams." + upstream.getName() + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + nginxPath.replace("nginx.conf", "conf.d/upstreams." + upstream.getName() + ".conf"));
					ngxBlockStream.addEntry(ngxParam);
				} else {
					ngxBlockStream.addEntry(ngxBlockServer);
				}

				hasStream = true;
			}

			// 添加server
			servers = serverService.getListByProxyType(1);
			for (Server server : servers) {

				NgxBlock ngxBlockServer = new NgxBlock();
				ngxBlockServer.addValue("server");

				// 监听端口
				ngxParam = new NgxParam();
				ngxParam.addValue("listen " + server.getListen());
				ngxBlockServer.addEntry(ngxParam);

				// 指向负载均衡
				Upstream upstream = sqlHelper.findById(server.getProxyUpstreamId(), Upstream.class);
				if (upstream != null) {
					ngxParam = new NgxParam();
					ngxParam.addValue("proxy_pass " + upstream.getName());
					ngxBlockServer.addEntry(ngxParam);
				}

				// 其他一些参数
				ngxParam = new NgxParam();
				ngxParam.addValue("proxy_connect_timeout 1s");
				ngxBlockServer.addEntry(ngxParam);

				ngxParam = new NgxParam();
				ngxParam.addValue("proxy_timeout 3s");
				ngxBlockServer.addEntry(ngxParam);

				// 自定义参数
				List<Param> paramList = paramService.getListByTypeId(server.getId(), "server");
				for (Param param : paramList) {
					setSameParam(param, ngxBlockServer);
				}

				if (decompose) {
					addConfFile(confExt, "stream." + server.getListen() + ".conf", ngxBlockServer);

					ngxParam = new NgxParam();
					ngxParam.addValue("include " + nginxPath.replace("nginx.conf", "conf.d/stream." + server.getListen() + ".conf"));
					ngxBlockStream.addEntry(ngxParam);
				} else {
					ngxBlockStream.addEntry(ngxBlockServer);
				}

				hasStream = true;
			}

			if (!hasStream) {
				ngxConfig.remove(ngxBlockStream);
			}

			String conf = new NgxDumper(ngxConfig).dump();

			// 装载ngx_stream_module模块
			if (hasStream && SystemTool.isLinux()) {
				String module = settingService.get("ngx_stream_module");
				if (StrUtil.isEmpty(module)) {
					module = RuntimeTool.execForOne("find / -name ngx_stream_module.so").trim();
				}

				if (StrUtil.isNotEmpty(module)) {
					settingService.set("ngx_stream_module", module);
					conf = "load_module " + module + ";\n" + conf;
				}
			}

			confExt.setConf(conf);

			return confExt;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void setSameParam(Param param, NgxBlock ngxBlock) {
		for (NgxEntry ngxEntry : ngxBlock.getEntries()) {
			NgxParam ngxParam = (NgxParam) ngxEntry;
			if (ngxParam.toString().startsWith(param.getName())) {
				ngxBlock.remove(ngxParam);
				break;
			}
		}

		NgxParam ngxParam = new NgxParam();
		ngxParam.addValue(param.getName() + " " + param.getValue());
		ngxBlock.addEntry(ngxParam);

	}

	private void addConfFile(ConfExt confExt, String name, NgxBlock ngxBlockServer) {
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
	}

	private String buildStr(NgxBlock ngxBlockServer) {

		NgxConfig ngxConfig = new NgxConfig();
		ngxConfig.addEntry(ngxBlockServer);

		return new NgxDumper(ngxConfig).dump();
	}

	public void replace(String nginxPath, String nginxContent, String[] subContent, String[] subName) {
		String date = DateUtil.format(new Date(), "yyyy-MM-dd_HH-mm-ss");
		// 备份主文件
		FileUtil.copy(nginxPath, nginxPath + date + ".bak", true);
		// 备份conf.d文件夹
		String confd = nginxPath.replace("nginx.conf", "conf.d/");
		if (!FileUtil.exist(confd)) {
			FileUtil.mkdir(confd);
		}
		ZipUtil.zip(confd, nginxPath + date + ".zip");

		// 写入主文件
		FileUtil.writeString(nginxContent, nginxPath, Charset.defaultCharset());
		String decompose = settingService.get("decompose");

		if ("true".equals(decompose)) {
			// 写入conf.d文件
			if (subContent != null) {
				for (int i = 0; i < subContent.length; i++) {
					String tagert = nginxPath.replace("nginx.conf", "conf.d/" + subName[i]);
					FileUtil.writeString(subContent[i], tagert, Charset.defaultCharset()); // 清空
				}
			}
		} else {
			// 删除conf.d下全部文件
			FileUtil.del(confd);
			FileUtil.mkdir(confd);
		}

	}

	public AsycPack getAsycPack() {
		AsycPack asycPack = new AsycPack();
		List<Cert> certList = sqlHelper.findAll(Cert.class);
		for (Cert cert : certList) {
			if (StrUtil.isNotEmpty(cert.getPem())) {
				cert.setPemStr(FileUtil.readString(cert.getPem(), Charset.defaultCharset()));
				cert.setKeyStr(FileUtil.readString(cert.getKey(), Charset.defaultCharset()));
			}
		}

		asycPack.setCertList(certList);
		asycPack.setHttpList(sqlHelper.findAll(Http.class));
		List<Server> serverList = sqlHelper.findAll(Server.class);
		for (Server server : serverList) {
			if (StrUtil.isNotEmpty(server.getPem())) {
				server.setPemStr(FileUtil.readString(server.getPem(), Charset.defaultCharset()));
				server.setKeyStr(FileUtil.readString(server.getKey(), Charset.defaultCharset()));
			}
		}
		asycPack.setServerList(serverList);

		asycPack.setLocationList(sqlHelper.findAll(Location.class));
		asycPack.setUpstreamList(sqlHelper.findAll(Upstream.class));
		asycPack.setUpstreamServerList(sqlHelper.findAll(UpstreamServer.class));
		asycPack.setStreamList(sqlHelper.findAll(Stream.class));

		asycPack.setParamList(sqlHelper.findAll(Param.class));

		String nginxPath = settingService.get("nginxPath");
		String decompose = settingService.get("decompose");

		ConfExt confExt = buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"));

		if (FileUtil.exist(nginxPath)) {
			String orgStr = FileUtil.readString(nginxPath, Charset.defaultCharset());
			confExt.setConf(orgStr);

			for (ConfFile confFile : confExt.getFileList()) {
				confFile.setConf("");

				String filePath = nginxPath.replace("nginx.conf", "conf.d/" + confFile.getName());
				if (FileUtil.exist(filePath)) {
					confFile.setConf(FileUtil.readString(filePath, Charset.defaultCharset()));
				}
			}
		}

		asycPack.setDecompose(decompose);
		asycPack.setConfExt(confExt);
		return asycPack;
	}

	@Transactional
	public void setAsycPack(AsycPack asycPack) {

		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), Cert.class);
		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), Http.class);
		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), Server.class);
		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), Location.class);
		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), Upstream.class);
		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), UpstreamServer.class);
		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), Stream.class);
		sqlHelper.deleteByQuery(new CriteriaAndWrapper(), Param.class);

		sqlHelper.insertAll(asycPack.getCertList());
		sqlHelper.insertAll(asycPack.getHttpList());
		sqlHelper.insertAll(asycPack.getServerList());
		sqlHelper.insertAll(asycPack.getLocationList());
		sqlHelper.insertAll(asycPack.getUpstreamList());
		sqlHelper.insertAll(asycPack.getUpstreamServerList());
		sqlHelper.insertAll(asycPack.getStreamList());
		sqlHelper.insertAll(asycPack.getParamList());

		for (Cert cert : asycPack.getCertList()) {
			if (StrUtil.isNotEmpty(cert.getPem())) {
				FileUtil.writeString(cert.getPemStr(), cert.getPem(), Charset.defaultCharset());
				FileUtil.writeString(cert.getKeyStr(), cert.getKey(), Charset.defaultCharset());
			}
		}

		for (Server server : asycPack.getServerList()) {
			if (StrUtil.isNotEmpty(server.getPem())) {
				FileUtil.writeString(server.getPemStr(), server.getPem(), Charset.defaultCharset());
				FileUtil.writeString(server.getKeyStr(), server.getKey(), Charset.defaultCharset());
			}
		}

		settingService.set("decompose", asycPack.getDecompose());

		ConfExt confExt = asycPack.getConfExt();

		String nginxPath = settingService.get("nginxPath");

		if (FileUtil.exist(nginxPath)) {

			List<String> subContent = new ArrayList<String>();
			List<String> subName = new ArrayList<String>();

			for (ConfFile confFile : confExt.getFileList()) {
				subContent.add(confFile.getConf());
				subName.add(confFile.getName());
			}

			replace(nginxPath, confExt.getConf(), subContent.toArray(new String[0]), subName.toArray(new String[0]));
		}
	}

}
