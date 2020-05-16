package com.cym.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.cym.service.SettingService;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.system.SystemUtil;

@Component
public class CertConfig {

	public String acmeSh;
	@Autowired
	SettingService settingService;

	@PostConstruct
	public void init() throws IOException {
		if (!SystemUtil.get(SystemUtil.OS_NAME).toLowerCase().contains("win")) {
			// 初始化acme.sh
			String userDir = FileUtil.getUserHomePath();

			ClassPathResource resource = new ClassPathResource("acme.zip");
			InputStream inputStream = resource.getInputStream();

			FileUtil.writeFromStream(inputStream, userDir + File.separator + "acme.zip");
			FileUtil.mkdir(userDir + File.separator + ".acme.sh");
			ZipUtil.unzip(userDir + File.separator + "acme.zip", userDir + File.separator + ".acme.sh");
			FileUtil.del(userDir + File.separator + "acme.zip");

			acmeSh = userDir + File.separator + ".acme.sh" + File.separator + "acme.sh";

			RuntimeUtil.execForStr("chmod 777 " + acmeSh);
			System.err.println(acmeSh);
		}

		// 初始化nginx配置文件
		String nginxPath = settingService.get("nginxPath");
		if (StrUtil.isEmpty(nginxPath)) {
			nginxPath = "/etc/nginx/nginx.conf";
			settingService.set("nginxPath", nginxPath);
		}
	}

}
