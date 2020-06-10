package com.cym.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

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

			if (!FileUtil.exist("/root/.acme.sh")) {
				ClassPathResource resource = new ClassPathResource("acme.zip");
				InputStream inputStream = resource.getInputStream();

				FileUtil.writeFromStream(inputStream, "/root/acme.zip");
				FileUtil.mkdir("/root/.acme.sh");
				ZipUtil.unzip("/root/acme.zip", "/root/.acme.sh");
				FileUtil.del("/root/acme.zip");

				RuntimeUtil.exec("chmod 777 " + acmeSh);
			}

			// 找寻nginx配置文件
			String nginxPath = settingService.get("nginxPath");
			if (StrUtil.isEmpty(nginxPath)) {
				// 查找nginx.conf
				nginxPath = RuntimeTool.execForOne("find / -name nginx.conf");
				if (StrUtil.isNotEmpty(nginxPath) && FileUtil.exist(nginxPath)) {
					// 判断是否是容器中
					String lines = FileUtil.readUtf8String(nginxPath);
					if (StrUtil.isNotEmpty(lines) && lines.contains("include " + home + "nginx.conf;")) {
						nginxPath = home + "nginx.conf";

						// 释放nginxOrg.conf
						ClassPathResource resource = new ClassPathResource("nginxOrg.conf");
						InputStream inputStream = resource.getInputStream();
						FileUtil.writeFromStream(inputStream, nginxPath);
					}

					settingService.set("nginxPath", nginxPath);
				}
			}

			// 查找nginx执行文件
			String nginxExe = settingService.get("nginxExe");
			if (StrUtil.isEmpty(nginxExe)) {
				String rs = RuntimeTool.execForOne("which nginx");
				if (StrUtil.isNotEmpty(rs)) {
					nginxExe = "nginx";
					settingService.set("nginxExe", nginxExe);
				}
			}

			// 尝试启动nginx
			if (nginxExe.equals("nginx")) {
				String[] command = { "/bin/sh", "-c", "ps -ef|grep nginx" };
				String rs = RuntimeUtil.execForStr(command);
				if (!rs.contains("nginx: master process") && SystemTool.hasNginx()) {
					System.err.println("尝试启动nginx");
					try {
						RuntimeUtil.exec("nginx");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// 查找nginx.pid文件
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
