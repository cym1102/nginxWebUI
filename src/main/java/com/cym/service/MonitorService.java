package com.cym.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.ext.DiskInfo;
import com.cym.ext.MonitorInfo;
import com.cym.ext.NetworkInfo;
import com.cym.utils.OshiUtils;
import com.cym.utils.SystemTool;
import com.cym.utils.oshi.NetDomain;
import com.cym.utils.oshi.NetDomain.NetInterfaceDomain;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.system.oshi.OshiUtil;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;
import oshi.util.GlobalConfig;

/**
 * 获取系统信息的业务逻辑实现类.
 * 
 * @author amg * @version 1.0 Creation date: 2008-3-11 - 上午10:06:06
 */
@Component
public class MonitorService {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Init
	public void afterInjection() {
		if (SystemTool.isWindows()) {
			// oshi适配windows cpu占用率
			GlobalConfig.set(GlobalConfig.OSHI_OS_WINDOWS_CPU_UTILITY, true);
		}
	}

	public MonitorInfo getMonitorInfoOshi() {

		MonitorInfo infoBean = new MonitorInfo();
		try {
			infoBean.setCpuCount(OshiUtil.getProcessor().getPhysicalProcessorCount());
			infoBean.setThreadCount(OshiUtil.getProcessor().getLogicalProcessorCount());

			infoBean.setUsedMemory(FormatUtil.formatBytes(OshiUtil.getMemory().getTotal() - OshiUtil.getMemory().getAvailable()));
			infoBean.setTotalMemorySize(FormatUtil.formatBytes(OshiUtil.getMemory().getTotal()));

			infoBean.setCpuRatio(NumberUtil.decimalFormat("#.00", 100d - OshiUtil.getCpuInfo().getFree()) + "%");
			infoBean.setMemRatio(NumberUtil.decimalFormat("#.##%", NumberUtil.div(OshiUtil.getMemory().getTotal() - OshiUtil.getMemory().getAvailable(), OshiUtil.getMemory().getTotal())));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return infoBean;
	}

	public List<DiskInfo> getDiskInfo() {
		List<DiskInfo> list = new ArrayList<>();

		try {
			for (OSFileStore fs : OshiUtil.getOs().getFileSystem().getFileStores()) {
				DiskInfo diskInfo = new DiskInfo();

				diskInfo.setPath(fs.getMount());
				diskInfo.setUseSpace(FormatUtil.formatBytes(fs.getTotalSpace() - fs.getUsableSpace()));
				diskInfo.setTotalSpace(FormatUtil.formatBytes(fs.getTotalSpace()));
				if (fs.getTotalSpace() != 0) {
					diskInfo.setPercent(NumberUtil.decimalFormat("#.##%", NumberUtil.div(fs.getTotalSpace() - fs.getUsableSpace(), fs.getTotalSpace())));
				} else {
					diskInfo.setPercent(NumberUtil.decimalFormat("#.##%", 0));
				}

				list.add(diskInfo);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return list;
	}

	public NetworkInfo getNetworkInfo() {
		NetworkInfo networkInfo = new NetworkInfo();
		NetDomain netDomain = OshiUtils.getNetInfo();
		networkInfo.setSend(getNetUp(netDomain));
		networkInfo.setReceive(getNetDown(netDomain));
		networkInfo.setTime(DateUtil.format(new Date(), "HH:mm:ss"));

		return networkInfo;
	}

	private Double getNetUp(NetDomain netDomain) {
		Double uploadBps = 0d;
		if (netDomain != null) {
			for (NetInterfaceDomain netInterfaceDomain : netDomain.getNetList()) {
				uploadBps += netInterfaceDomain.getUploadBps();
			}
		}
		return uploadBps;
	}

	private Double getNetDown(NetDomain netDomain) {
		Double downloadBps = 0d;
		if (netDomain != null) {
			for (NetInterfaceDomain netInterfaceDomain : netDomain.getNetList()) {
				downloadBps += netInterfaceDomain.getDownloadBps();
			}
		}
		return downloadBps;
	}
}
