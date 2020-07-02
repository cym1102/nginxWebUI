package com.cym.ext;

import java.util.List;

import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;

public class UpstreamExt {
	Upstream upstream;
	List<UpstreamServer> upstreamServerList;
	String serverStr;
	String paramJson;
	
	
	public String getParamJson() {
		return paramJson;
	}

	public void setParamJson(String paramJson) {
		this.paramJson = paramJson;
	}

	public String getServerStr() {
		return serverStr;
	}

	public void setServerStr(String serverStr) {
		this.serverStr = serverStr;
	}

	public Upstream getUpstream() {
		return upstream;
	}

	public void setUpstream(Upstream upstream) {
		this.upstream = upstream;
	}

	public List<UpstreamServer> getUpstreamServerList() {
		return upstreamServerList;
	}

	public void setUpstreamServerList(List<UpstreamServer> upstreamServerList) {
		this.upstreamServerList = upstreamServerList;
	}

}
