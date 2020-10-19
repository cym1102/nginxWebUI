package com.cym.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.StringTokenizer;

public class NetWorkUtil {
	private static final int SLEEP_TIME = 2 * 1000;

	// 获取网络上行下行速度
	public static Map<String, String> getNetworkDownUp() {
		Properties props = System.getProperties();
		String os = props.getProperty("os.name").toLowerCase();
		os = os.startsWith("win") ? "windows" : "linux";
		Map<String, String> result = new HashMap<>();
		Process pro = null;
		Runtime r = Runtime.getRuntime();
		BufferedReader input = null;
		String rxPercent = "";
		String txPercent = "";
		try {
			String command = "windows".equals(os) ? "netstat -e" : "ifconfig";
			pro = r.exec(command);
			input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			long result1[] = readInLine(input, "windows");
			Thread.sleep(SLEEP_TIME);
			pro.destroy();
			input.close();
			pro = r.exec(command);
			input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			long result2[] = readInLine(input, os);
			rxPercent = formatNumber((result2[0] - result1[0]) / (1024.0 * (SLEEP_TIME / 1000))); // 上行速率(kB/s)
			txPercent = formatNumber((result2[1] - result1[1]) / (1024.0 * (SLEEP_TIME / 1000))); // 下行速率(kB/s)
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Optional.ofNullable(pro).ifPresent(p -> p.destroy());
		}
		result.put("rxPercent", rxPercent);// 下行速率
		result.put("txPercent", txPercent);// 上行速率
		return result;

	}

	private static long[] readInLine(BufferedReader input, String osType) {
		long arr[] = new long[2];
		StringTokenizer tokenStat = null;
		try {
			if (osType.equals("linux")) { // 获取linux环境下的网口上下行速率
				long rx = 0, tx = 0;
				String line = null;
				// RX packets:4171603 errors:0 dropped:0 overruns:0 frame:0
				// TX packets:4171603 errors:0 dropped:0 overruns:0 carrier:0
				while ((line = input.readLine()) != null) {
					if (line.indexOf("RX packets") >= 0) {
						rx += Long.parseLong(line.substring(line.indexOf("RX packets") + 11, line.indexOf(" ", line.indexOf("RX packets") + 11)));
					} else if (line.indexOf("TX packets") >= 0) {
						tx += Long.parseLong(line.substring(line.indexOf("TX packets") + 11, line.indexOf(" ", line.indexOf("TX packets") + 11)));
					}
				}
				arr[0] = rx;
				arr[1] = tx;
			} else { // 获取windows环境下的网口上下行速率
				input.readLine();
				input.readLine();
				input.readLine();
				input.readLine();
				tokenStat = new StringTokenizer(input.readLine());
				tokenStat.nextToken();
				arr[0] = Long.parseLong(tokenStat.nextToken());
				arr[1] = Long.parseLong(tokenStat.nextToken());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}

	private static String formatNumber(double f) {
		return new Formatter().format("%.2f", f).toString();
	}

	public static void main(String[] args) {
		Map<String, String> result = getNetworkDownUp();
		System.out.println(result.get("rxPercent"));
		System.out.println(result.get("txPercent"));

	}
}
