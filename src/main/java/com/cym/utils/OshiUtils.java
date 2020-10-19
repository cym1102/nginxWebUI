package com.cym.utils;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

@Component
public class OshiUtils {
	HardwareAbstractionLayer hal;

	@PostConstruct
	public void init() {
		SystemInfo si = new SystemInfo();
		hal = si.getHardware();
	}

}
