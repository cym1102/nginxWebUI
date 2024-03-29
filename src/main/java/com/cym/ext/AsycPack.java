package com.cym.ext;

import java.util.List;

import com.cym.model.Basic;
import com.cym.model.Cert;
import com.cym.model.CertCode;
import com.cym.model.DenyAllow;
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
	
	List<Cert> certList;
	List<CertCode> certCodeList;
	
	List<DenyAllow> denyAllowList;
	
	String acmeZip;
	
	String certZip;
	
	
	
	public List<DenyAllow> getDenyAllowList() {
		return denyAllowList;
	}

	public void setDenyAllowList(List<DenyAllow> denyAllowList) {
		this.denyAllowList = denyAllowList;
	}

	public String getCertZip() {
		return certZip;
	}

	public void setCertZip(String certZip) {
		this.certZip = certZip;
	}

	public String getAcmeZip() {
		return acmeZip;
	}

	public void setAcmeZip(String acmeZip) {
		this.acmeZip = acmeZip;
	}

	public List<Cert> getCertList() {
		return certList;
	}

	public void setCertList(List<Cert> certList) {
		this.certList = certList;
	}

	public List<CertCode> getCertCodeList() {
		return certCodeList;
	}

	public void setCertCodeList(List<CertCode> certCodeList) {
		this.certCodeList = certCodeList;
	}

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

//	public String getDecompose() {
//		return decompose;
//	}
//
//	public void setDecompose(String decompose) {
//		this.decompose = decompose;
//	}
//
//	public ConfExt getConfExt() {
//		return confExt;
//	}
//
//	public void setConfExt(ConfExt confExt) {
//		this.confExt = confExt;
//	}

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
