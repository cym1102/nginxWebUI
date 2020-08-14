package com.cym.controller.adminPage;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.cym.utils.BaseController;

@RequestMapping("/adminPage/abort")
@Controller
public class AbortController extends BaseController {

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {

		modelAndView.setViewName("/adminPage/abort/index");
		return modelAndView;
	}

}
