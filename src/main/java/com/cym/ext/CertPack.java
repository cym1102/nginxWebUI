package com.cym.ext;

/**
 * 证书同步传输载荷，pem/key 为 Base64 编码内容
 */
public class CertPack {
    String domain;
    String pem;
    String key;
    String encryption;
    Long makeTime;
    Long endTime;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPem() {
        return pem;
    }

    public void setPem(String pem) {
        this.pem = pem;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public Long getMakeTime() {
        return makeTime;
    }

    public void setMakeTime(Long makeTime) {
        this.makeTime = makeTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
