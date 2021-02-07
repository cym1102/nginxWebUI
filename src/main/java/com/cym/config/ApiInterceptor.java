package com.cym.config;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cym.service.AdminService;
import com.cym.utils.JsonResult;
import com.cym.utils.MessageUtils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Component
public class ApiInterceptor implements HandlerInterceptor {

	@Autowired
	MessageUtils m;
	@Autowired
	AdminService adminService;
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
		String token = request.getHeader("token");

		if (StrUtil.isNotEmpty(token) && adminService.checkToken(token)) {
			return true;
		} else {

			JsonResult result = new JsonResult();
			result.setSuccess(false);
			result.setStatus("401");
			result.setMsg(m.get("apiStr.wrongToken")); 

			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.append(JSONUtil.toJsonPrettyStr(result));

			return false;
		}

	}

}