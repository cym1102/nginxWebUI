package com.cym.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import cn.hutool.core.io.FileUtil;

public class MyZipUtils {

	public static void unzip(String fromZip, String toPath) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(new File(fromZip));
		byte[] buffer = new byte[4096];
		ZipEntry entry;
		Enumeration<? extends ZipEntry> entries = zipFile.entries(); // 获取全部文件的迭代器
		InputStream inputStream = null;
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			if (entry.isDirectory()) {
				continue;
			}

			String name = entry.getName();
			if (SystemTool.isWindows()) {
				name = name.replace("*", "_");
			}
			File outputFile = new File(toPath + File.separator + name);

			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}

			inputStream = zipFile.getInputStream(entry);
			try (FileOutputStream fos = new FileOutputStream(outputFile)) {
				while (inputStream.read(buffer) > 0) {
					fos.write(buffer);
				}
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			inputStream.close();
		}

		zipFile.close();

	}

	public static void main(String[] args) {
		try {
			MyZipUtils.unzip("d:/acme.zip", "d:/acme/");
			FileUtil.del("d:/acme.zip");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
