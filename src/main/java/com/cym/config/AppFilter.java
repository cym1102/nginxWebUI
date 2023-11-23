package com.cym.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.core.handle.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.Admin;
import com.cym.model.Message;
import com.cym.model.Remote;
import com.cym.service.AdminService;
import com.cym.service.CreditService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.MessageUtils;
import com.cym.utils.PropertiesUtils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Component
public class AppFilter implements Filter {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	AdminService adminService;
	@Inject
	MessageUtils m;
	@Inject
	CreditService creditService;
	@Inject("${solon.app.name}")
	String projectName;

	@Inject
	VersionConfig versionConfig;

	@Inject
	PropertiesUtils propertiesUtils;
	@Inject
	SettingService settingService;

	@Override
	public void doFilter(Context ctx, FilterChain chain) throws Throwable {
		
		String path = ctx.path().toLowerCase();
		
		// 全局过滤器
		if (!path.contains("/lib/") //
				&& !path.toLowerCase().contains("/js/") //
				&& !path.toLowerCase().contains("/doc/") //
				&& !path.toLowerCase().contains("/img/") //
				&& !path.toLowerCase().contains("/css/")) {
			frontInterceptor(ctx);
		}

		// 登录过滤器
		if (path.toLowerCase().contains("/adminPage/".toLowerCase()) //
				&& !path.contains("/lib/") //
				&& !path.contains("/doc/") //
				&& !path.contains("/js/") //
				&& !path.contains("/img/") //
				&& !path.contains("/css/")) {
			if (!adminInterceptor(ctx)) {
				// 设置为已处理
				ctx.setHandled(true);
				return;
			}
		}

		// api过滤器
		if (path.toLowerCase().contains("/api/") //
				&& !path.contains("/lib/") //
				&& !path.contains("/doc/") //
				&& !path.contains("/js/") //
				&& !path.contains("/img/") //
				&& !path.contains("/css/")) {
			if (!apiInterceptor(ctx)) {
				// 设置为已处理
				ctx.setHandled(true);
				return;
			}
		}

		chain.doFilter(ctx);

	}

	private boolean apiInterceptor(Context ctx) {
		String token = ctx.header("token");
		Admin admin = adminService.getByToken(token);

		if (admin != null && admin.getApi()) {
			return true;
		} else {

			JsonResult result = new JsonResult();
			result.setSuccess(false);
			result.setStatus("401");
			result.setMsg(m.get("apiStr.wrongToken"));

			ctx.output(JSONUtil.toJsonPrettyStr(result));
			return false;
		}

	}

	private boolean adminInterceptor(Context ctx) {
		String ctxStr = getCtxStr(ctx);

		if (ctx.path().toLowerCase().contains("adminPage/login".toLowerCase())) {
			return true;
		}

		String creditKey = ctx.param("creditKey");
		boolean isCredit = creditService.check(creditKey);

		Boolean isLogin = (Boolean) ctx.session("isLogin");
		if (!((isLogin != null && isLogin) || isCredit)) {
			ctx.redirect("/adminPage/login");
			return false;
		}

		String localType = (String) ctx.session("localType");
		if (localType != null //
				&& localType.equals("remote") //
				&& !ctx.path().toLowerCase().contains("adminPage/remote".toLowerCase()) //
				&& !ctx.path().toLowerCase().contains("adminPage/admin".toLowerCase()) //
				&& !ctx.path().toLowerCase().contains("adminPage/about".toLowerCase()) //
		) {
			// 转发到远程服务器
			Remote remote = (Remote) ctx.session("remote");
			String url = buildUrl(ctxStr, ctx, remote);

			try {
				String rs = null;
				if (url.contains("main/upload")) {
					// 上传文件
					Map<String, Object> map = new HashMap<>();
					map.put("creditKey", remote.getCreditKey());

					UploadedFile uploadedFile = ctx.file("file");

					File temp = new File(FileUtil.getTmpDir() + "/" + uploadedFile.getName());
					uploadedFile.transferTo(temp);
					map.put("file", temp);

					rs = HttpUtil.post(url, map);

				} else {
					// 普通请求
					Admin admin = new BaseController().getAdmin();
					String body = buldBody(ctx.paramsMap(), remote, admin);
					rs = HttpUtil.post(url, body);
				}

				ctx.charset("utf-8");
				ctx.contentType("text/html;charset=utf-8");

				if (JSONUtil.isTypeJSON(rs)) {
					String date = DateUtil.format(new Date(), "yyyy-MM-dd_HH-mm-ss");
					ctx.headerOrDefault("Content-Type", "application/octet-stream");
					ctx.headerOrDefault("content-disposition", "attachment;filename=" + URLEncoder.encode(date + ".json", "UTF-8")); // 设置文件名

					byte[] buffer = new byte[1024];
					BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(rs.getBytes(StandardCharsets.UTF_8)));
					OutputStream os = ctx.outputStream();
					int i = bis.read(buffer);
					while (i != -1) {
						os.write(buffer, 0, i);
						i = bis.read(buffer);
					}
				} else {
					ctx.output(rs);
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				ctx.redirect("/adminPage/login/noServer");
			}

			return false;
		}

		return true;
	}

