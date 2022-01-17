package com.cym.ext;

import java.util.List;
import java.util.Map;

/**
 * 监视信息的JavaBean类.
 * 
 * @author amg
 * @version 1.0 Creation date: 2008-4-25 - 上午10:37:00
 */
public class MonitorInfo {

	/** 总的物理内存. */
	private String totalMemorySize;

//	/** 剩余的物理内存. */
//	private String freePhysicalMemorySize;

	/** 已使用的物理内存. */
	private String usedMemory;

	/** cpu使用率. */
	private String cpuRatio;
	/** 内存使用率. */
	private String memRatio;

	/**
	 * cpu数量
	 */
	private Integer cpuCount;

	/**
	 * 线程数量
	 */
	private Integer threadCount;
	
	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	public Integer getCpuCount() {
		return cpuCount;
	}

	public void setCpuCount(Integer cpuCount) {
		this.cpuCount = cpuCount;
	}

	public String getMemRatio() {
		return memRatio;
	}

	public void setMemRatio(String memRatio) {
		this.memRatio = memRatio;
	}

	public String getTotalMemorySize() {
		return totalMemorySize;
	}

	public void setTotalMemorySize(String totalMemorySize) {
		this.totalMemorySize = totalMemorySize;
	}

//	public String getFreePhysicalMemorySize() {
//		return freePhysicalMemorySize;
//	}
//
//	public void setFreePhysicalMemorySize(String freePhysicalMemorySize) {
//		this.freePhysicalMemorySize = freePhysicalMemorySize;
//	}

	public String getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(String usedMemory) {
		this.usedMemory = usedMemory;
	}

	public String getCpuRatio() {
		return cpuRatio;
	}

	public void setCpuRatio(String cpuRatio) {
		this.cpuRatio = cpuRatio;
	}

}