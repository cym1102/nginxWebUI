package com.cym.ext;

public class DiskInfo {
	String path;

	String useSpace;
	String totalSpace;

	String percent;
	
	
	
	public String getPercent() {
		return percent;
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


	public String getUseSpace() {
		return useSpace;
	}

	public void setUseSpace(String useSpace) {
		this.useSpace = useSpace;
	}

	public String getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(String totalSpace) {
		this.totalSpace = totalSpace;
	}

}
