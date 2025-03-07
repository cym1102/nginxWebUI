package com.cym.controller.api;

import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.controller.adminPage.WwwController;
import com.cym.model.Cert;
import com.cym.model.Www;
import com.cym.service.WwwService;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

/**
 * 静态网页接口
 *
 */
@Mapping("/api/www")
@Controller
public class WwwApiController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	WwwService wwwService;
	@Inject
	WwwController wwwController;

	/**
	 * 获取静态网页分页列表
	 * 
	 */
	@Mapping("getList")
	public JsonResult<List<Www>> getList() {
		List<Www> wwws = sqlHelper.findAll(new Sort("dir", Direction.ASC), Www.class);

		return renderSuccess(wwws);
	}

	/**
	 * 添加或编辑静态网页
	 * 
	 * @param www     静态网页
	 * @param dirTemp zip文件路径
	 * 
	 */
	@Mapping("addOver")
	public JsonResult addOver(Www www, String dirTemp) {
		return wwwController.addOver(www, dirTemp);
	}

	/**
	 * 清空静态网页路径
	 * 
	 * @param id 静态网页id
	 * 
	 */
	@Mapping("clean")
	public JsonResult clean(String id) {
		return wwwController.clean(id);
	}

	/**
	 * 删除静态网页
	 * 
	 * @param id 静态网页id
	 * 
	 */
	@Mapping("del")
	public JsonResult del(String id) {
		return wwwController.del(id);
	}

	/**
	 * 静态网页详情
	 * 
	 * @param id 静态网页id
	 * 
	 */
	@Mapping("detail")
	public JsonResult<Www> detail(String id) {
		return wwwController.detail(id);
	}

	/**
	 * 获取静态网页描述
	 * 
	 * @param id 静态网页id
	 * 
	 */
	@Mapping("getDescr")
	public JsonResult<String> getDescr(String id) {
		return wwwController.getDescr(id);
	}

	/**
	 * 编辑静态网页描述
	 * 
	 * @param id    静态网页id
	 * @param descr 静态网页描述
	 * 
	 */
	@Mapping("editDescr")
	public JsonResult editDescr(String id, String descr) {
		return wwwController.editDescr(id, descr);
	}
}
