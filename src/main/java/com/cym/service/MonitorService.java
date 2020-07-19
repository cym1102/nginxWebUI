package com.cym.service;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.cym.model.MonitorInfo;
import com.cym.utils.SystemTool;
import com.sun.management.OperatingSystemMXBean;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

/** */
/**
 * 获取系统信息的业务逻辑实现类.
 * 
 * @author amg * @version 1.0 Creation date: 2008-3-11 - 上午10:06:06
 */
@Service
public class MonitorService {

	OperatingSystemMXBean osmxb;
	Double gb = Double.valueOf(1024 * 1024 * 1024);

	@PostConstruct
	private void init() {
		osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	}

	/** */
	/**
	 * 获得当前的监控对象.
	 * 
	 * @return 返回构造好的监控对象
	 * @throws Exception
	 * @author amg * Creation date: 2008-4-25 - 上午10:45:08
	 */
	public MonitorInfo getMonitorInfo() {
		// 总的物理内存
		Double totalMemorySize = osmxb.getTotalPhysicalMemorySize() / gb;

		// 剩余的物理内存
		Long freePhysicalMemorySize = 0l;
		if (SystemTool.isWindows()) {
			freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
		} else {
			freePhysicalMemorySize = getLinuxFreeMem();
		}

		// 已使用的物理内存
		Double usedMemory = (osmxb.getTotalPhysicalMemorySize() - freePhysicalMemorySize) / gb;

		Double mem = usedMemory / totalMemorySize;
		Double cpu = osmxb.getSystemCpuLoad();

		// 构造返回对象
		MonitorInfo infoBean = new MonitorInfo();
		infoBean.setFreePhysicalMemorySize(NumberUtil.decimalFormat("0.00GB", freePhysicalMemorySize));
		infoBean.setTotalMemorySize(NumberUtil.decimalFormat("0.00GB", totalMemorySize));
		infoBean.setUsedMemory(NumberUtil.decimalFormat("0.00GB", usedMemory));
		infoBean.setCpuRatio(NumberUtil.decimalFormat("#.##%", cpu));
		infoBean.setMemRatio(NumberUtil.decimalFormat("#.##%", mem));
		infoBean.setCpuCount(osmxb.getAvailableProcessors());

		return infoBean;
	}


	private Long getLinuxFreeMem() {
		try {
			String line = RuntimeUtil.execForStr("cat /proc/meminfo");

			if (StrUtil.isNotEmpty(line)) {
				String[] lines = line.split("\n");
				for (String rs : lines) {
					if (rs.contains("MemAvailable")) {
						return Long.parseLong(rs.replace("MemAvailable:", "").replace("kB", "").trim()) * 1024;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return osmxb.getFreePhysicalMemorySize();
	}

}
