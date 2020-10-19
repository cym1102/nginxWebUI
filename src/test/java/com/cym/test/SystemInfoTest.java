package com.cym.test;

import java.util.Arrays;
import java.util.List;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;

/**
 * Java系统监控测试类
 * 
 * @ClassName: SystemInfoTest
 * @Description:TODO(这里用一句话描述这个类的作用)
 * @author: 哒哒
 * @date: 2018年3月1日 下午5:33:51
 * 
 * @Copyright: 2018 www.sundablog.com Inc. All rights reserved.
 */
public class SystemInfoTest {

	public static void main(String[] args) {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();

		printProcessor(hal.getProcessor());
		printMemory(hal.getMemory());
		printFileSystem(si.getOperatingSystem().getFileSystem());

		printNetworkInterfaces(hal.getNetworkIFs());
		printNetworkParameters(si.getOperatingSystem().getNetworkParams());
	}

	private static void printProcessor(CentralProcessor processor) {
		System.out.println(" " + processor.getPhysicalPackageCount() + " physical CPU package(s)");
		System.out.println(" " + processor.getPhysicalProcessorCount() + " physical CPU core(s)");
		System.out.println(" " + processor.getLogicalProcessorCount() + " logical CPU(s)");
		System.out.println(" ");
	}

	private static void printMemory(GlobalMemory memory) {
		System.out.println("Memory: " + FormatUtil.formatBytes(memory.getAvailable()) + "/" + FormatUtil.formatBytes(memory.getTotal()));
//		System.out.println("Swap used: " + FormatUtil.formatBytes(memory.getSwapUsed()) + "/" + FormatUtil.formatBytes(memory.getSwapTotal()));

		System.out.println(" ");
	}

	private static void printFileSystem(FileSystem fileSystem) {
		System.out.println("File System:");

		System.out.format(" File Descriptors: %d/%d%n", fileSystem.getOpenFileDescriptors(), fileSystem.getMaxFileDescriptors());

		List<OSFileStore> fsArray = fileSystem.getFileStores();
		for (OSFileStore fs : fsArray) {
			long usable = fs.getUsableSpace();
			long total = fs.getTotalSpace();
			System.out.format(" %s (%s) [%s] %s of %s free (%.1f%%) is %s " + (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s") + " and is mounted at %s%n",
					fs.getName(), fs.getDescription().isEmpty() ? "file system" : fs.getDescription(), fs.getType(), FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace()),
					100d * usable / total, fs.getVolume(), fs.getLogicalVolume(), fs.getMount());
		}

		System.out.println(" ");
	}

	private static void printNetworkInterfaces(List<NetworkIF> networkIFs) {
		System.out.println("Network interfaces:");
		for (NetworkIF net : networkIFs) {
			System.out.format(" Name: %s (%s)%n", net.getName(), net.getDisplayName());
			System.out.format("   MAC Address: %s %n", net.getMacaddr());
			System.out.format("   MTU: %s, Speed: %s %n", net.getMTU(), FormatUtil.formatValue(net.getSpeed(), "bps"));
			System.out.format("   IPv4: %s %n", Arrays.toString(net.getIPv4addr()));
			System.out.format("   IPv6: %s %n", Arrays.toString(net.getIPv6addr()));
			boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0 || net.getPacketsSent() > 0;
			System.out.format("   Traffic: received %s/%s%s; transmitted %s/%s%s %n", hasData ? net.getPacketsRecv() + " packets" : "?", hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
					hasData ? " (" + net.getInErrors() + " err)" : "", hasData ? net.getPacketsSent() + " packets" : "?", hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?",
					hasData ? " (" + net.getOutErrors() + " err)" : "");
		}

		System.out.println(" ");
	}

	private static void printNetworkParameters(NetworkParams networkParams) {
		System.out.println("Network parameters:");
		System.out.format(" Host name: %s%n", networkParams.getHostName());
		System.out.format(" Domain name: %s%n", networkParams.getDomainName());
		System.out.format(" DNS servers: %s%n", Arrays.toString(networkParams.getDnsServers()));
		System.out.format(" IPv4 Gateway: %s%n", networkParams.getIpv4DefaultGateway());
		System.out.format(" IPv6 Gateway: %s%n", networkParams.getIpv6DefaultGateway());

		System.out.println(" ");
	}

}