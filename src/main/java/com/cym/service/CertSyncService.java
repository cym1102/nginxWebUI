package com.cym.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.HomeConfig;
import com.cym.controller.adminPage.ConfController;
import com.cym.ext.CertPack;
import com.cym.model.Cert;
import com.cym.model.Remote;
import com.cym.model.RemoteCert;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.JsonResult;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/**
 * 证书同步到远程服务器的编排服务
 */
@Component
public class CertSyncService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    SqlHelper sqlHelper;
    @Inject
    HomeConfig homeConfig;
    @Inject
    ConfController confController;

    /**
     * 源端：把指定证书推送到指定远程服务器
     */
    public JsonResult syncCertToRemote(String remoteId, String certId) {
        Remote remote = sqlHelper.findById(remoteId, Remote.class);
        if (remote == null) {
            return renderError("远程服务器不存在");
        }
        Cert cert = sqlHelper.findById(certId, Cert.class);
        if (cert == null) {
            return renderError("证书不存在");
        }
        if (StrUtil.isEmpty(cert.getPem()) || StrUtil.isEmpty(cert.getKey())
                || !FileUtil.exist(cert.getPem()) || !FileUtil.exist(cert.getKey())) {
            return renderError("证书文件未找到：" + cert.getDomain());
        }

        CertPack pack = new CertPack();
        pack.setDomain(cert.getDomain());
        pack.setPem(Base64.encode(FileUtil.readBytes(cert.getPem())));
        pack.setKey(Base64.encode(FileUtil.readBytes(cert.getKey())));
        pack.setEncryption(cert.getEncryption());
        pack.setMakeTime(cert.getMakeTime());
        pack.setEndTime(cert.getEndTime());

        try {
            cn.hutool.http.HttpResponse response = cn.hutool.http.HttpRequest.post(
                    remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort()
                            + "/adminPage/remote/receiveCert?creditKey=" + remote.getCreditKey())
                    .form("json", JSONUtil.toJsonStr(pack))
                    .timeout(30000)
                    .execute();
            int status = response.getStatus();
            String body = response.body();
            if (status == 302 || status == 401 || !JSONUtil.isJson(body)) {
                return renderError("远端授权失败，请重新获取 creditKey");
            }
            return JSONUtil.toBean(body, JsonResult.class);
        } catch (Exception e) {
            logger.error("证书同步到远程失败，certId={}, remoteId={}", certId, remoteId, e);
            return renderError("同步失败：" + e.getMessage());
        }
    }

    /**
     * 远端：接收证书并落地（upsert + 写文件 + reload）
     */
    public JsonResult applyCertPack(CertPack pack) {
        if (pack == null || StrUtil.isEmpty(pack.getDomain())) {
            return renderError("证书数据为空");
        }
        try {
            // 按域名 upsert
            List<Cert> existList = sqlHelper.findListByQuery(
                    new ConditionAndWrapper().eq(Cert::getDomain, pack.getDomain()), Cert.class);
            Cert cert;
            if (existList != null && !existList.isEmpty()) {
                cert = existList.get(0);
                if (cert.getType() != null && cert.getType() != 3) {
                    return renderError("远端已存在同名且非同步来源的证书，拒绝覆盖");
                }
            } else {
                cert = new Cert();
                cert.setDomain(pack.getDomain());
                cert.setType(3); // 同步证书
                cert.setAutoRenew(0);
            }
            cert.setEncryption(pack.getEncryption());
            cert.setMakeTime(pack.getMakeTime());
            cert.setEndTime(pack.getEndTime());

            // 写文件到 home/cert/<安全域名>/
            String safeDomain = pack.getDomain().replaceAll("[^A-Za-z0-9._-]", "_");
            if (safeDomain.contains("..")) {
                return renderError("非法域名");
            }
            String dir = homeConfig.home + "cert" + File.separator + safeDomain + File.separator;
            java.io.File dirFile = new java.io.File(dir);
            FileUtil.mkdir(dirFile);
            // 断言规范化路径仍在 home/cert 下
            String canonBase = new java.io.File(homeConfig.home + "cert").getCanonicalPath();
            String canonDir = dirFile.getCanonicalPath();
            if (!canonDir.startsWith(canonBase + File.separator)) {
                return renderError("非法域名");
            }
            String pemPath = dir + safeDomain + ".pem";
            String keyPath = dir + safeDomain + ".key";
            FileUtil.writeBytes(Base64.decode(pack.getPem()), pemPath);
            FileUtil.writeBytes(Base64.decode(pack.getKey()), keyPath);
            cert.setPem(pemPath);
            cert.setKey(keyPath);

            if (cert.getId() == null) {
                sqlHelper.insert(cert);
            } else {
                sqlHelper.updateById(cert);
            }

            // 刷新 nginx
            try {
                confController.reload(null, null, null);
            } catch (Exception e) {
                logger.error("远端 nginx reload 失败", e);
                return renderSuccess("证书已更新，但 nginx reload 失败：" + e.getMessage());
            }
            return renderSuccess("同步成功");
        } catch (Exception e) {
            logger.error("远端接收证书失败", e);
            return renderError("接收失败：" + e.getMessage());
        }
    }

    /**
     * 续期钩子：把证书同步到所有关联的远程服务器，单点失败不影响其余
     */
    public void syncCertToAllRemotes(String certId) {
        List<RemoteCert> list = findRemoteCertsByCert(certId);
        for (RemoteCert rc : list) {
            try {
                JsonResult result = syncCertToRemote(rc.getRemoteId(), certId);
                if (!result.isSuccess()) {
                    logger.error("续期后同步证书失败，certId={}, remoteId={}, msg={}", certId, rc.getRemoteId(), result.getMsg());
                }
            } catch (Exception e) {
                logger.error("续期后同步证书失败，certId={}, remoteId={}", certId, rc.getRemoteId(), e);
            }
        }
    }

    public List<RemoteCert> findRemoteCertsByCert(String certId) {
        return sqlHelper.findListByQuery(
                new ConditionAndWrapper().eq(RemoteCert::getCertId, certId), RemoteCert.class);
    }

    public List<String> findCertIdsByRemote(String remoteId) {
        List<RemoteCert> list = sqlHelper.findListByQuery(
                new ConditionAndWrapper().eq(RemoteCert::getRemoteId, remoteId), RemoteCert.class);
        List<String> ids = new ArrayList<>();
        for (RemoteCert rc : list) {
            ids.add(rc.getCertId());
        }
        return ids;
    }

    private JsonResult renderSuccess(String msg) {
        JsonResult r = new JsonResult();
        r.setSuccess(true);
        r.setStatus("200");
        r.setMsg(msg);
        return r;
    }

    private JsonResult renderError(String msg) {
        JsonResult r = new JsonResult();
        r.setSuccess(false);
        r.setStatus("500");
        r.setMsg(msg);
        return r;
    }
}
