package com.cym;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Test;
import org.noear.solonhat.smartdoc.SolonHtmlApiDocBuilder;

import com.power.doc.constants.DocGlobalConstants;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.SourceCodePath;

import cn.hutool.core.io.FileUtil;

public class Doc {

	/**
	 * 包括设置请求头，缺失注释的字段批量在文档生成期使用定义好的注释
	 */
	@Test
	public void generate() {
		ApiConfig config = new ApiConfig();

		config.setServerUrl("http://your_ip:port");

		// 设置用md5加密html文件名,不设置为true，html的文件名将直接为controller的名称
		config.setStrict(true);// true会严格要求注释，推荐设置true
		config.setOutPath(DocGlobalConstants.HTML_DOC_OUT_PATH);// 输出到static/doc下

		// 不指定SourcePaths默认加载代码为项目src/main/java下的,如果项目的某一些实体来自外部代码可以一起加载
		config.setSourceCodePaths(//
				SourceCodePath.path().setPath("src/main/java/com/cym/controller/api/"), //
				SourceCodePath.path().setPath("src/main/java/com/cym/model/"), //
				SourceCodePath.path().setPath("src/main/java/com/cym/sqlhelper/"), //
				SourceCodePath.path().setPath("src/main/java/com/cym/utils/") //
		);

		SolonHtmlApiDocBuilder.buildApiDoc(config);

		// 最后加两句js,调整div高度
		File file = new File(DocGlobalConstants.HTML_DOC_OUT_PATH, "api.html");
		List<String> lines = FileUtil.readLines(file, Charset.forName("utf-8"));
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).contains("<div class=\"book-summary\">")) {
				lines.set(i, "<div class=\"book-summary\" id=\"menu\">");
			}

			if (lines.get(i).contains("</script>")) {
				lines.set(i, "var h = document.getElementById(\"book_iframe\").height -20; \n" //
						+ "document.getElementById(\"menu\").style.height = h + 'px'; \n"//
						+ "document.getElementById(\"menu\").style.overflow = 'auto'; \n" //
						+ "</script>");
				break;
			}

		}
		FileUtil.writeLines(lines, file, Charset.forName("utf-8"));

		System.out.println("生成完毕");
	}
}
