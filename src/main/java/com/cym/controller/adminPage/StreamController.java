package com.cym.controller.adminPage;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Http;
import com.cym.model.Stream;
import com.cym.service.StreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;

@Controller
@RequestMapping("/adminPage/stream")
public class StreamController extends BaseController {
	@Autowired
	StreamService streamService;
	
	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {
		List<Stream> streamList = sqlHelper.findAll(new Sort("name", Direction.ASC), Stream.class);

		modelAndView.addObject("streamList", streamList);
		modelAndView.setViewName("/adminPage/stream/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Stream stream)  {
		if(streamService.hasName(stream.getName())) {
			return renderError("名称已存在");
		}
		sqlHelper.insertOrUpdate(stream);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id)  {
		return renderSuccess(sqlHelper.findById(id, Stream.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id)  {
		sqlHelper.deleteById(id, Stream.class);
		
		return renderSuccess();
	}

}
