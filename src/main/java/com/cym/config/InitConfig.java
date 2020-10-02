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

import com.cym.model.Basic;
import com.cym.model.Http;
import com.cym.model.Stream;
import com.cym.service.BasicService;
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
	BasicService basicService;

	@Autowired
	SqlHelper sqlHelper;

	@Value("${project.home}")
	public void setHome(String home) {
		InitConfig.home = home;
	}

	@PostConstruct
	public void init() throws IOException {
		// 初始化base值
		Long count = sqlHelper.findAllCount(Basic.class);
		if (count == 0) {
			List<Basic> basics = new ArrayList<Basic>();
			basics.add(new Basic("worker_processes", "auto", 1l));
			basics.add(new Basic("events", "{\r\n" + "    worker_connections  1024;\r\n" + "}", 2l));
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
		if(!FileUtil.exist(home + "nginx.conf")) {
			ClassPathResource resource = new ClassPathResource("nginx.conf");
			FileUtil.writeFromStream(resource.getInputStream(), home + "nginx.conf");
		}
		if(!FileUtil.exist(home + "mime.types")) {
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
			// 初始化acme.sh
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
				}
				
				// 赋予执行权限
				RuntimeUtil.exec("chmod 777 " + acmeSh);
			}

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
				String nginxExe = "nginx";
				// 设置nginx执行文件
				settingService.set("nginxExe", nginxExe);
				// 启动nginx
				RuntimeUtil.exec(nginxExe, "-c", nginxPath);
			}
		}

		// 初始化http和stream的seq值
		List<Http> https = sqlHelper.findAll(Http.class);
		List<Stream> streams = sqlHelper.findAll(Stream.class);

		for (Http http : https) {
			if (http.getSeq() == null) {
				http.setSeq(Long.parseLong(http.getId()));
				sqlHelper.updateById(http);
			}
		}
		for (Stream stream : streams) {
			if (stream.getSeq() == null) {
				stream.setSeq(Long.parseLong(stream.getId()));
				sqlHelper.updateById(stream);
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
			if (str.startsWith("1:name") && str.contains("docker")) {
				return true;
			}
		}
		return false;
	}

}
