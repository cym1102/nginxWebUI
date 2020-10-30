package com.cym.controller.adminPage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Basic;
import com.cym.service.BasicService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.MessageUtils;

import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.hutool.core.util.StrUtil;

@Controller
@RequestMapping("/adminPage/basic")
public class BasicController extends BaseController {
	@Autowired
	BasicService basicService;
	
	@RequestMapping("")
	public ModelAndView index(ModelAndView modelAndView) {
		List<Basic> basicList = sqlHelper.findAll(new Sort().add(Basic::getSeq, Direction.ASC), Basic.class);

		modelAndView.addObject("basicList", basicList);
		modelAndView.setViewName("/adminPage/basic/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Basic base) {
		if (StrUtil.isEmpty(base.getId())) {
			base.setSeq(basicService.buildOrder());
		}
		sqlHelper.insertOrUpdate(base);

		return renderSuccess();
	}

	@RequestMapping("setOrder")
	@ResponseBody
	public JsonResult setOrder(String id, Integer count) {
		basicService.setSeq(id, count);

		return renderSuccess();
	}
	
	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Basic.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Basic.class);

		return renderSuccess();
	}

}
