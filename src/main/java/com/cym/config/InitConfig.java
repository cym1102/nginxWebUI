package com.cym.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.Admin;
import com.cym.model.Basic;
import com.cym.model.Cert;
import com.cym.model.Http;
import com.cym.model.Password;
import com.cym.service.BasicService;
import com.cym.service.ConfService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.JdbcTemplate;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.EncodePassUtils;
import com.cym.utils.MessageUtils;
import com.cym.utils.NginxUtils;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.crypto.SecureUtil;

@Component
public class InitConfig {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	MessageUtils m;

	@Inject
	HomeConfig homeConfig;

	@Inject
	VersionConfig versionConfig;

	@Inject
	SettingService settingService;
	@Inject
	BasicService basicService;
	@Inject
	SqlHelper sqlHelper;
	@Inject
	JdbcTemplate jdbcTemplate;
	@Inject
	ConfService confService;

	@Inject("${project.findPass}")
	Boolean findPass;

	@Init
	public void init() throws IOException {

		// 找回密码
		if (findPass) {
			List<Admin> admins = sqlHelper.findAll(Admin.class);
			for (Admin admin : admins) {
				System.out.println(m.get("adminStr.name") + ":" + admin.getName() + " " + m.get("adminStr.pass") + ":" + EncodePassUtils.defaultPass);
				admin.setAuth(false); // 关闭二次验证
				admin.setPass(EncodePassUtils.encodeDefaultPass());
				sqlHelper.updateById(admin);
			}
			System.exit(1);
		}

		// 初始化base值
		Long count = sqlHelper.findAllCount(Basic.class);
		if (count == 0) {
			List<Basic> basics = new ArrayList<Basic>();
			basics.add(new Basic("worker_processes", "auto", 1l));
			basics.add(new Basic("events", "{\r\n    worker_connections  1024;\r\n    accept_mutex on;\r\n" + "}", 2l));
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
		if (!FileUtil.exist(homeConfig.home + "nginx.conf")) {
			ClassPathResource resource = new ClassPathResource("nginx.conf");
			FileUtil.writeFromStream(resource.getStream(), homeConfig.home + "nginx.conf");
		}
		if (!FileUtil.exist(homeConfig.home + "mime.types")) {
			ClassPathResource resource = new ClassPathResource("mime.types");
			FileUtil.writeFromStream(resource.getStream(), homeConfig.home + "mime.types");
		}

		// 设置nginx配置文件
		String nginxPath = settingService.get("nginxPath");
		if (StrUtil.isEmpty(nginxPath)) {
			nginxPath = homeConfig.home + "nginx.conf";
			// 设置nginx.conf路径
			settingService.set("nginxPath", nginxPath);
		}

		// 释放acme全新包
		ClassPathResource resource = new ClassPathResource("acme.zip");
		InputStream inputStream = resource.getStream();
		FileUtil.writeFromStream(inputStream, homeConfig.home + "acme.zip");
		FileUtil.mkdir(homeConfig.acmeShDir);
		ZipUtil.unzip(homeConfig.home + "acme.zip", homeConfig.acmeShDir);
		FileUtil.del(homeConfig.home + "acme.zip");

		// 修改acme.sh文件
		List<String> res = FileUtil.readUtf8Lines(homeConfig.acmeSh);
		for (int i = 0; i < res.size(); i++) {
			if (res.get(i).contains("DEFAULT_INSTALL_HOME=\"$HOME/.$PROJECT_NAME\"")) {
				res.set(i, "DEFAULT_INSTALL_HOME=\"" + homeConfig.acmeShDir + "\"");
			}
		}
		FileUtil.writeUtf8Lines(res, homeConfig.acmeSh);

		if (SystemTool.isLinux()) {
			RuntimeUtil.exec("chmod a+x " + homeConfig.acmeSh);

			// 查找ngx_stream_module模块
			if (!basicService.contain("ngx_stream_module.so")) {
				if (FileUtil.exist("/usr/lib/nginx/modules/ngx_stream_module.so")) {
					Basic basic = new Basic("load_module", "/usr/lib/nginx/modules/ngx_stream_module.so", -10l);
					sqlHelper.insert(basic);
				} else {
					logger.info(m.get("commonStr.ngxStream"));
					List<String> list = RuntimeUtil.execForLines(CharsetUtil.systemCharset(), "find / -name ngx_stream_module.so");
					for (String path : list) {
						if (path.contains("ngx_stream_module.so") && path.length() < 80) {
							Basic basic = new Basic("load_module", path, -10l);
							sqlHelper.insert(basic);
							break;
						}
					}
				}
			}

			// 判断是否存在nginx命令
			if (hasNginx()) {
				// 设置nginx执行文件
				settingService.set("nginxExe", "nginx");
			}

			// 尝试启动nginx
			String nginxExe = settingService.get("nginxExe");
			String nginxDir = settingService.get("nginxDir");

			logger.info("nginxIsRun:" + NginxUtils.isRun());
			if (!NginxUtils.isRun() && StrUtil.isNotEmpty(nginxExe) && StrUtil.isNotEmpty(nginxPath)) {
				String cmd = nginxExe + " -c " + nginxPath;

				if (StrUtil.isNotEmpty(nginxDir)) {
					cmd += " -p " + nginxDir;
				}
				logger.info("runCmd:" + cmd);
				RuntimeUtil.execForStr("/bin/sh", "-c", cmd);
			}
		}

		// 将复制的证书文件还原到acme文件夹里面
		List<Cert> certs = confService.getApplyCerts();
		for (Cert cert : certs) {
			boolean update = false;
			if (cert.getPem() != null && cert.getPem().equals(homeConfig.home + "cert/" + cert.getDomain() + ".fullchain.cer")) {
				cert.setPem(homeConfig.acmeShDir + cert.getDomain() + "/fullchain.cer");
				update = true;
			}
			if (cert.getKey() != null && cert.getKey().equals(homeConfig.home + "cert/" + cert.getDomain() + ".key")) {
				cert.setKey(homeConfig.acmeShDir + cert.getDomain() + "/" + cert.getDomain() + ".key");
				update = true;
			}

			if (update) {
				sqlHelper.updateById(cert);
			}
		}

		// 证书加密方式RAS改为RSA
		certs = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(Cert::getEncryption, "RAS"), Cert.class);
		for (Cert cert : certs) {
			cert.setEncryption("RSA");
			sqlHelper.updateById(cert);
		}

		// 将密码加密
		List<Admin> admins = sqlHelper.findAll(Admin.class);
		for (Admin admin : admins) {
			if (!StrUtil.endWith(admin.getPass(), SecureUtil.md5(EncodePassUtils.defaultPass))) {
				admin.setPass(EncodePassUtils.encode(admin.getPass()));
				sqlHelper.updateById(admin);
			}
		}

		// 展示logo
		showLogo();
	}

	private boolean hasNginx() {
		String rs = RuntimeUtil.execForStr("which nginx");
		if (StrUtil.isNotEmpty(rs)) {
			return true;
		}

		return false;
	}

	private void showLogo() throws IOException {
		ClassPathResource resource = new ClassPathResource("banner.txt");
		BufferedReader reader = resource.getReader(Charset.forName("utf-8"));
		String str = null;
		StringBuilder stringBuilder = new StringBuilder();
		// 使用readLine() 比较方便的读取一行
		while (null != (str = reader.readLine())) {
			stringBuilder.append(str + "\n");
		}
		reader.close();// 关闭流

		stringBuilder.append("nginxWebUI " + versionConfig.currentVersion + "\n");

		logger.info(stringBuilder.toString());

	}

}
