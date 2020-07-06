package com.cym.config;

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
import com.cym.utils.SystemTool;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
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

			// 查找ngx_stream_module模块
			logger.info("----------------find ngx_stream_module--------------");
			String module = settingService.get("ngx_stream_module");
			if (StrUtil.isEmpty(module)) {
				List<String> list = RuntimeUtil.execForLines(CharsetUtil.systemCharset(), "find / -name ngx_stream_module.so");

				for (String path : list) {
					if (path.contains("ngx_stream_module.so") && path.length() < 80) {
						settingService.set("ngx_stream_module", path);
					}
				}
			}

			// 找寻nginx配置文件
			logger.info("----------------find nginx.conf--------------");
			String nginxPath = settingService.get("nginxPath");
			
			if (StrUtil.isEmpty(nginxPath)) {
				try {
					// 判断是否是容器中 
					if (FileUtil.exist("/etc/nginx/nginx.conf") && FileUtil.readUtf8String("/etc/nginx/nginx.conf").contains("include " + home + "nginx.conf")) {
						logger.info("----------------release nginx.conf--------------");
						// 释放nginxOrg.conf
						nginxPath = home + "nginx.conf";
						ClassPathResource resource = new ClassPathResource("nginxOrg.conf");
						InputStream inputStream = resource.getInputStream();
						FileUtil.writeFromStream(inputStream, nginxPath);
						settingService.set("nginxPath", nginxPath);
						
						// 设置nginx执行文件
						settingService.set("nginxExe", "nginx");
						
						// 启动nginx
						RuntimeUtil.exec("nginx");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}


		}
	}
}
