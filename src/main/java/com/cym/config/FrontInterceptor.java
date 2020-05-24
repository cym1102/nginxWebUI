package com.cym.config;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class FrontInterceptor implements HandlerInterceptor {

	@Value("${spring.application.name}")
	String projectName;

	@Autowired
	VersionConfig versionConfig;

	@Value("${project.version}")
	String currentVersion;
	
	
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
		String ctx = getIP(request.getRequestURL().toString() + "/");
		request.setAttribute("ctx", ctx);
		request.setAttribute("jsrandom", System.currentTimeMillis());
		request.setAttribute("projectName", projectName);

		if (versionConfig.getVersion() != null) {
			request.setAttribute("version", versionConfig.getVersion());
			
			if(Integer.parseInt(currentVersion.replace(".", "").replace("v", "")) < Integer.parseInt(versionConfig.getVersion().getVersion().replace(".", "").replace("v", ""))) {
				request.setAttribute("hasNewVersion", 1);
			}
			
		}
		return true;
	}

	private static String getIP(String url) {
		URI effectiveURI = null;

		try {
			URI uri = new URI(url);
			effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
		} catch (Throwable var4) {
			effectiveURI = null;
		}
		return effectiveURI.toString().replace("http:", "").replace("https:", "");
	}

}