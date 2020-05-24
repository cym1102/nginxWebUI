package com.cym.ext;

import java.util.List;

import com.cym.model.Location;
import com.cym.model.Server;
import com.cym.model.Upstream;

public class ServerExt {
	Server server;
	List<Location> locationList;
	String locationStr;
	String paramJson;
	
	
	
	public String getParamJson() {
		return paramJson;
	}

	public void setParamJson(String paramJson) {
		this.paramJson = paramJson;
	}

	public String getLocationStr() {
		return locationStr;
	}

	public void setLocationStr(String locationStr) {
		this.locationStr = locationStr;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public List<Location> getLocationList() {
		return locationList;
	}

	public void setLocationList(List<Location> locationList) {
		this.locationList = locationList;
	}



}
