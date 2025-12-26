package com.cym.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.Admin;
import com.cym.model.Basic;
import com.cym.model.Http;
import com.cym.service.BasicService;
import com.cym.service.ConfService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.config.DataSourceEmbed;
import com.cym.sqlhelper.config.Table;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.JdbcTemplate;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.EncodePassUtils;
import com.cym.utils.MessageUtils;
import com.cym.utils.SystemTool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

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
	@Inject
	DataSourceEmbed dataSourceEmbed;
	@Inject("${project.beanPackage}")
	String packageName;
	@Inject("${project.findPass}")
	Boolean findPass;
	@Inject("${spring.database.type}")
	String databaseType;

	@Inject("${init.admin}")
	String initAdmin;
	@Inject("${init.pass}")
	String initPass;
	@Inject("${init.api}")
	Boolean initApi;

	@Init
	public void start() throws Throwable {

		// 找回密码
		if (findPass) {
			List<Admin> admins = sqlHelper.findAll(Admin.class);
			for (Admin admin : admins) {
				String randomPass = RandomUtil.randomString(8);
				
				admin.setAuth(false); // 关闭二次验证
				admin.setPass(EncodePassUtils.encode(randomPass));
				sqlHelper.updateById(admin);
				
				System.out.println(m.get("adminStr.name") + ":" + admin.getName() + " " + m.get("adminStr.pass") + ":" + randomPass);
			}
			System.exit(1);
		}

		// 初始化管理员账号
		if (StrUtil.isNotBlank(initAdmin) && StrUtil.isNotBlank(initPass)) {
			addAdmin();
		}

		// 初始化base值
		Long count = sqlHelper.findAllCount(Basic.class);
		if (count == 0) {
			List<Basic> basics = new ArrayList<Basic>();
			basics.add(new Basic("worker_processes", "auto", 1l));
			basics.add(new Basic("events", "{\r\n    worker_connections  1024;\r\n    accept_mutex on;\r\n}", 2l));
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

		// 释放基础nginx配置文件
		if (!FileUtil.exist(homeConfig.home + "fastcgi.conf")) {
			ClassPathResource resource = new ClassPathResource("conf.zip");
			InputStream inputStream = resource.getStream();
			ZipUtil.unzip(inputStream, new File(homeConfig.home), CharsetUtil.defaultCharset());
		}
		if (!FileUtil.exist(homeConfig.home + "nginx.conf")) {
			ClassPathResource resource = new ClassPathResource("nginx.conf");
			InputStream inputStream = resource.getStream();
			FileUtil.writeFromStream(inputStream, homeConfig.home + "nginx.conf");

		}

		// 设置nginx配置文件
		String nginxPath = settingService.get("nginxPath");
		if (StrUtil.isEmpty(nginxPath)) {
			nginxPath = homeConfig.home + "nginx.conf";
			// 设置nginx.conf路径
			settingService.set("nginxPath", nginxPath);
		}

		// 释放acme全新包
		String acmeShDir = homeConfig.home + ".acme.sh" + File.separator;
		ClassPathResource resource = new ClassPathResource("acme.zip");
		InputStream inputStream = resource.getStream();
		ZipUtil.unzip(inputStream, new File(acmeShDir), CharsetUtil.defaultCharset());

		// 全局黑白名单
		if (settingService.get("denyAllow") == null) {
			settingService.set("denyAllow", "0");
		}
		if (settingService.get("denyAllowStream") == null) {
			settingService.set("denyAllowStream", "0");
		}

		if (SystemTool.isLinux()) {
			// 查找ngx_stream_module模块
			if (!basicService.contain("ngx_stream_module.so") && FileUtil.exist("/usr/lib/nginx/modules/ngx_stream_module.so")) {
				Basic basic = new Basic("load_module", "/usr/lib/nginx/modules/ngx_stream_module.so", -10l);
				sqlHelper.insert(basic);
			}

			// 判断是否存在nginx命令
			if (hasNginx() && StrUtil.isEmpty(settingService.get("nginxExe"))) {
				// 设置nginx执行文件
				settingService.set("nginxExe", "nginx");
			}

			// 异步重启nginx, 重建pid
			ThreadUtil.execute(new Runnable() {

				@Override
				public void run() {

					String nginxExe = settingService.get("nginxExe");
					String nginxDir = settingService.get("nginxDir");
					String nginxPath = settingService.get("nginxPath");
					if (StrUtil.isNotEmpty(nginxExe) && StrUtil.isNotEmpty(nginxPath)) {
						runCmd("pkill -9 nginx");
						String cmd = nginxExe + " -c " + nginxPath;
						if (StrUtil.isNotEmpty(nginxDir)) {
							cmd += " -p " + nginxDir;
						}
						runCmd(cmd);
					}
				}

			});
		}

		// 展示logo
		showLogo();
	}

	private void runCmd(String cmd) {
		logger.info("run: " + cmd);
		RuntimeUtil.execForStr("/bin/sh", "-c", cmd);
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
		BufferedReader reader = resource.getReader(StandardCharsets.UTF_8);
		String str = null;
		StringBuilder stringBuilder = new StringBuilder();
		// 使用readLine() 比较方便的读取一行
		while (null != (str = reader.readLine())) {
			stringBuilder.append(str).append("\n");
		}
		reader.close();// 关闭流

		stringBuilder.append("nginxWebUI ").append(versionConfig.currentVersion).append("\n");

		logger.info(stringBuilder.toString());

	}

	@Deprecated
	private void transferSql() {
		// 关闭sqlite连接
		dataSourceEmbed.getDataSource().close();
		// 建立h2连接
		HikariConfig dbConfig = new HikariConfig();
		dbConfig.setJdbcUrl("jdbc:h2:" + homeConfig.home + "h2");
		dbConfig.setUsername("sa");
		dbConfig.setPassword("");
		dbConfig.setMaximumPoolSize(1);
		HikariDataSource dataSourceH2 = new HikariDataSource(dbConfig);
		dataSourceEmbed.setDataSource(dataSourceH2);
		// 读取全部数据
		Map<String, List<?>> map = readAll();

		// 关闭h2连接
		dataSourceH2.close();

		// 重新建立sqlite连接
		dataSourceEmbed.init();

		// 导入数据
		insertAll(map);

		// 重命名h2文件
		FileUtil.rename(new File(homeConfig.home + "h2.mv.db"), homeConfig.home + "h2.mv.db.bak", true);
	}

	private Map<String, List<?>> readAll() {
		Map<String, List<?>> map = new HashMap<>();

		Set<Class<?>> set = ClassUtil.scanPackage(packageName);
		for (Class<?> clazz : set) {
			Table table = clazz.getAnnotation(Table.class);
			if (table != null) {
				try {
					List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM " + SQLConstants.SUFFIX + StrUtil.toUnderlineCase(clazz.getSimpleName()) + SQLConstants.SUFFIX);

					map.put(clazz.getName(), sqlHelper.buildObjects(list, clazz));
				} catch (Exception e) {
					logger.info(e.getMessage(), e);
				}
			}
		}

		return map;
	}

	private void insertAll(Map<String, List<?>> map) {
		try {
			for (String key : map.keySet()) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Class.forName(key));

				sqlHelper.insertAll(map.get(key));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void addAdmin() {
		Long adminCount = sqlHelper.findAllCount(Admin.class);
		if (adminCount > 0) {
			return;
		}

		Admin admin = new Admin();
		admin.setName(initAdmin);
		admin.setPass(EncodePassUtils.encode(initPass));
		admin.setApi(initApi);
		admin.setType(0);

		sqlHelper.insert(admin);

	}
}
