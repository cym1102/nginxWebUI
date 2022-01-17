package com.cym;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import com.cym.utils.SystemTool;

public class TestUtils {

	@Test
	public void generate() throws FileNotFoundException, IOException, XmlPullParserException {

		// 查看jar包里面pom.properties版本号
		String jarPath = TestUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		jarPath = java.net.URLDecoder.decode(jarPath, "UTF-8");
		try {
			URL url = new URL("jar:file:" + jarPath + "!/META-INF/maven/com.hzcominfo.application.etl-settings/application-etl-settings/pom.properties");
			InputStream inputStream = url.openStream();
			Properties properties = new Properties();
			properties.load(inputStream);
			String version = properties.getProperty("version");
			System.out.println(version);
		} catch (Exception e) {
			e.printStackTrace();
			// 开发过程中查看pom.xml版本号
			MavenXpp3Reader reader = new MavenXpp3Reader();
			String basePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			if (SystemTool.isWindows() && basePath.startsWith("/")) {
				basePath = basePath.substring(1);
			}
			if (basePath.indexOf("/target/") != -1) {
				basePath = basePath.substring(0, basePath.indexOf("/target/"));
			}
			Model model = reader.read(new FileReader(new File(basePath + "\\pom.xml")));
			String version = model.getVersion();
			System.out.println(version);
		}

	}
}
