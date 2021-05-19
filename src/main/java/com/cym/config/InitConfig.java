package com.cym.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.cym.model.Basic;
import com.cym.model.Http;
import com.cym.service.BasicService;
import com.cym.service.SettingService;
import com.cym.utils.NginxUtils;
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

	public static String acmeSh;
	public static String acmeShDir;
	public static String home;

	@Autowired
	SettingService settingService;
	@Autowired
	BasicService basicService;
	@Autowired
	ScheduleTask scheduleTask;
	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Value("${project.home}")
	public void setHome(String home) {
		InitConfig.home = home;
		InitConfig.acmeShDir = home + ".acme.sh/";
		InitConfig.acmeSh = home + ".acme.sh/acme.sh";
	}

	@PostConstruct
	public void init() throws IOException {
		// 初始化base值
		Long count = sqlHelper.findAllCount(Basic.class);
		if (count == 0) {
			List<Basic> basics = new ArrayList<Basic>();
			basics.add(new Basic("worker_processes", "auto", 1l));
			basics.add(new Basic("events", "{\r\n" + "    worker_connections  1024;\r\n    accept_mutex on;\r\n" + "}", 2l));
			sqlHelper.insertAll(basics);
		}

		// 初始化http值
		count = sqlHelper.findAllCount(Http.class);
		if (count == 0) {
			List<Http> https = new ArrayList<Http>();
			https.add(new Http("include", "mime.types", 0l));
			https.add(new Http("default_type", "application/octet-stream", 1l));
			sqlHelper.insertAll(https);
		}

		// 释放nginx.conf,mime.types
		if (!FileUtil.exist(home + "nginx.conf")) {
			ClassPathResource resource = new ClassPathResource("nginx.conf");
			FileUtil.writeFromStream(resource.getInputStream(), home + "nginx.conf");
		}
		if (!FileUtil.exist(home + "mime.types")) {
			ClassPathResource resource = new ClassPathResource("mime.types");
			FileUtil.writeFromStream(resource.getInputStream(), home + "mime.types");
		}

		// 设置nginx配置文件
		String nginxPath = settingService.get("nginxPath");
		if (StrUtil.isEmpty(nginxPath)) {
			nginxPath = home + "nginx.conf";
			// 设置nginx.conf路径
			settingService.set("nginxPath", nginxPath);
		}

		if (SystemTool.isLinux()) {
			// 释放全新包
			ClassPathResource resource = new ClassPathResource("acme.zip");
			InputStream inputStream = resource.getInputStream();
			FileUtil.writeFromStream(inputStream, InitConfig.home + "acme.zip");
			FileUtil.mkdir(InitConfig.acmeShDir);
			ZipUtil.unzip(InitConfig.home + "acme.zip", InitConfig.acmeShDir);
			FileUtil.del(InitConfig.home + "acme.zip");

			// 释放新的.acme.sh文件
			resource = new ClassPathResource("acme.sh");
			List<String> res = new ArrayList<>();
			BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
			String str = "";
			while ((str = br.readLine()) != null) {
				if (str.contains("${acmeShDir}")) {
					str = str.replace("${acmeShDir}", InitConfig.acmeShDir);
				}
				res.add(str);
			}

			FileUtil.writeLines(res, InitConfig.acmeSh, "UTF-8");
			RuntimeUtil.exec("chmod a+x " + acmeSh);

			// 查找ngx_stream_module模块
			if (!basicService.contain("ngx_stream_module.so")) {
				List<String> list = RuntimeUtil.execForLines(CharsetUtil.systemCharset(), "find / -name ngx_stream_module.so");
				for (String path : list) {
					if (path.contains("ngx_stream_module.so") && path.length() < 80) {
						Basic basic = new Basic("load_module", path, -10l);
						sqlHelper.insert(basic);
						break;
					}
				}
			}

			// 判断是否是容器中
			if (inDocker()) {
				if (!NginxUtils.isRun()) {

					String nginxExe = "nginx";
					// 设置nginx执行文件
					settingService.set("nginxExe", nginxExe);
					// 启动nginx
					String cmd = nginxExe + " -c " + nginxPath;
					RuntimeUtil.execForStr("/bin/sh", "-c", cmd);
				} 
			}
		}

	}

	/**
	 * 是否在docker中
	 * 
	 * @return
	 */
	private Boolean inDocker() {
		List<String> rs = RuntimeUtil.execForLines("cat /proc/1/cgroup");
		for (String str : rs) {
			logger.info(str);
			if (str.contains("docker")) {
				logger.info("I am in docker");
				return true;
			}
			if (str.contains("kubepods")) {
				logger.info("I am in k8s");
				return true;
			}
		}
		logger.info("I am not in docker");
		return false;
	}

}
