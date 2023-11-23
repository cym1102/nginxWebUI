package com.cym.config;

import java.io.File;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.utils.FilePermissionUtil;
import com.cym.utils.JarUtil;
import com.cym.utils.SystemTool;
import com.cym.utils.ToolUtils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@Component
public class HomeConfig  {
	@Inject("${project.home}")
	public String home;
	public String acmeShDir;
	public String acmeSh;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Init
	public void afterInjection() {
		if (StrUtil.isEmpty(home)) {
			// 获取jar位置
			File file = new File(JarUtil.getCurrentFilePath());

			if (file.getPath().contains("target") && file.getPath().contains("classes")) {
				home = FileUtil.getUserHomePath() + File.separator + "nginxWebUI";
			} else {
				home = file.getParent();
			}
		}

		// windows 加上盘符
		if (SystemTool.isWindows() && !home.contains(":")) {
			home = JarUtil.getCurrentFilePath().split(":")[0] + ":" + home;
		}

		// 如果最后没有/, 加上/
		home = ToolUtils.endDir(ToolUtils.handlePath(home));
		
		// 检查路home权限
		if (!FilePermissionUtil.canWrite(new File(home))) {
			logger.error(home + " " + "directory does not have writable permission. Please specify it again.");
			logger.error(home + " " + "目录没有可写权限,请重新指定.");
			System.exit(1);
		}
		
		// acme路径
		acmeShDir = home + ".acme.sh/";
		acmeSh = home + ".acme.sh/acme.sh";
	}


}
