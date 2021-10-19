package com.cym.ext;

import java.util.List;

import com.cym.model.Cert;
import com.cym.model.CertCode;

public class CertExt {
	Cert cert;
	
	List<CertCode> certCodes;

	public Cert getCert() {
		return cert;
	}

	public void setCert(Cert cert) {
		this.cert = cert;
	}

	public List<CertCode> getCertCodes() {
		return certCodes;
	}

	public void setCertCodes(List<CertCode> certCodes) {
		this.certCodes = certCodes;
	}
	
	
}
