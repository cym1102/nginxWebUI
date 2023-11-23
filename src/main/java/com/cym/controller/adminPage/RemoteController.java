package com.cym.controller.adminPage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.VersionConfig;
import com.cym.controller.api.NginxApiController;
import com.cym.ext.AsycPack;
import com.cym.ext.Tree;
import com.cym.model.Admin;
import com.cym.model.Group;
import com.cym.model.Remote;
import com.cym.service.ConfService;
import com.cym.service.GroupService;
import com.cym.service.RemoteService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.NginxUtils;
import com.cym.utils.SystemTool;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/remote")
public class RemoteController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	RemoteService remoteService;
	@Inject
	SettingService settingService;
	@Inject
	ConfService confService;
	@Inject
	GroupService groupService;
	@Inject
	ConfController confController;
	@Inject
	MainController mainController;
	@Inject
	NginxApiController nginxApiController;
	@Inject
	VersionConfig versionConfig;
	@Inject("${server.port}")
	Integer port;

	@Mapping("version")
	public Map<String, Object> version() {
		Map<String, Object> map = new HashMap<>();
		map.put("version", versionConfig.currentVersion);

		if (NginxUtils.isRun()) {
			map.put("nginx", 1);
		} else {
			map.put("nginx", 0);
		}

		return map;
	}

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {

		JsonResult<List<String>> jsonResult = nginxApiController.getNginxStartCmd();
		modelAndView.put("startCmds", jsonResult.getObj());

		jsonResult = nginxApiController.getNginxStopCmd();
		modelAndView.put("stopCmds", jsonResult.getObj());

		modelAndView.put("projectVersion", versionConfig.currentVersion);
		modelAndView.view("/adminPage/remote/index.html");

		return modelAndView;
	}

	@Mapping("allTable")
	public List<Remote> allTable(Context context) {
		Admin admin = getAdmin();
		List<Remote> remoteList = sqlHelper.findAll(Remote.class);

		for (Remote remote : remoteList) {
			remote.setStatus(0);
			remote.setType(0);
			if (remote.getParentId() == null) {
				remote.setParentId("");
			}

			remote.setSelect(false);
			if (context.session("localType").equals("remote")) {
				Remote remoteSession = (Remote) context.session("remote");
				if (remoteSession.getId().equals(remote.getId())) {
					remote.setSelect(true);
				}
			}

			try {
				String json = HttpUtil.get(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/remote/version?creditKey=" + remote.getCreditKey(), 1000);
				if (StrUtil.isNotEmpty(json)) {
					Map<String, Object> map = JSONUtil.toBean(json, new TypeReference<Map<String, Object>>() {
					}.getType(), false);

					remote.setStatus(1);
					remote.setVersion((String) map.get("version"));
					remote.setNginx((Integer) map.get("nginx"));
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		}

		Remote remoteLocal = new Remote();
		remoteLocal.setId("local");
		remoteLocal.setIp("");
		remoteLocal.setProtocol("");
		remoteLocal.setParentId("");
		remoteLocal.setDescr(m.get("remoteStr.local"));
		Map<String, Object> map = version();
		remoteLocal.setVersion((String) map.get("version"));
		remoteLocal.setNginx((Integer) map.get("nginx"));
		remoteLocal.setPort(port);
		remoteLocal.setStatus(1);
		remoteLocal.setType(0);
		remoteLocal.setMonitor(settingService.get("monitorLocal") != null ? Integer.parseInt(settingService.get("monitorLocal")) : 0);
		remoteLocal.setSystem(SystemTool.getSystem());
		remoteLocal.setSelect(false);
		if (context.session("localType").equals("local")) {
			remoteLocal.setSelect(true);
		}

		remoteList.add(0, remoteLocal);

		List<Group> groupList = remoteService.getGroupByAdmin(admin);
		for (Group group : groupList) {
			Remote remoteGroup = new Remote();
			remoteGroup.setDescr(group.getName());
			remoteGroup.setId(group.getId());
			remoteGroup.setParentId(checkParent(group.getParentId(), groupList));
			remoteGroup.setType(1);

			remoteGroup.setIp("");
			remoteGroup.setProtocol("");
			remoteGroup.setVersion("");
			remoteGroup.setSystem("");

			remoteList.add(remoteGroup);
		}

		return remoteList;
	}

	private String checkParent(String parentId, List<Group> groupList) {

		if (parentId == null) {
			return "";
		}

		for (Group group : groupList) {
			if (group.getId().equals(parentId)) {
				return parentId;
			}
		}

		return "";
	}

	@Mapping("addGroupOver")
	public JsonResult addGroupOver(Group group) {
		if (StrUtil.isNotEmpty(group.getParentId()) && StrUtil.isNotEmpty(group.getId()) && group.getId().equals(group.getParentId())) {
			return renderError(m.get("remoteStr.parentGroupMsg"));
		}
		sqlHelper.insertOrUpdate(group);

		return renderSuccess();
	}

	@Mapping("groupDetail")
	public JsonResult groupDetail(String id) {
		return renderSuccess(sqlHelper.findById(id, Group.class));
	}

	@Mapping("delGroup")
	public JsonResult delGroup(String id) {

		groupService.delete(id);
		return renderSuccess();
	}

	@Mapping("getGroupTree")
	public JsonResult getGroupTree() {
		Admin admin = getAdmin();

		List<Tree> treeList = new ArrayList<>();
		Tree tree = new Tree();
		tree.setName(m.get("remoteStr.noGroup"));
		tree.setValue("");
		treeList.add(0, tree);

		List<Group> groups = remoteService.getGroupByAdmin(admin);
		fillTree(groups, treeList);

		return renderSuccess(treeList);
	}

	public void fillTree(List<Group> groups, List<Tree> treeList) {
		for (Group group : groups) {
			if (!hasParentIn(group.getParentId(), groups)) {
				Tree tree = new Tree();
				tree.setName(group.getName());
				tree.setValue(group.getId());

				List<Tree> treeSubList = new ArrayList<>();
				fillTree(groupService.getListByParent(group.getId()), treeSubList);
				tree.setChildren(treeSubList);

				treeList.add(tree);
			}
		}

	}

	private boolean hasParentIn(String parentId, List<Group> groups) {
		for (Group group : groups) {
			if (group.getId().equals(parentId)) {
				return true;
			}
		}
		return false;
	}

	@Mapping("getCmdRemote")
	public JsonResult getCmdRemote() {
		Admin admin = getAdmin();

		List<Group> groups = remoteService.getGroupByAdmin(admin);
		List<Remote> remotes = remoteService.getListByParent(null);

		List<Tree> treeList = new ArrayList<>();
		fillTreeRemote(groups, remotes, treeList);

		Tree tree = new Tree();
		tree.setName(m.get("remoteStr.local"));
		tree.setValue("local");

		treeList.add(0, tree);

		return renderSuccess(treeList);
	}

	private void fillTreeRemote(List<Group> groups, List<Remote> remotes, List<Tree> treeList) {
		for (Group group : groups) {
			if (!hasParentIn(group.getParentId(), groups)) {
				Tree tree = new Tree();
				tree.setName(group.getName());
				tree.setValue(group.getId());

				List<Tree> treeSubList = new ArrayList<>();

				fillTreeRemote(groupService.getListByParent(group.getId()), remoteService.getListByParent(group.getId()), treeSubList);

				tree.setChildren(treeSubList);

				treeList.add(tree);
			}
		}

		for (Remote remote : remotes) {
			Tree tree = new Tree();
			tree.setName(remote.getIp() + "【" + remote.getDescr() + "】");
			tree.setValue(remote.getId());

			treeList.add(tree);
		}

	}

	@Mapping("cmdOver")
	public JsonResult cmdOver(String[] remoteId, String cmd, Integer interval) {
		if (remoteId == null || remoteId.length == 0) {
			return renderSuccess(m.get("remoteStr.noSelect"));
		}

		StringBuilder rs = new StringBuilder();
		for (String id : remoteId) {
			JsonResult jsonResult = null;
			if (id.equals("local")) {
				if (cmd.contentEquals("check")) {
					jsonResult = confController.checkBase();
				}
				if (cmd.contentEquals("reload")) {
					jsonResult = confController.reload(null, null, null);
				}
				if (cmd.contentEquals("replace")) {
					jsonResult = confController.replace(confController.getReplaceJson(), null);
				}
				if (cmd.startsWith("startNginx") || cmd.startsWith("stopNginx")) {
					jsonResult = confController.runCmd(cmd.replaceFirst("startNginx ", "").replaceFirst("stopNginx ", ""), null);
				}
				if (cmd.contentEquals("update")) {
					jsonResult = renderError(m.get("remoteStr.notAllow"));
				}
				rs.append("<span class='blue'>").append(m.get("remoteStr.local")).append("> </span>");
			} else {
				Remote remote = sqlHelper.findById(id, Remote.class);
				rs.append("<span class='blue'>").append(remote.getIp()).append(":").append(remote.getPort()).append("> </span>");

				if (cmd.contentEquals("check")) {
					cmd = "checkBase";
				}

				try {
					String action = cmd;
					if (cmd.startsWith("startNginx") || cmd.startsWith("stopNginx")) {
						action = "runCmd";
					}

					String url = remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/conf/" + action + "?creditKey=" + remote.getCreditKey();

					Map<String, Object> map = new HashMap<>();
					if (cmd.startsWith("startNginx") || cmd.startsWith("stopNginx")) {
						map.put("cmd", cmd.replaceFirst("startNginx ", "").replaceFirst("stopNginx ", ""));
					}

					String json = HttpUtil.post(url, map);
					jsonResult = JSONUtil.toBean(json, JsonResult.class);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

			if (jsonResult != null) {
				if (jsonResult.isSuccess()) {
					rs.append(jsonResult.getObj().toString());
				} else {
					rs.append(jsonResult.getMsg());
				}
			}
			rs.append("<br>");

			if (interval != null) {
				try {
					Thread.sleep(interval * 1000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return renderSuccess(rs.toString());
	}

	@Mapping("asyc")
	public JsonResult asyc(String fromId, String[] remoteId, String[] asycData) {
		if (StrUtil.isEmpty(fromId) || remoteId == null || remoteId.length == 0) {
			return renderError(m.get("remoteStr.noChoice"));
		}

		Remote remoteFrom = sqlHelper.findById(fromId, Remote.class);
		String json;
		if (remoteFrom == null) {
			// 本地
			json = getAsycPack(asycData);
		} else {
			// 远程
			json = HttpUtil.get(remoteFrom.getProtocol() + "://" + remoteFrom.getIp() + ":" + remoteFrom.getPort() + "/adminPage/remote/getAsycPack?creditKey=" + remoteFrom.getCreditKey()
					+ "&asycData=" + StrUtil.join(",", Arrays.asList(asycData)), 1000);
		}

		String adminName = getAdmin().getName();

		for (String remoteToId : remoteId) {
			if (remoteToId.equals("local") || remoteToId.equals("本地")) {
				setAsycPack(json, adminName);
			} else {
				Remote remoteTo = sqlHelper.findById(remoteToId, Remote.class);
				try {
					String version = HttpUtil.get(remoteTo.getProtocol() + "://" + remoteTo.getIp() + ":" + remoteTo.getPort() + "/adminPage/remote/version?creditKey=" + remoteTo.getCreditKey(),
							1000);
					if (StrUtil.isNotEmpty(version)) {
						// 在线
						Map<String, Object> map = new HashMap<>();
						map.put("json", json);
						HttpUtil.post(remoteTo.getProtocol() + "://" + remoteTo.getIp() + ":" + remoteTo.getPort() + "/adminPage/remote/setAsycPack?creditKey=" + remoteTo.getCreditKey()
								+ "&adminName=" + adminName, map);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		}
		return renderSuccess();
	}

	@Mapping("getAsycPack")
	public String getAsycPack(String[] asycData) {
		AsycPack asycPack = confService.getAsycPack(asycData);

		return JSONUtil.toJsonPrettyStr(asycPack);
	}

	@Mapping("setAsycPack")
	public JsonResult setAsycPack(String json, String adminName) {
		AsycPack asycPack = JSONUtil.toBean(json, AsycPack.class);

		confService.setAsycPack(asycPack);

		return renderSuccess();
	}

	@Mapping("addOver")
	public JsonResult addOver(Remote remote, String code, String auth) {
		remote.setIp(remote.getIp().trim());

		if (remoteService.hasSame(remote)) {
			return renderError(m.get("remoteStr.sameIp"));
		}

		remoteService.getCreditKey(remote, code, auth);

		if (StrUtil.isNotEmpty(remote.getCreditKey())) {
			sqlHelper.insertOrUpdate(remote);
			return renderSuccess();
		} else {
			return renderError(m.get("remoteStr.noAuth"));
		}

	}

	@Mapping("getAuth")
	public JsonResult getAuth(Remote remote) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("name", Base64.encode(Base64.encode(remote.getName())));
			map.put("pass", Base64.encode(Base64.encode(remote.getPass())));
			map.put("remote", 1);

			String rs = HttpUtil.post(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/login/getAuth", map, 3000);

			if (StrUtil.isNotEmpty(rs)) {
				JsonResult jsonResult = JSONUtil.toBean(rs, JsonResult.class);
				if (jsonResult.isSuccess()) {
					return renderSuccess(jsonResult.getObj());
				} else {
					return renderError(jsonResult.getMsg());
				}
			} else {
				return renderError(m.get("remoteStr.noAuth"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return renderError(m.get("remoteStr.noAuth"));
		}
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Remote.class));
	}

	@Mapping("del")
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Remote.class);

		return renderSuccess();
	}

	@Mapping("content")
	public JsonResult content(String id) {

		Remote remote = sqlHelper.findById(id, Remote.class);

		String rs = HttpUtil.get(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/remote/readContent?creditKey=" + remote.getCreditKey());

		return renderSuccess(rs);
	}

	@Mapping("readContent")
	public String readContent() {

		String nginxPath = settingService.get("nginxPath");

		if (FileUtil.exist(nginxPath)) {
			return FileUtil.readString(nginxPath, StandardCharsets.UTF_8);
		} else {
			return m.get("remoteStr.noFile");
		}

	}

	@Mapping("change")
	public JsonResult change(String id, Context context) {
		Remote remote = sqlHelper.findById(id, Remote.class);

		if (remote == null) {
			context.sessionSet("localType", "local");
			context.sessionRemove("remote");
		} else {
			context.sessionSet("localType", "remote");
			context.sessionSet("remote", remote);
		}

		return renderSuccess();
	}

	@Mapping("nginxStatus")
	public JsonResult nginxStatus() {
		Map<String, String> map = new HashMap<>();
		map.put("mail", settingService.get("mail"));

		String nginxMonitor = settingService.get("nginxMonitor");
		map.put("nginxMonitor", nginxMonitor != null ? nginxMonitor : "false");

		return renderSuccess(map);
	}

	@Mapping("nginxOver")
	public JsonResult nginxOver(String mail, String nginxMonitor) {
		settingService.set("mail", mail);
		settingService.set("nginxMonitor", nginxMonitor);

		return renderSuccess();
	}

	@Mapping("setMonitor")
	public JsonResult setMonitor(String id, Integer monitor) {
		if (!"local".equals(id)) {
			Remote remote = new Remote();
			remote.setId(id);
			remote.setMonitor(monitor);
			sqlHelper.updateById(remote);
		} else {
			settingService.set("monitorLocal", monitor.toString());
		}

		return renderSuccess();
	}

	@Mapping("/src")
	public void src(String url) throws Exception {

//		response.addHeader("Content-Type", "image/jpeg");
//		response.setHeader("content-disposition", "attachment;filename=code.jpg"); // 设置文件名

		byte[] buffer = new byte[1024];
		URL downUrl = new URL(url);
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(downUrl.openConnection().getInputStream());
			OutputStream os = Context.current().outputStream();
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer, 0, i);
				i = bis.read(buffer);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}
}