	private void frontInterceptor(Context ctx) {
		String ctxStr = getCtxStr(ctx);
		if (StrUtil.isNotEmpty(ctx.param("ctx"))) {
			ctxStr = Base64.decodeStr(ctx.param("ctx"));
		}

		ctx.attrSet("ctx", ctxStr);

		ctx.attrSet("jsrandom", versionConfig.currentVersion);
		ctx.attrSet("currentVersion", versionConfig.currentVersion);
		ctx.attrSet("projectName", projectName);

		ctx.attrSet("showAdmin", ctx.param("showAdmin"));
		ctx.attrSet("admin", ctx.session("admin"));

		// 显示版本更新
		if (versionConfig.newVersion != null) {
			ctx.attrSet("newVersion", versionConfig.newVersion);

			if (Integer.parseInt(versionConfig.currentVersion.replace(".", "").replace("v", "")) < Integer.parseInt(versionConfig.newVersion.getVersion().replace(".", "").replace("v", ""))) {
				ctx.attrSet("hasNewVersion", 1);
			}
		}

		// 读取配置文件
		Properties properties = null;
		String l = ctx.param("l");
		if (StrUtil.isNotEmpty(l) && l.equals("en_US") || settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
			settingService.set("lang", "en_US");
			properties = m.getPropertiesEN();
		} else {
			settingService.set("lang", "");
			properties = m.getProperties();
		}

		// js国际化
		Set<String> messageHeaders = new HashSet<>();
		List<Message> messages = new ArrayList<>();
		for (String key : properties.stringPropertyNames()) {
			Message message = new Message();
			message.setKey(key);
			message.setValue(properties.getProperty(key));
			messages.add(message);

			messageHeaders.add(key.split("\\.")[0]);
		}

		ctx.attrSet("messageHeaders", messageHeaders);
		ctx.attrSet("messages", messages);

		// html国际化
		for (String key : messageHeaders) {
			Map<String, String> map = new HashMap<>();
			for (Message message : messages) {
				if (message.getKey().split("\\.")[0].equals(key)) {
					map.put(message.getKey().split("\\.")[1], message.getValue());
				}
			}

			ctx.attrSet(key, map);
		}

		if (settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
			ctx.attrSet("langType", "切换到中文");
		} else {
			ctx.attrSet("langType", "Switch to English");
		}

	}

	private String buldBody(Map<String, List<String>> parameterMap, Remote remote, Admin admin) throws UnsupportedEncodingException {
		List<String> body = new ArrayList<>();
		body.add("creditKey=" + remote.getCreditKey());
		if (admin != null) {
			body.add("adminName=" + admin.getName());
		}

		for (Iterator itr = parameterMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();

			for (String value : (List<String>) me.getValue()) {
				body.add(me.getKey() + "=" + URLEncoder.encode(value, "UTF-8"));
			}

		}

		return StrUtil.join("&", body);
	}

	private String buildUrl(String ctxStr, Context ctx, Remote remote) {
		String url = ctx.url().replace(ctxStr, "//" + remote.getIp() + ":" + remote.getPort() + "/");

		if (url.startsWith("http")) {
			url = url.replace("http:", "").replace("https:", "");

		}
		url = remote.getProtocol() + ":" + url;

		Admin admin = (Admin) ctx.session("admin");
		String showAdmin = "false";
		if (admin != null && admin.getType() == 0) {
			showAdmin = "true";
		}
		return url + "?jsrandom=" + System.currentTimeMillis() + //
				"&protocol=" + remote.getProtocol() + //
				"&showAdmin=" + showAdmin + //
				"&ctx=" + Base64.encode(ctxStr);
	}

	public String getCtxStr(Context context) {
		String httpHost = context.header("X-Forwarded-Host");
		String realPort = context.header("X-Forwarded-Port");
		String host = context.header("Host");

		String ctx = "//";
		if (StrUtil.isNotEmpty(httpHost)) {
			ctx += httpHost;
		} else if (StrUtil.isNotEmpty(host)) {
			ctx += host;
			if (!host.contains(":") && StrUtil.isNotEmpty(realPort)) {
				ctx += ":" + realPort;
			}
		} else {
			host = context.url().split("/")[2];
			ctx += host;
			if (!host.contains(":") && StrUtil.isNotEmpty(realPort)) {
				ctx += ":" + realPort;
			}
		}
		return ctx;

	}

}