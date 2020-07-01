package com.cym.service;

import java.io.DataOutput;
import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.cym.model.MonitorInfo;
import com.sun.management.OperatingSystemMXBean;

import cn.hutool.core.util.NumberUtil;

/** */
/**
 * 获取系统信息的业务逻辑实现类.
 * 
 * @author amg * @version 1.0 Creation date: 2008-3-11 - 上午10:06:06
 */
@Service
public class MonitorService {

	OperatingSystemMXBean osmxb;

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
		Double gb = Double.valueOf(1024 * 1024 * 1024);

		// 总的物理内存
		Double totalMemorySize = osmxb.getTotalPhysicalMemorySize() / gb;
		// 剩余的物理内存
		Double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / gb;
		// 已使用的物理内存
		Double usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / gb;

		double cpu = osmxb.getSystemCpuLoad();

		// 构造返回对象
		MonitorInfo infoBean = new MonitorInfo();
		infoBean.setFreePhysicalMemorySize(NumberUtil.decimalFormat("#.00GB", freePhysicalMemorySize)  );
		infoBean.setTotalMemorySize(NumberUtil.decimalFormat("#.00GB", totalMemorySize));
		infoBean.setUsedMemory(NumberUtil.decimalFormat("#.00GB", usedMemory));
		infoBean.setCpuRatio(NumberUtil.decimalFormat("#.##%", cpu));
		return infoBean;
	}

	/** */
	/**
	 * 测试方法.
	 * 
	 * @param args
	 * @throws Exception
	 * @author amg * Creation date: 2008-4-30 - 下午04:47:29
	 */
	public static void main(String[] args) throws Exception {

		MonitorService service = new MonitorService();
		MonitorInfo monitorInfo = service.getMonitorInfo();

		System.out.println("cpu占有率=" + monitorInfo.getCpuRatio());

		System.out.println("总的物理内存=" + monitorInfo.getTotalMemorySize() + "gb");
		System.out.println("已使用的物理内存=" + monitorInfo.getUsedMemory() + "gb");
		System.out.println("剩余物理内存=" + monitorInfo.getFreePhysicalMemorySize() + "gb");
	}
}
