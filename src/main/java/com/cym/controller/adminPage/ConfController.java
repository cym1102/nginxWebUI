package com.cym.controller.adminPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Http;
import com.cym.model.Location;
import com.cym.model.Server;
import com.cym.model.Stream;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;
import com.cym.service.ServerService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.github.odiszapc.nginxparser.NgxBlock;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxDumper;
import com.github.odiszapc.nginxparser.NgxParam;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;

@Controller
@RequestMapping("/adminPage/conf")
public class ConfController extends BaseController {
	@Autowired
	UpstreamController upstreamController;
	@Autowired
	UpstreamService upstreamService;
	@Autowired
	SettingService settingService;
	@Autowired
	ServerService serverService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) throws IOException, SQLException {

		String confStr = buildConf();
		modelAndView.addObject("confStr", confStr);

		String nginxPath = settingService.get("nginxPath");
		modelAndView.addObject("nginxPath", nginxPath);

		modelAndView.setViewName("/adminPage/conf/index");
		return modelAndView;
	}

	private String buildConf() {
		try {
			ClassPathResource resource = new ClassPathResource("nginx.conf");
			InputStream inputStream = resource.getInputStream();

			NgxConfig ngxConfig = NgxConfig.read(inputStream);

			// 创建http
			List<Http> httpList = sqlHelper.findAll(Http.class);
			NgxBlock ngxBlockHttp = ngxConfig.findBlock("http");
			for (Http http : httpList) {
				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(http.getName() + " " + http.getValue());
				ngxBlockHttp.addEntry(ngxParam);
			}

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

				ngxBlockHttp.addEntry(ngxBlockServer);
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

				List<Location> locationList = serverService.getLocationByServerId(server.getId());

				// http转发配置
				for (Location location : locationList) {
					if (location.getType() == 0 || location.getType() == 2) { // http或负载均衡
						// 添加location
						NgxBlock ngxBlockLocation = new NgxBlock();
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

						ngxBlockServer.addEntry(ngxBlockLocation);

					} else if (location.getType() == 1) { // 静态html
						NgxBlock ngxBlockLocation = new NgxBlock();
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

						ngxBlockServer.addEntry(ngxBlockLocation);
					}
				}
				ngxBlockHttp.addEntry(ngxBlockServer);

				// https添加80端口重写
				if (server.getSsl() == 1 && server.getRewrite() == 1) {
					ngxBlockServer = new NgxBlock();
					ngxBlockServer.addValue("server");

					ngxParam = new NgxParam();
					ngxParam.addValue("server_name " + server.getServerName());
					ngxBlockServer.addEntry(ngxParam);

					ngxParam = new NgxParam();
					ngxParam.addValue("listen 80");
					ngxBlockServer.addEntry(ngxParam);

					ngxParam = new NgxParam();
					ngxParam.addValue("rewrite ^(.*)$ https://${server_name}$1 permanent");
					ngxBlockServer.addEntry(ngxParam);

					ngxBlockHttp.addEntry(ngxBlockServer);
				}

			}

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

				ngxBlockStream.addEntry(ngxBlockServer);
				
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
				
				ngxBlockStream.addEntry(ngxBlockServer);
				
				hasStream = true;
			}
			
			if(!hasStream) {
				ngxConfig.remove(ngxBlockStream);
			}

			return new NgxDumper(ngxConfig).dump();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@RequestMapping(value = "replace")
	@ResponseBody
	public JsonResult replace(String nginxPath, String nginxContent)  {
		settingService.set("nginxPath", nginxPath);

		try {
			// 备份文件
			FileUtil.copy(nginxPath, nginxPath + DateUtil.format(new Date(), "yyyy-MM-dd_HH-mm-ss") + ".bak", true);
			FileUtil.writeString(nginxContent, nginxPath, Charset.defaultCharset());
			return renderSuccess("替换成功，原文件已备份");
		} catch (Exception e) {
			e.printStackTrace();

			return renderError("替换失败:" + e.getMessage());
		}

	}

	@RequestMapping(value = "check")
	@ResponseBody
	public JsonResult check(String nginxPath)  {
		settingService.set("nginxPath", nginxPath);

		try {
			String rs = null;
			if (SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("win")) {
				File file = new File(nginxPath);
				if (file.exists() && file.getParentFile().getParentFile().exists()) {
					File nginxDir = file.getParentFile().getParentFile();
					rs = RuntimeUtil.execForStr("cmd /c powershell cd " + nginxDir.getPath() + "; ./nginx.exe -t;");
				} else {
					return renderError("nginx目录不存在");
				}
			} else {
				rs = RuntimeUtil.execForStr("nginx -t");
			}

			if (rs.contains("successful")) {
				return renderSuccess("效验成功");
			} else {
				return renderError("效验失败:<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("效验失败:<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "reboot")
	@ResponseBody
	public JsonResult reboot(String nginxPath)  {
		settingService.set("nginxPath", nginxPath);
		try {
			String rs = null;
			if (SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("win")) {
				File file = new File(nginxPath);
				if (file.exists() && file.getParentFile().getParentFile().exists()) {
					File nginxDir = file.getParentFile().getParentFile();
					rs = RuntimeUtil.execForStr("cmd /c powershell cd " + nginxDir.getPath() + "; ./nginx.exe -s reload;");
				} else {
					return renderError("nginx目录不存在");
				}
			} else {
				rs = RuntimeUtil.execForStr("nginx -s reload");
			}

			if (rs.trim().equals("")) {
				return renderSuccess("重启成功");
			} else {
				return renderError("重启失败:<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("重启失败:<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "loadOrg")
	@ResponseBody
	public JsonResult loadOrg(String nginxPath)  {
		settingService.set("nginxPath", nginxPath);

		if (FileUtil.exist(nginxPath)) {
			String orgStr = FileUtil.readString(nginxPath, Charset.defaultCharset());
			return renderSuccess(orgStr);
		} else {
			return renderError("文件不存在");
		}

	}

}
