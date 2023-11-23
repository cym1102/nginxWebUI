package com.cym.controller.adminPage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.ext.AsycPack;
import com.cym.model.Cert;
import com.cym.model.CertCode;
import com.cym.service.CertService;
import com.cym.service.ConfService;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.utils.BaseController;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/export")
public class ExportController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	ConfService confService;
	@Inject
	CertService certService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {

		modelAndView.view("/adminPage/export/index.html");
		return modelAndView;
	}

	@Mapping("dataExport")
	public DownloadedFile dataExport(Context context) {
		AsycPack asycPack = confService.getAsycPack(new String[] { "all" });
		// 导出证书
		asycPack.setCertList(sqlHelper.findAll(Cert.class));
		asycPack.setCertCodeList(sqlHelper.findAll(CertCode.class));
		asycPack.setAcmeZip(certService.getAcmeZipBase64());
		
		String json = JSONUtil.toJsonPrettyStr(asycPack);

		String date = DateUtil.format(new Date(), "yyyy-MM-dd_HH-mm-ss");
		DownloadedFile downloadedFile = new DownloadedFile("application/octet-stream", new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), date + ".json");
		return downloadedFile;
	}

	@Mapping(value = "dataImport")
	public void dataImport(UploadedFile file, Context context) throws IOException {
		if (file != null) {
			File tempFile = new File(homeConfig.home + "temp" + File.separator + file.getName().replace("..", ""));
			FileUtil.mkdir(tempFile.getParentFile());
			file.transferTo(tempFile);
			String json = FileUtil.readString(tempFile, StandardCharsets.UTF_8);
			tempFile.delete();

			AsycPack asycPack = JSONUtil.toBean(json, AsycPack.class);
			confService.setAsycPack(asycPack);
			
			// 导入证书
			if (asycPack.getCertList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), Cert.class);
				sqlHelper.insertAll(asycPack.getCertList());
			}
			if (asycPack.getCertCodeList() != null) {
				sqlHelper.deleteByQuery(new ConditionAndWrapper(), CertCode.class);
				sqlHelper.insertAll(asycPack.getCertCodeList());
			}
			
			certService.writeAcmeZipBase64(asycPack.getAcmeZip());
		}
		context.redirect("/adminPage/export?over=true");
	}

	@Mapping("logExport")
	public DownloadedFile logExport(Context context) throws IOException {
		File file = new File(homeConfig.home + "log/nginxWebUI.log");
		if (file.exists()) {
			DownloadedFile downloadedFile = new DownloadedFile("application/octet-stream", Files.newInputStream(file.toPath()), file.getName());
			return downloadedFile;
		}

		return null;
	}

}
