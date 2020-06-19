package com.cym.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.cym.model.Http;
import com.cym.service.SettingService;
import com.cym.utils.RuntimeTool;
import com.cym.utils.SystemTool;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Component
public class InitConfig {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static String acmeSh = "/root/.acme.sh/acme.sh";
	public static String home;

	@Autowired
	SettingService settingService;

	@Autowired
	SqlHelper sqlHelper;

	@Value("${project.home}")
	public void setHome(String home) {
		InitConfig.home = home;
	}

	@PostConstruct
	public void init() throws IOException {

		Long count = sqlHelper.findAllCount(Http.class);
		if (count == 0) {
			List<Http> https = new ArrayList<Http>();
			https.add(new Http("include", "mime.types"));
			https.add(new Http("default_type", "application/octet-stream"));

			sqlHelper.insertAll(https);
		}

		if (SystemTool.isLinux()) {
			// 初始化acme.sh
			logger.info("----------------release acme.sh--------------");
			if (!FileUtil.exist("/root/.acme.sh")) {

				// 查看是否存在/home/nginxWebUI/.acme.sh
				if (FileUtil.exist(home + ".acme.sh")) {
					// 有,直接复制过来
					FileUtil.copy(home + ".acme.sh", "/root/", true);
				} else {
					// 没有,释放全新包
					ClassPathResource resource = new ClassPathResource("acme.zip");
					InputStream inputStream = resource.getInputStream();

					FileUtil.writeFromStream(inputStream, "/root/acme.zip");
					FileUtil.mkdir("/root/.acme.sh");
					ZipUtil.unzip("/root/acme.zip", "/root/.acme.sh");
					FileUtil.del("/root/acme.zip");

					RuntimeUtil.exec("chmod 777 " + acmeSh);
				}

			}

			// 找寻nginx配置文件
			logger.info("----------------find nginx.conf--------------");
			String nginxPath = settingService.get("nginxPath");
			if (StrUtil.isEmpty(nginxPath)) {
				// 查找nginx.conf
				nginxPath = RuntimeTool.execForOne("find / -name nginx.conf");
				if (StrUtil.isNotEmpty(nginxPath)) {
					// 判断是否是容器中
					String lines = FileUtil.readUtf8String(nginxPath);
					if (StrUtil.isNotEmpty(lines) && lines.contains("include " + home)) {
						nginxPath = home + "nginx.conf";

						logger.info("----------------release nginx.conf--------------");
						// 释放nginxOrg.conf
						ClassPathResource resource = new ClassPathResource("nginxOrg.conf");
						InputStream inputStream = resource.getInputStream();
						FileUtil.writeFromStream(inputStream, nginxPath);
					}

					settingService.set("nginxPath", nginxPath);
				}
			}

			// 查找nginx执行文件
			logger.info("----------------find nginx--------------");
			String nginxExe = settingService.get("nginxExe");
			if (StrUtil.isEmpty(nginxExe)) {
				String rs = RuntimeTool.execForOne("which nginx");
				if (StrUtil.isNotEmpty(rs)) {
					nginxExe = "nginx";
					settingService.set("nginxExe", nginxExe);
				}
			}

			// 查找ngx_stream_module模块
			logger.info("----------------find ngx_stream_module--------------");
			String module = settingService.get("ngx_stream_module");
			if (StrUtil.isEmpty(module)) {
				module = RuntimeTool.execForOne("find / -name ngx_stream_module.so");
				if (StrUtil.isNotEmpty(module)) {
					settingService.set("ngx_stream_module", module);
				}
			}

			// 尝试启动nginx
			logger.info("----------------start nginx--------------");
			if (nginxExe.equals("nginx")) {
				String[] command = { "/bin/sh", "-c", "ps -ef|grep nginx" };
				String rs = RuntimeUtil.execForStr(command);
				if (!rs.contains("nginx: master process") && SystemTool.hasNginx()) {
					try {
						RuntimeUtil.exec("nginx");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// 查找nginx.pid文件
			logger.info("----------------find nginx.pid--------------");
			String nginxPid = settingService.get("nginxPid");
			if (StrUtil.isEmpty(nginxPid)) {
				nginxPid = RuntimeTool.execForOne("find / -name nginx.pid");
				if (StrUtil.isNotEmpty(nginxPid)) {
					settingService.set("nginxPid", nginxPid);
				}
			}
		}
	}
}
