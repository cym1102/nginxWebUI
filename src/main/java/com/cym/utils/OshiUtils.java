package com.cym.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.utils.oshi.CpuDomain;
import com.cym.utils.oshi.DiskDomain;
import com.cym.utils.oshi.MemoryDomain;
import com.cym.utils.oshi.NetDomain;
import com.cym.utils.oshi.ServerDomain;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.GlobalConfig;

/**
 * <p>
 * 服务器信息工具类
 * </p>
 *
 * @author 皮锋
 * @custom.date 2020年3月3日 上午11:55:09
 */
public final class OshiUtils {
	private static final Logger LOG = LoggerFactory.getLogger(OshiUtils.class);
	/**
	 * 初始化oshi，并创建SystemInfo对象
	 */
	public static final SystemInfo SYSTEM_INFO = initOshi();

	/**
	 * <p>
	 * 初始化oshi
	 * </p>
	 *
	 * @return {@link SystemInfo}
	 * @author 皮锋
	 * @custom.date 2022/6/27 14:51
	 */
	private static SystemInfo initOshi() {
		// 全局配置属性
		GlobalConfig.set(GlobalConfig.OSHI_OS_WINDOWS_LOADAVERAGE, true);
		return new SystemInfo();
	}

	/**
	 * <p>
	 * 通过 oshi 获取服务器信息
	 * </p>
	 *
	 * @return {@link ServerDomain}
	 * @author 皮锋
	 * @custom.date 2022/6/2 17:23
	 */
	public static ServerDomain getOshiServerInfo() {
		ServerDomain serverDomain = new ServerDomain();
		serverDomain.setCpuDomain(getCpuInfo());
		serverDomain.setMemoryDomain(getMemoryInfo());
		serverDomain.setNetDomain(getNetInfo());
		serverDomain.setDiskDomain(getDiskInfo());
		return serverDomain;
	}

	public static void main(String[] args) {
		ServerDomain serverDomain = getOshiServerInfo();
		System.out.println(JSONUtil.toJsonPrettyStr(serverDomain));
	}

