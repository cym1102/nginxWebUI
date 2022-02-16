package com.cym.utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.noear.solon.annotation.Component;

import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.file.Tailer;
import cn.hutool.core.util.StrUtil;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

@Component
public class BLogFileTailer {
	// 定时过期map
	public Map<String, Tailer> tailerMap = ExpiringMap//
			.builder()//
			.expiration(20, TimeUnit.SECONDS)//
			.expirationPolicy(ExpirationPolicy.ACCESSED)//
			.expirationListener(new ExpirationListener<String, Tailer>() {
				@Override
				public void expired(String guid, Tailer tailer) {
					tailer.stop();
					tailer = null;
				}
			}).build();//

	public Map<String, Vector<String>> lineMap = ExpiringMap//
			.builder()//
			.expiration(20, TimeUnit.SECONDS)//
			.expirationPolicy(ExpirationPolicy.ACCESSED)//
			.build();//

	public String run(String guid, String path) {

		if (tailerMap.get(guid) == null) {
			Tailer tailer = new Tailer(new File(path), new LineHandler() {

				@Override
				public void handle(String line) {
					if (lineMap.get(guid) == null) {
						lineMap.put(guid, new Vector<String>());
					}

					lineMap.get(guid).add("<div>" + line + "</div>");
				}
			}, 50);
			tailer.start(true);

			tailerMap.put(guid, tailer);
		}

		List<String> list = lineMap.get(guid);
		if (list != null && list.size() > 0) {

			// 清除到500行
			while (list.size() > 500) {
				list.remove(0);
			}

			return StrUtil.join("", list);
		}
		return "";
	}

}
