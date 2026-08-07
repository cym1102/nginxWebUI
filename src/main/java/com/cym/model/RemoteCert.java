package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.Table;

/**
 * 证书与远程服务器的同步关联
 */
@Table
public class RemoteCert extends BaseModel {
    /** 远程服务器 id */
    String remoteId;
    /** 证书 id */
    String certId;

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }
}