	/**
	 * <p>
	 * 获取Cpu信息
	 * </p>
	 *
	 * @return {@link CpuDomain}
	 * @author 皮锋
	 * @custom.date 2022/5/5 17:24
	 */
	public static CpuDomain getCpuInfo() {
		try {
			CentralProcessor processor = SYSTEM_INFO.getHardware().getProcessor();
			CentralProcessor.ProcessorIdentifier processorIdentifier = processor.getProcessorIdentifier();
			String model = processorIdentifier.getName();
			String vendor = processorIdentifier.getVendor();
			int logicalProcessorCount = processor.getLogicalProcessorCount();
			long[] currentFreq = processor.getCurrentFreq();
			long[][] prevTicks = processor.getProcessorCpuLoadTicks();
			// 休眠一秒
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.error("线程中断异常！", e);
			}
			long[][] ticks = processor.getProcessorCpuLoadTicks();
			// 创建返回对象
			CpuDomain cpuDomain = new CpuDomain();
			List<CpuDomain.CpuInfoDomain> cpuInfoDomains = new ArrayList<>();
			for (int cpu = 0; cpu < logicalProcessorCount; cpu++) {

				long user = ticks[cpu][TickType.USER.getIndex()] - prevTicks[cpu][TickType.USER.getIndex()];
				long nice = ticks[cpu][TickType.NICE.getIndex()] - prevTicks[cpu][TickType.NICE.getIndex()];
				long sys = ticks[cpu][TickType.SYSTEM.getIndex()] - prevTicks[cpu][TickType.SYSTEM.getIndex()];
				long idle = ticks[cpu][TickType.IDLE.getIndex()] - prevTicks[cpu][TickType.IDLE.getIndex()];
				long iowait = ticks[cpu][TickType.IOWAIT.getIndex()] - prevTicks[cpu][TickType.IOWAIT.getIndex()];
				long irq = ticks[cpu][TickType.IRQ.getIndex()] - prevTicks[cpu][TickType.IRQ.getIndex()];
				long softirq = ticks[cpu][TickType.SOFTIRQ.getIndex()] - prevTicks[cpu][TickType.SOFTIRQ.getIndex()];
				long steal = ticks[cpu][TickType.STEAL.getIndex()] - prevTicks[cpu][TickType.STEAL.getIndex()];
				long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

				// 设置每一个cpu的信息
				CpuDomain.CpuInfoDomain cpuInfoDomain = new CpuDomain.CpuInfoDomain();
				// CPU的总量MHz
				cpuInfoDomain.setCpuMhz((int) (currentFreq[cpu] / 1000000L));
				// 获得CPU的类别，如：Celeron
				cpuInfoDomain.setCpuModel(model);
				// 获得CPU的卖主，如：Intel
				cpuInfoDomain.setCpuVendor(vendor);
				// 用户使用率
				cpuInfoDomain.setCpuUser((double) user / (double) totalCpu);
				// 系统使用率
				cpuInfoDomain.setCpuSys((double) sys / (double) totalCpu);
				// 当前等待率
				cpuInfoDomain.setCpuWait((double) iowait / (double) totalCpu);
				// 当前错误率
				cpuInfoDomain.setCpuNice((double) nice / (double) totalCpu);
				// 当前空闲率
				cpuInfoDomain.setCpuIdle((double) idle / (double) totalCpu);
				// 总的使用率
				cpuInfoDomain.setCpuCombined((double) (user + sys) / (double) totalCpu);

				cpuInfoDomains.add(cpuInfoDomain);
			}
			cpuDomain.setCpuNum(logicalProcessorCount);
			cpuDomain.setCpuList(cpuInfoDomains);
			return cpuDomain;
		} catch (Throwable e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * <p>
	 * 获取内存信息
	 * </p>
	 *
	 * @return {@link MemoryDomain}
	 * @author 皮锋
	 * @custom.date 2022/5/29 10:44
	 */
	public static MemoryDomain getMemoryInfo() {
		try {
			GlobalMemory memory = SYSTEM_INFO.getHardware().getMemory();
			long memoryTotal = memory.getTotal();
			long memFree = memory.getAvailable();
			long memUsed = memoryTotal - memFree;
			MemoryDomain.MenDomain menDomain = new MemoryDomain.MenDomain();
			menDomain.setMemTotal(memoryTotal);
			// 实际内存使用量
			menDomain.setMemUsed(memUsed);
			// 实际内存剩余量
			menDomain.setMemFree(memFree);
			// 物理内存使用率
			menDomain.setMenUsedPercent(NumberUtil.round((double) memUsed / (double) memoryTotal, 4).doubleValue());
			VirtualMemory virtualMemory = memory.getVirtualMemory();
			// long swapTotal = virtualMemory.getVirtualMax();
			long swapTotal = virtualMemory.getSwapTotal();
			// long swapUsed = virtualMemory.getSwapTotal(); // 到底是哪个？
			// long swapUsed = virtualMemory.getSwapUsed() +
			// virtualMemory.getVirtualInUse(); // 到底是哪个？
			// long swapUsed = virtualMemory.getVirtualInUse(); // 到底是哪个？
			long swapUsed = virtualMemory.getSwapUsed();
			long swapFree = swapTotal - swapUsed;
			MemoryDomain.SwapDomain swapDomain = new MemoryDomain.SwapDomain();
			// 交换区总量
			swapDomain.setSwapTotal(swapTotal);
			// 交换区使用量
			swapDomain.setSwapUsed(swapUsed);
			// 交换区剩余量
			swapDomain.setSwapFree(swapFree);
			// 交换区使用率
			swapDomain.setSwapUsedPercent(swapTotal == 0 ? 0 : NumberUtil.round((double) swapUsed / (double) swapTotal, 4).doubleValue());

			MemoryDomain memoryDomain = new MemoryDomain();
			memoryDomain.setMenDomain(menDomain);
			memoryDomain.setSwapDomain(swapDomain);
			return memoryDomain;
		} catch (Throwable e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * <p>
	 * 获取网卡信息
	 * </p>
	 *
	 * @return {@link NetDomain}
	 * @author 皮锋
	 * @custom.date 2022/5/30 9:26
	 */
	public static NetDomain getNetInfo() {
		try {
			NetDomain netDomain = new NetDomain();
			List<NetDomain.NetInterfaceDomain> netInterfaceConfigDomains = new ArrayList<>();

			List<NetworkIF> networkIfsEnd = SYSTEM_INFO.getHardware().getNetworkIFs(true);
			for (NetworkIF net : networkIfsEnd) {
				NetDomain.NetInterfaceDomain netInterfaceDomain = new NetDomain.NetInterfaceDomain();
				// 网卡地址
				String[] iPv4addr = net.getIPv4addr();
				// 掩码长度
//				Short[] subnetMasks = net.getSubnetMasks();
				// MAC地址
				String macAddr = net.getMacaddr().toUpperCase();
				// 网卡名字
				String netName = net.getName();
				// 网卡描述信息
//				String displayName = net.getDisplayName();
				// 是否忽略此网卡
				if (ignore(iPv4addr, macAddr, netName)) {
					continue;
				}
				// 网速
				long start = System.currentTimeMillis();
				long rxBytesStart = net.getBytesRecv();
				long txBytesStart = net.getBytesSent();
				// 休眠一秒
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOG.error("线程中断异常！", e);
				}
				long end = System.currentTimeMillis();
				// 更新此接口上的接口网络统计信息
				net.updateAttributes();
				long rxBytesEnd = net.getBytesRecv();
				long txBytesEnd = net.getBytesSent();
				// 网卡配置
				netInterfaceDomain.setName(net.getName());
				netInterfaceDomain.setType("Ethernet");
				netInterfaceDomain.setAddress(iPv4addr[0]);
//				netInterfaceDomain.setMask(Ipv4Util.getMaskByMaskBit(subnetMasks[0]));
//				netInterfaceDomain.setBroadcast(Ipv4Util.getEndIpStr(iPv4addr[0], (int) subnetMasks[0]));
//				netInterfaceDomain.setHwAddr(macAddr);
//				netInterfaceDomain.setDescription(displayName);
//				// 网卡状态
//				netInterfaceDomain.setRxBytes(net.getBytesRecv());
//				netInterfaceDomain.setRxDropped(net.getInDrops());
//				netInterfaceDomain.setRxErrors(net.getInErrors());
//				netInterfaceDomain.setRxPackets(net.getPacketsRecv());
//				netInterfaceDomain.setTxBytes(net.getBytesSent());
//				netInterfaceDomain.setTxDropped(net.getCollisions());
//				netInterfaceDomain.setTxErrors(net.getOutErrors());
//				netInterfaceDomain.setTxPackets(net.getPacketsSent());

				// 1Byte=8bit
				double rxBps = (double) (rxBytesEnd - rxBytesStart) / ((double) (end - start) / 1000);
				double txBps = (double) (txBytesEnd - txBytesStart) / ((double) (end - start) / 1000);
				// 网速
				netInterfaceDomain.setDownloadBps(rxBps);
				netInterfaceDomain.setUploadBps(txBps);
				netInterfaceConfigDomains.add(netInterfaceDomain);
			}

			netDomain.setNetNum(netInterfaceConfigDomains.size());
			netDomain.setNetList(netInterfaceConfigDomains);
			return netDomain;
		} catch (Throwable e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * <p>
	 * 是否忽略此网卡
	 * </p>
	 *
	 * @param iPv4addr 网卡地址
	 * @param macAddr  MAC地址
	 * @param netName  网卡名字
	 * @return 是/否
	 * @author 皮锋
	 * @custom.date 2022/5/31 10:13
	 */
	private static boolean ignore(String[] iPv4addr, String macAddr, String netName) {
		return ArrayUtil.isEmpty(iPv4addr)
				// 127.0.0.1
				|| ArrayUtil.contains(iPv4addr, "127.0.0.1")
				// MAC地址不存在
				|| "00:00:00:00:00:00".equals(macAddr)
				// 0.0.0.0
				|| ArrayUtil.contains(iPv4addr, "0.0.0.0")
				// docker
				|| StrUtil.containsIgnoreCase(netName, "docker")
				// lo
				|| StrUtil.containsIgnoreCase(netName, "lo");
	}

	/**
	 * 文件系统类型名，比如本地硬盘、光驱、网络文件系统等
	 */
	private static final String TYPE_NAME = "local";

	/**
	 * <p>
	 * 获取磁盘信息
	 * </p>
	 *
	 * @return {@link DiskDomain}
	 * @author 皮锋
	 * @custom.date 2022/5/12 10:57
	 */
	public static DiskDomain getDiskInfo() {
		try {
			DiskDomain diskDomain = new DiskDomain();
			List<DiskDomain.DiskInfoDomain> diskInfoDomains = new ArrayList<>();

			FileSystem fileSystem = SYSTEM_INFO.getOperatingSystem().getFileSystem();
			// 如果为 true，则将列表筛选为仅本地文件存储
			List<OSFileStore> fsArray = fileSystem.getFileStores(true);
			for (OSFileStore fs : fsArray) {
				DiskDomain.DiskInfoDomain diskInfoDomain = new DiskDomain.DiskInfoDomain();

				diskInfoDomain.setDevName(fs.getName());
				diskInfoDomain.setDirName(fs.getMount());
				diskInfoDomain.setTypeName(TYPE_NAME);
				diskInfoDomain.setSysTypeName(fs.getType());
				// 总空间/容量
				long total = fs.getTotalSpace();
				// 可用空间
				long usable = fs.getUsableSpace();
				// 剩余空间
				long free = fs.getFreeSpace();
				// 已使用空间
				long used = total - usable;
				diskInfoDomain.setTotal(total);
				diskInfoDomain.setFree(free);
				diskInfoDomain.setUsed(used);
				diskInfoDomain.setAvail(usable);
				diskInfoDomain.setUsePercent(total == 0 ? 0 : NumberUtil.round((double) used / (double) total, 4).doubleValue());
				diskInfoDomains.add(diskInfoDomain);
			}
			diskDomain.setDiskInfoList(diskInfoDomains);
			diskDomain.setDiskNum(diskInfoDomains.size());
			return diskDomain;
		} catch (Throwable e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

}
