package com.cym.controller.adminPage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.ScheduleTask;
import com.cym.controller.MainController;
import com.cym.ext.AsycPack;
import com.cym.ext.Tree;
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

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/remote")
public class RemoteController extends BaseController {
	final RemoteService remoteService;
	final SettingService settingService;
	final ConfService confService;
	final GroupService groupService;
	final ConfController confController;
	final MainController mainController;

	@Value("${project.version}")
	String projectVersion;
	@Value("${server.port}")
	Integer port;

	public RemoteController(RemoteService remoteService, SettingService settingService, ConfService confService, GroupService groupService, ConfController confController,
			MainController mainController) {
		this.remoteService = remoteService;
		this.settingService = settingService;
		this.confService = confService;
		this.groupService = groupService;
		this.confController = confController;
		this.mainController = mainController;
	}

	@RequestMapping("version")
	@ResponseBody
	public Map<String, Object> version() {
		Map<String, Object> map = new HashMap<>();
		map.put("version", projectVersion);

		if (NginxUtils.isRun()) {
			map.put("nginx", 1);
		} else {
			map.put("nginx", 0);
		}

		return map;
	}

	@RequestMapping("")
	public ModelAndView index(ModelAndView modelAndView) {
		modelAndView.setViewName("/adminPage/remote/index");
		modelAndView.addObject("projectVersion", projectVersion);
		return modelAndView;
	}

