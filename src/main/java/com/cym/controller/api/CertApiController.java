package com.cym.controller.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cym.controller.adminPage.CertController;
import com.cym.model.Cert;
import com.cym.service.CertService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "证书接口")
@RestController
@RequestMapping("/api/cert")
public class CertApiController extends BaseController {

	@Autowired
	CertController certController;
	@Autowired
	CertService certService;

	@SuppressWarnings("unchecked")
	@ApiOperation("获取证书分页列表")
	@PostMapping("getPage")
	public JsonResult<Page<Cert>> getPage(@ApiParam("当前页数(从1开始)") @RequestParam(defaultValue = "1") Integer current, //
			@ApiParam("每页数量(默认为10)") @RequestParam(defaultValue = "10") Integer limit) {
		Page page = new Page();
		page.setCurr(current);
		page.setLimit(limit);
		page = sqlHelper.findPage(page, Cert.class);

		return renderSuccess(page);
	}

	@ApiOperation("添加或编辑证书")
	@PostMapping("addOver")
	public JsonResult addOver(Cert cert) {
		if (StrUtil.isEmpty(cert.getDomain())) {
			return renderError("域名为空");
		}
		if (StrUtil.isEmpty(cert.getDnsType())) {
			return renderError("dns提供商为空");
		}
		if(cert.getDnsType().equals("ali") && (StrUtil.isEmpty(cert.getAliKey()) || StrUtil.isEmpty(cert.getAliSecret()))) {
			return renderError("aliKey 或 aliSecret为空");
		}
		if(cert.getDnsType().equals("dp") && (StrUtil.isEmpty(cert.getDpId()) || StrUtil.isEmpty(cert.getDpKey()))) {
			return renderError("dpId 或 dpKey为空");
		}
		if(cert.getDnsType().equals("cf") && (StrUtil.isEmpty(cert.getCfEmail()) || StrUtil.isEmpty(cert.getCfKey()))) {
			return renderError("cfEmail 或 cfKey为空");
		}
		if(cert.getDnsType().equals("gd") && (StrUtil.isEmpty(cert.getGdKey()) || StrUtil.isEmpty(cert.getGdSecret()))) {
			return renderError("gdKey 或 gdSecret为空");
		}
		return certController.addOver(cert);
	}

	@ApiOperation("设置证书自动续签")
	@PostMapping("setAutoRenew")
	public JsonResult setAutoRenew(@ApiParam("主键id")String id, @ApiParam("是否自动续签:0否 1是")Integer autoRenew) {
		Cert cert = new Cert();
		cert.setId(id);
		cert.setAutoRenew(autoRenew);

		certController.setAutoRenew(cert);
		return renderSuccess();
	}

	@ApiOperation("删除证书")
	@PostMapping("del")
	public JsonResult del(String id) {
		return certController.del(id);
	}

	@ApiOperation("执行申请")
	@PostMapping("apply")
	public JsonResult apply(@ApiParam("主键id") String id,@ApiParam("申请类型 issue:申请 renew:续签") String type) {

		return certController.apply(id, type);
	}

	@ApiOperation("下载证书文件")
	@PostMapping("download")
	public void download(@ApiParam("主键id")String id, HttpServletResponse response) throws IOException {
		certController.download(id, response);
	}
}
