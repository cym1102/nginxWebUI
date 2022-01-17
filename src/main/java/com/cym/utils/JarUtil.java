package com.cym.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class JarUtil {
	public static String getCurrentFilePath() {
		ProtectionDomain protectionDomain = JarUtil.class.getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		URI location = null;
		try {
			location = (codeSource == null ? null : codeSource.getLocation().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String path = (location == null ? null : location.getSchemeSpecificPart());
		if (path == null) {
			throw new IllegalStateException("Unable to determine code source archive");
		}
		File root = new File(path);
		if (!root.exists()) {
			throw new IllegalStateException("Unable to determine code source archive from " + root);
		}
		return root.getAbsolutePath();
	}

	public static File getCurrentFile() {
		return new File(getCurrentFilePath());
	}

	public static void main(String[] args) {
		System.out.println(getCurrentFilePath());
	}
}
