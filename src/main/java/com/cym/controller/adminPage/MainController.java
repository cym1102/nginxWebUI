package com.cym.controller.adminPage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.ext.ConfExt;
import com.cym.model.Http;
import com.cym.model.Server;
import com.cym.model.Stream;
import com.cym.model.Upstream;
import com.cym.service.ConfService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.utils.BaseController;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.ToolUtils;
import com.cym.utils.UpdateUtils;
import com.github.odiszapc.nginxparser.NgxBlock;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxDumper;
import com.github.odiszapc.nginxparser.NgxParam;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;

@Mapping("")
@Controller
public class MainController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	UpdateUtils updateUtils;
	@Inject
	SettingService settingService;
	@Inject
	ConfService confService;

	private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, String keywords) {
		modelAndView.view("/adminPage/index.html");
		return modelAndView;
	}

	@Mapping("doc.html")
	public void doc(Context context) {
		context.redirect("doc/api.html");
	}

	@Mapping("/adminPage/main/upload")
	public JsonResult upload(Context context, UploadedFile file) {
		try {
			File temp = new File(FileUtil.getTmpDir() + File.separator + file.getName().replace("..", ""));
			file.transferTo(temp);

			return renderSuccess(temp.getPath().replace("\\", "/"));
		} catch (IllegalStateException | IOException e) {
			logger.error(e.getMessage(), e);
		}

		return renderError();
	}

	@Mapping("/adminPage/main/autoUpdate")
	public JsonResult autoUpdate(String url) {
		// 仅允许从受信任的官方更新源下载，防止任意 URL 下载并执行 JAR 导致 RCE
		if (!isTrustedUpdateUrl(url)) {
			LOG.warn("非法更新地址被拒绝: {}", url);
			return renderError("更新地址非法，仅允许官方更新源");
		}

		File jar = JarUtil.getCurrentFile();
		String path = jar.getParent();
		LOG.info("download:" + path);
		HttpUtil.downloadFile(url, path);

		String fileName = url.split("/")[url.split("/").length - 1];
		updateUtils.run(path + File.separator + fileName);
		return renderSuccess();
	}

	// 受信任的更新源域名，对应 VersionConfig 中获取版本信息的官方地址及 GitHub 发布源
	private static final List<String> TRUSTED_UPDATE_HOSTS = Arrays.asList( //
			"www.nginxwebui.cn", //
			"nginxwebui.cn", //
			"github.com", //
			"raw.githubusercontent.com");

	private boolean isTrustedUpdateUrl(String url) {
		if (url == null || url.isEmpty()) {
			return false;
		}
		try {
			URI uri = URI.create(url);
			String host = uri.getHost();
			if (host == null) {
				return false;
			}
			host = host.toLowerCase();
			for (String trusted : TRUSTED_UPDATE_HOSTS) {
				if (host.equals(trusted) || host.endsWith("." + trusted)) {
					return true;
				}
			}
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Mapping("/adminPage/main/preview")
	public JsonResult preview(String id, String type) {
		ConfExt confExt = new ConfExt();
		confExt.setFileList(new ArrayList<>());

		NgxBlock ngxBlock = null;
		if (type.equals("server")) {
			Server server = sqlHelper.findById(id, Server.class);
			ngxBlock = confService.bulidBlockServer(server, confExt);
		} else if (type.equals("upstream")) {
			Upstream upstream = sqlHelper.findById(id, Upstream.class);
			ngxBlock = confService.buildBlockUpstream(upstream);
		} else if (type.equals("http")) {
			List<Http> httpList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Http.class);
			ngxBlock = new NgxBlock();
			ngxBlock.addValue("http");
			for (Http http : httpList) {
				if (http.getEnable() == null || !http.getEnable()) {
					continue;
				}

				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(http.getName().trim() + " " + http.getValue().trim());
				ngxBlock.addEntry(ngxParam);
			}

			confService.buildDenyAllow(ngxBlock, "http", "httpDenyAllow", confExt);
		} else if (type.equals("stream")) {
			List<Stream> streamList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Stream.class);
			ngxBlock = new NgxBlock();
			ngxBlock.addValue("stream");
			for (Stream stream : streamList) {
				NgxParam ngxParam = new NgxParam();
				ngxParam.addValue(stream.getName() + " " + stream.getValue());
				ngxBlock.addEntry(ngxParam);
			}

			confService.buildDenyAllow(ngxBlock, "stream", "streamDenyAllow", confExt);
		}
		NgxConfig ngxConfig = new NgxConfig();
		ngxConfig.addEntry(ngxBlock);

		String conf = ToolUtils.handleConf(new NgxDumper(ngxConfig).dump());

		return renderSuccess(conf);
	}

}