	@RequestMapping("allTable")
	@ResponseBody
	public List<Remote> allTable() {
		List<Remote> remoteList = sqlHelper.findAll(Remote.class);

		for (Remote remote : remoteList) {
			remote.setStatus(0);
			remote.setType(0);
			if (remote.getParentId() == null) {
				remote.setParentId("");
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
				e.printStackTrace();
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
		remoteList.add(0, remoteLocal);

		List<Group> groupList = sqlHelper.findAll(Group.class);
		for (Group group : groupList) {
			Remote remoteGroup = new Remote();
			remoteGroup.setDescr(group.getName());
			remoteGroup.setId(group.getId());
			remoteGroup.setParentId(group.getParentId() != null ? group.getParentId() : "");
			remoteGroup.setType(1);

			remoteGroup.setIp("");
			remoteGroup.setProtocol("");
			remoteGroup.setVersion("");
			remoteGroup.setSystem("");

			remoteList.add(remoteGroup);
		}

		return remoteList;
	}

	@RequestMapping("addGroupOver")
	@ResponseBody
	public JsonResult addGroupOver(Group group) {

		sqlHelper.insertOrUpdate(group);

		return renderSuccess();
	}

	@RequestMapping("groupDetail")
	@ResponseBody
	public JsonResult groupDetail(String id) {
		return renderSuccess(sqlHelper.findById(id, Group.class));
	}

	@RequestMapping("delGroup")
	@ResponseBody
	public JsonResult delGroup(String id) {

		groupService.delete(id);
		return renderSuccess();
	}

	@RequestMapping("getGroupTree")
	@ResponseBody
	public JsonResult getGroupTree() {

		List<Group> groups = groupService.getListByParent(null);
		List<Tree> treeList = new ArrayList<>();
		fillTree(groups, treeList);

		Tree tree = new Tree();
		tree.setName(m.get("remoteStr.noGroup"));
		tree.setValue("");

		treeList.add(0, tree);

		return renderSuccess(treeList);
	}

	private void fillTree(List<Group> groups, List<Tree> treeList) {
		for (Group group : groups) {
			Tree tree = new Tree();
			tree.setName(group.getName());
			tree.setValue(group.getId());

			List<Tree> treeSubList = new ArrayList<>();
			fillTree(groupService.getListByParent(group.getId()), treeSubList);
			tree.setChildren(treeSubList);

			treeList.add(tree);
		}

	}

	@RequestMapping("getCmdRemote")
	@ResponseBody
	public JsonResult getCmdRemote() {

		List<Group> groups = groupService.getListByParent(null);
		List<Remote> remotes = remoteService.getListByParent(null);

		List<Tree> treeList = new ArrayList<>();
		fillTreeRemote(groups, remotes, treeList);

		Tree tree = new Tree();
		tree.setName(m.get("remoteStr.local"));
		tree.setValue(m.get("remoteStr.local"));

		treeList.add(0, tree);

		return renderSuccess(treeList);
	}

	private void fillTreeRemote(List<Group> groups, List<Remote> remotes, List<Tree> treeList) {
		for (Group group : groups) {
			Tree tree = new Tree();
			tree.setName(group.getName());
			tree.setValue(group.getId());

			List<Tree> treeSubList = new ArrayList<>();

			fillTreeRemote(groupService.getListByParent(group.getId()), remoteService.getListByParent(group.getId()), treeSubList);

			tree.setChildren(treeSubList);

			treeList.add(tree);
		}

		for (Remote remote : remotes) {
			Tree tree = new Tree();
			tree.setName(remote.getIp() + "【" + remote.getDescr() + "】");
			tree.setValue(remote.getId());

			treeList.add(tree);
		}

	}

	@RequestMapping("cmdOver")
	@ResponseBody
	public JsonResult cmdOver(String[] remoteId, String cmd) {
		if (remoteId == null || remoteId.length == 0) {
			return renderSuccess("未选择服务器");
		}

		StringBuilder rs = new StringBuilder();
		for (String id : remoteId) {
			JsonResult jsonResult = null;
			if (id.equals("local")) {
				if (cmd.contentEquals("check")) {
					jsonResult = confController.check(null, null, null);
				}
				if (cmd.contentEquals("reload")) {
					jsonResult = confController.reload(null, null, null);
				}
				if (cmd.contentEquals("start")) {
					jsonResult = confController.start(null, null, null);
				}
				if (cmd.contentEquals("stop")) {
					jsonResult = confController.stop(null, null);
				}
				if (cmd.contentEquals("update")) {
					jsonResult = renderError(m.get("remoteStr.notAllow"));
				}
				rs.append("<span class='blue'>" + m.get("remoteStr.local") + "> </span>");
			} else {
				Remote remote = sqlHelper.findById(id, Remote.class);
				rs.append("<span class='blue'>").append(remote.getIp()).append("> </span>");

				try {
					String json = HttpUtil.get(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/conf/" + cmd + "?creditKey=" + remote.getCreditKey());
					jsonResult = JSONUtil.toBean(json, JsonResult.class);
				} catch (Exception e) {
					e.printStackTrace();
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
		}

		return renderSuccess(rs.toString());
	}

	@RequestMapping("asyc")
	@ResponseBody
	public JsonResult asyc(String fromId, String[] remoteId) {
		if (StrUtil.isEmpty(fromId) || remoteId == null || remoteId.length == 0) {
			return renderSuccess(m.get("remoteStr.noChoice"));
		}

		Remote remoteFrom = sqlHelper.findById(fromId, Remote.class);
		String json;
		if (remoteFrom == null) {
			// 本地
			json = getAsycPack();
		} else {
			// 远程
			json = HttpUtil.get(remoteFrom.getProtocol() + "://" + remoteFrom.getIp() + ":" + remoteFrom.getPort() + "/adminPage/remote/getAsycPack?creditKey=" + remoteFrom.getCreditKey(), 1000);
		}

		for (String remoteToId : remoteId) {
			if (remoteToId.equals("local")) {
				setAsycPack(json);
			} else {
				Remote remoteTo = sqlHelper.findById(remoteToId, Remote.class);
				try {
					String version = HttpUtil.get(remoteTo.getProtocol() + "://" + remoteTo.getIp() + ":" + remoteTo.getPort() + "/adminPage/remote/version?creditKey=" + remoteTo.getCreditKey(),
							1000);
					if (StrUtil.isNotEmpty(version)) {
						// 在线
						Map<String, Object> map = new HashMap<>();
						map.put("json", json);
						HttpUtil.post(remoteTo.getProtocol() + "://" + remoteTo.getIp() + ":" + remoteTo.getPort() + "/adminPage/remote/setAsycPack?creditKey=" + remoteTo.getCreditKey(), map);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return renderSuccess();
	}

	@RequestMapping("getAsycPack")
	@ResponseBody
	public String getAsycPack() {
		AsycPack asycPack = confService.getAsycPack();

		return JSONUtil.toJsonPrettyStr(asycPack);
	}

	@RequestMapping("setAsycPack")
	@ResponseBody
	public JsonResult setAsycPack(String json) {
		AsycPack asycPack = JSONUtil.toBean(json, AsycPack.class);

		confService.setAsycPack(asycPack);

		return renderSuccess();
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Remote remote) {
		remote.setIp(remote.getIp().trim());

		if (remoteService.hasSame(remote)) {
			return renderError(m.get("remoteStr.sameIp"));
		}

		remoteService.getCreditKey(remote);

		if (StrUtil.isNotEmpty(remote.getCreditKey())) {
			sqlHelper.insertOrUpdate(remote);
			return renderSuccess();
		} else {
			return renderError(m.get("remoteStr.noAuth"));
		}

	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Remote.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Remote.class);

		return renderSuccess();
	}

	@RequestMapping("content")
	@ResponseBody
	public JsonResult content(String id) {

		Remote remote = sqlHelper.findById(id, Remote.class);

		String rs = HttpUtil.get(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/remote/readContent?creditKey=" + remote.getCreditKey());

		return renderSuccess(rs);
	}

	@RequestMapping("readContent")
	@ResponseBody
	public String readContent() {

		String nginxPath = settingService.get("nginxPath");

		if (FileUtil.exist(nginxPath)) {
			return FileUtil.readString(nginxPath, StandardCharsets.UTF_8);
		} else {
			return m.get("remoteStr.noFile");
		}

	}

	@RequestMapping("change")
	@ResponseBody
	public JsonResult change(String id, HttpSession httpSession) {
		Remote remote = sqlHelper.findById(id, Remote.class);

		if (remote == null) {
			httpSession.setAttribute("localType", "local");
			httpSession.removeAttribute("remote");
		} else {
			httpSession.setAttribute("localType", "remote");
			httpSession.setAttribute("remote", remote);
		}

		return renderSuccess();
	}

	@RequestMapping("nginxStatus")
	@ResponseBody
	public JsonResult nginxStatus(HttpSession httpSession) {
		Map<String, String> map = new HashMap<>();
		map.put("mail", settingService.get("mail"));

		String nginxMonitor = settingService.get("nginxMonitor");
		map.put("nginxMonitor", nginxMonitor != null ? nginxMonitor : "false");

		return renderSuccess(map);
	}

	@RequestMapping("nginxOver")
	@ResponseBody
	public JsonResult nginxOver(String mail, String nginxMonitor) {
		settingService.set("mail", mail);
		settingService.set("nginxMonitor", nginxMonitor);

		return renderSuccess();
	}

	@RequestMapping("setMonitor")
	@ResponseBody
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

}
