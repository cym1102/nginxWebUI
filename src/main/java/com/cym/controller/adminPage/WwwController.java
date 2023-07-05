package com.cym.controller.adminPage;

import java.net.URL;
import java.nio.charset.Charset;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.Server;
import com.cym.model.Www;
import com.cym.service.WwwService;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;

@Mapping("/adminPage/www")
@Controller
public class WwwController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	WwwService wwwService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {

		modelAndView.put("list", sqlHelper.findAll(new Sort("dir", Direction.ASC), Www.class));
		modelAndView.view("/adminPage/www/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(Www www, String dirTemp) {
		if (wwwService.hasDir(www.getDir(), www.getId())) {
			return renderError(m.get("wwwStr.sameDir"));
		}

		try {
			FileUtil.clean(www.getDir());

			try {
				ZipUtil.unzip(dirTemp, www.getDir());
			} catch (Exception e) {
				// 默认UTF-8下不能解压中文字符, 尝试使用gbk
				ZipUtil.unzip(dirTemp, www.getDir(), Charset.forName("GBK"));
			}

			FileUtil.del(dirTemp);
			sqlHelper.insertOrUpdate(www);

			return renderSuccess();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return renderError(m.get("wwwStr.zipError"));
	}

	@Mapping("del")
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Www.class);

		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		Www www = sqlHelper.findById(id, Www.class);

		return renderSuccess(www);
	}

	public String getClassPath() throws Exception {
		try {
			String strClassName = getClass().getName();
			String strPackageName = "";
			if (getClass().getPackage() != null) {
				strPackageName = getClass().getPackage().getName();
			}
			String strClassFileName = "";
			if (!"".equals(strPackageName)) {
				strClassFileName = strClassName.substring(strPackageName.length() + 1, strClassName.length());
			} else {
				strClassFileName = strClassName;
			}
			URL url = null;
			url = getClass().getResource(strClassFileName + ".class");
			String strURL = url.toString();
			strURL = strURL.substring(strURL.indexOf('/') + 1, strURL.lastIndexOf('/'));
			// 返回当前类的路径，并且处理路径中的空格，因为在路径中出现的空格如果不处理的话，
			// 在访问时就会从空格处断开，那么也就取不到完整的信息了，这个问题在web开发中尤其要注意
			return strURL.replaceAll("%20", " ");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Mapping("getDescr")
	public JsonResult getDescr(String id) {
		Www www = sqlHelper.findById(id, Www.class);
		return renderSuccess(www.getDescr());
	}

	@Mapping("editDescr")
	public JsonResult editDescr(String id, String descr) {
		Www www = new Www();
		www.setId(id);
		www.setDescr(descr);
		sqlHelper.updateById(www);

		return renderSuccess();
	}
}
