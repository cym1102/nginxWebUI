package com.cym.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Admin;
import com.cym.model.Remote;
import com.cym.service.CreditService;
import com.cym.utils.BaseController;
import com.cym.utils.MessageUtils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Component
public class AdminInterceptor implements HandlerInterceptor {

	@Autowired
	CreditService creditService;
	@Autowired
	MessageUtils m;

	/*
	 * 视图渲染之后的操作
	 */
	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {

	}

	/*
	 * 处理请求完成后视图渲染之前的处理操作
	 */
	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {

	}

	/*
	 * 进入controller层之前拦截请求
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) throws Exception {
		String httpHost = request.getHeader("X-Forwarded-Host");
		String realPort = request.getHeader("X-Forwarded-Port");
		String host = request.getHeader("Host");

		String ctx = getCtx(httpHost, host, realPort);

		if (request.getRequestURL().toString().contains("adminPage/login")) {
			return true;
		}

		String creditKey = request.getParameter("creditKey");
		Boolean isCredit = creditService.check(creditKey);

		Boolean isLogin = (Boolean) request.getSession().getAttribute("isLogin");
		if (!((isLogin != null && isLogin) || isCredit)) {
			response.sendRedirect(ctx + "/adminPage/login");
			return false;
		}

		String localType = (String) request.getSession().getAttribute("localType");
		if (localType != null //
				&& localType.equals("remote") //
				&& !request.getRequestURL().toString().contains("adminPage/remote") //
				&& !request.getRequestURL().toString().contains("adminPage/admin") //
				&& !request.getRequestURL().toString().contains("adminPage/abort") //
		) {
			// 转发到远程服务器
			Remote remote = (Remote) request.getSession().getAttribute("remote");
			String url = buildUrl(ctx, request, remote);

			try {
				String rs = null;
				if (url.contains("main/upload")) {
					// 上传文件
					Map<String, Object> map = new HashMap<>();
					map.put("creditKey", remote.getCreditKey());
					MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
					MultipartFile multipartFile = multipartRequest.getFile("file");
					File temp = new File(FileUtil.getTmpDir() + "/" + multipartFile.getOriginalFilename());
					multipartFile.transferTo(temp);
					map.put("file", temp);

					rs = HttpUtil.post(url, map);

				} else {
					// 普通请求
					Admin admin = new BaseController().getAdmin(request);
					String body = buldBody(request.getParameterMap(), remote, admin);
					rs = HttpUtil.post(url, body);
				}

//				String remoteDomain = "//" + remote.getIp() + ":" + remote.getPort();
//				String remoteDomainNoPort = "//" + remote.getIp();
//				String localDomain = "//" + request.getServerName() + ":" + request.getServerPort();
//
//				rs = rs.replace(remoteDomain + "/", localDomain + "/")//
//						.replace(remoteDomainNoPort + "/", localDomain + "/")//
//						.replace(remoteDomain + "/adminPage", localDomain + "/adminPage")//
//						.replace(remoteDomainNoPort + "/adminPage", localDomain + "/adminPage")//
//						.replace(remoteDomain + "/lib", localDomain + "/lib")//
//						.replace(remoteDomainNoPort + "/lib", localDomain + "/lib")//
//						.replace(remoteDomain + "/js", localDomain + "/js")//
//						.replace(remoteDomainNoPort + "/js", localDomain + "/js")//
//						.replace(remoteDomain + "/css", localDomain + "/css")//
//						.replace(remoteDomainNoPort + "/css", localDomain + "/css")//
//						.replace(remoteDomain + "/img", localDomain + "/img")//
//						.replace(remoteDomainNoPort + "/img", localDomain + "/img")//
//				;
				response.setCharacterEncoding("utf-8");
				response.setContentType("text/html;charset=utf-8");

				if (JSONUtil.isJson(rs)) {
					String date = DateUtil.format(new Date(), "yyyy-MM-dd_HH-mm-ss");
					response.addHeader("Content-Type", "application/octet-stream");
					response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(date + ".json", "UTF-8")); // 设置文件名

					byte[] buffer = new byte[1024];
					BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(rs.getBytes(Charset.forName("UTF-8"))));
					OutputStream os = response.getOutputStream();
					int i = bis.read(buffer);
					while (i != -1) {
						os.write(buffer, 0, i);
						i = bis.read(buffer);
					}
				} else {
					PrintWriter out = response.getWriter();
					out.append(rs);
				}

			} catch (Exception e) {
				e.printStackTrace();
				response.sendRedirect(ctx + "/adminPage/login/noServer");
			}
			return false;
		}

		return true;
	}

	private String buldBody(Map<String, String[]> parameterMap, Remote remote, Admin admin) throws UnsupportedEncodingException {
		List<String> body = new ArrayList<>();
		body.add("creditKey=" + remote.getCreditKey());
		if (admin != null) {
			body.add("adminName=" + admin.getName());
		}

		for (Iterator itr = parameterMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();

			for (String value : (String[]) me.getValue()) {
				body.add(me.getKey() + "=" + URLEncoder.encode(value, "UTF-8"));
			}

		}

		return StrUtil.join("&", body);
	}

	private String buildUrl(String ctx, HttpServletRequest request, Remote remote) {
		String url = request.getRequestURL().toString().replace(ctx, "//" + remote.getIp() + ":" + remote.getPort() + "/");

		if (url.startsWith("http")) {
			url = url.replace("http:", "").replace("https:", "");

		}
		url = remote.getProtocol() + ":" + url;

		Admin admin = (Admin) request.getSession().getAttribute("admin");
		String showAdmin = "false";
		if (admin != null && admin.getType() == 0) {
			showAdmin = "true";
		}
		return url + "?jsrandom=" + System.currentTimeMillis() + //
				"&protocol=" + remote.getProtocol() + //
				"&showAdmin=" + showAdmin + //
				"&ctx=" + Base64.encode(ctx);
	}

	public static String getCtx(String httpHost, String host, String realPort) {
		String ctx = "//";
		if (StrUtil.isNotEmpty(httpHost)) {
			ctx += httpHost;
		} else {
			ctx += host;
			if (!host.contains(":") && StrUtil.isNotEmpty(realPort)) {
				ctx += ":" + realPort;
			}
		}
		return ctx;
	}

}