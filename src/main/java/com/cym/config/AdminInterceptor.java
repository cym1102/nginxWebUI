package com.cym.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Remote;
import com.cym.service.CreditService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Component
public class AdminInterceptor implements HandlerInterceptor {

	@Autowired
	CreditService creditService;

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
//		System.err.println(request.getRequestURL());
		String ctx = getCtx(request.getRequestURL().toString() + "/");

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
		if (localType != null && localType.equals("远程") && !request.getRequestURL().toString().contains("adminPage/remote")) {
			// 转发到远程服务器
			Remote remote = (Remote) request.getSession().getAttribute("remote");

			String url = buildUrl(ctx, request, remote);
			String body = buldBody(request.getParameterMap(), remote);
			try {
				String rs = HttpUtil.post(url, body);

				rs = rs.replace(remote.getIp(), request.getServerName()).replace(":" + remote.getPort().toString(), ":" + request.getServerPort());

				response.setCharacterEncoding("utf-8");
				response.setContentType("text/html;charset=utf-8");
				
				if(JSONUtil.isJson(rs)) {
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
				}else {
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

	private String buldBody(Map<String, String[]> parameterMap, Remote remote) {
		List<String> body = new ArrayList<>();
		body.add("creditKey=" + remote.getCreditKey());
		body.add("loca=" + remote.getCreditKey());

		for (Iterator itr = parameterMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();

			for (String value : (String[]) me.getValue()) {
				body.add(me.getKey() + "=" + value);
			}

		}

		return StrUtil.join("&", body);
	}

	private String buildUrl(String ctx, HttpServletRequest request, Remote remote) {
		String url = request.getRequestURL().toString().replace(ctx, remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/");

//		url += "?creditKey=" + remote.getCreditKey();
//		if (StrUtil.isNotEmpty(request.getQueryString())) {
//			url += "&" + request.getQueryString();
//		}
		return url;
	}

	private static String getCtx(String url) {
		URI effectiveURI = null;

		try {
			URI uri = new URI(url);
			effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
		} catch (Throwable var4) {
			effectiveURI = null;
		}
		return effectiveURI.toString();
	}

}