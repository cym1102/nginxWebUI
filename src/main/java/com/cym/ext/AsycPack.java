package com.cym.ext;

import java.util.List;

import com.cym.model.Basic;
import com.cym.model.Http;
import com.cym.model.Location;
import com.cym.model.Param;
import com.cym.model.Password;
import com.cym.model.Server;
import com.cym.model.Stream;
import com.cym.model.Template;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;

public class AsycPack {
	List<Basic> basicList;
	List<Http> httpList;
	List<Server> serverList;
	List<Location> locationList;
	List<Upstream> upstreamList;
	List<UpstreamServer> upstreamServerList;
	List<Stream> streamList;
	List<Template> templateList;
	List<Param> paramList;
	List<Password> passwordList;
	
	ConfExt confExt;

	String decompose;


	
	public List<Template> getTemplateList() {
		return templateList;
	}

	public void setTemplateList(List<Template> templateList) {
		this.templateList = templateList;
	}

	public List<Password> getPasswordList() {
		return passwordList;
	}

	public void setPasswordList(List<Password> passwordList) {
		this.passwordList = passwordList;
	}

	public List<Basic> getBasicList() {
		return basicList;
	}

	public void setBasicList(List<Basic> basicList) {
		this.basicList = basicList;
	}

	public List<Param> getParamList() {
		return paramList;
	}

	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}

	public String getDecompose() {
		return decompose;
	}

	public void setDecompose(String decompose) {
		this.decompose = decompose;
	}

	public ConfExt getConfExt() {
		return confExt;
	}

	public void setConfExt(ConfExt confExt) {
		this.confExt = confExt;
	}

	public List<Stream> getStreamList() {
		return streamList;
	}

	public void setStreamList(List<Stream> streamList) {
		this.streamList = streamList;
	}

	public List<Location> getLocationList() {
		return locationList;
	}

	public void setLocationList(List<Location> locationList) {
		this.locationList = locationList;
	}

	public List<Http> getHttpList() {
		return httpList;
	}

	public void setHttpList(List<Http> httpList) {
		this.httpList = httpList;
	}

	public List<Server> getServerList() {
		return serverList;
	}

	public void setServerList(List<Server> serverList) {
		this.serverList = serverList;
	}

	public List<Upstream> getUpstreamList() {
		return upstreamList;
	}

	public void setUpstreamList(List<Upstream> upstreamList) {
		this.upstreamList = upstreamList;
	}

	public List<UpstreamServer> getUpstreamServerList() {
		return upstreamServerList;
	}

	public void setUpstreamServerList(List<UpstreamServer> upstreamServerList) {
		this.upstreamServerList = upstreamServerList;
	}

}
