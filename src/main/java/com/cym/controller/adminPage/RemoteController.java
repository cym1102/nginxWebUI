package com.cym.controller.adminPage;

import java.nio.charset.Charset;
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
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/remote")
public class RemoteController extends BaseController {
	@Autowired
	RemoteService remoteService;
	@Autowired
	SettingService settingService;
	@Autowired
	ConfService confService;
	@Autowired
	GroupService groupService;
	
	@Value("${project.version}")
	String version;
	@Value("${server.port}")
	Integer port;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {

		modelAndView.addObject("groupList", sqlHelper.findAll(Group.class));
		modelAndView.setViewName("/adminPage/remote/index");

		return modelAndView;
	}

	@RequestMapping("allTable")
	@ResponseBody
	public List<Remote> allTable() {
		List<Remote> remoteList = sqlHelper.findAll(Remote.class);

		for (Remote remote : remoteList) {
			remote.setStatus(0);
			try {
				String version = HttpUtil.get(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/adminPage/remote/version?creditKey=" + remote.getCreditKey(), 500);
				if (StrUtil.isNotEmpty(version)) {
					remote.setStatus(1);
					remote.setVersion(version);
				}

				if (remote.getParentId() == null) {
					remote.setParentId("");
				}

				remote.setType(0);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		Remote remoteLocal = new Remote();
		remoteLocal.setId("本地");
		remoteLocal.setIp("");
		remoteLocal.setProtocol("");
		remoteLocal.setParentId("");
		remoteLocal.setDescr("本地");
		remoteLocal.setVersion(version);
		remoteLocal.setPort(port);
		remoteLocal.setStatus(1);
		remoteLocal.setType(0);
		remoteLocal.setSystem(SystemTool.getSystem());
		remoteList.add(0, remoteLocal);

		List<Group> groupList = sqlHelper.findAll(Group.class);
		for (Group group : groupList) {
			Remote remoteGroup = new Remote();
			remoteGroup.setDescr(group.getName());
			remoteGroup.setId(group.getId());
			remoteGroup.setParentId(group.getParentId()!=null?group.getParentId():"");
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
		fillTree(groups ,treeList);
		
		Tree tree = new Tree();
		tree.setName("--无分组--");
		tree.setValue("");
		
		treeList.add(0, tree);
		
		return renderSuccess(treeList);
	}

	private void fillTree(List<Group> groups, List<Tree> treeList) {
		for(Group group:groups) {
			Tree tree = new Tree();
			tree.setName(group.getName());
			tree.setValue(group.getId());
			
			List<Tree> treeSubList = new ArrayList<>();
			fillTree(groupService.getListByParent(group.getId()),treeSubList);
			tree.setChildren(treeSubList);
			
			treeList.add(tree);
		}
		
	}

	@RequestMapping("getAllowRemote")
	@ResponseBody
	public JsonResult getAllowRemote(String id) {
		Remote remoteFrom = sqlHelper.findById(id, Remote.class);
		String system = null;
		List<Remote> remotes = null;
		if (remoteFrom == null) {
			// 本地
			system = SystemTool.getSystem();
			remotes = remoteService.getBySystem(system);
		} else {
			// 远程
			system = remoteFrom.getSystem();
			remotes = remoteService.getBySystem(system);

			// 去掉自己
			for (Remote remote : remotes) {
				if (remote.getId().equals(remoteFrom.getId())) {
					remotes.remove(remote);
					break;
				}
			}

			// 系统相同,加上本地
			if (system.equals(SystemTool.getSystem())) {
				Remote remote = new Remote();
				remote.setId("本地");
				remote.setIp("本地");
				remote.setVersion(version);
				remote.setPort(port);
				remote.setSystem(SystemTool.getSystem());
				remote.setDescr("");

				remotes.add(0, remote);
			}
		}

		return renderSuccess(remotes);
	}

	@RequestMapping("asyc")
	@ResponseBody
	public JsonResult asyc(String fromId, String[] remoteId) {

		Remote remoteFrom = sqlHelper.findById(fromId, Remote.class);
		String json = null;
		if (remoteFrom == null) {
			// 本地
			json = getAsycPack();
		} else {
			// 远程
			json = HttpUtil.get(remoteFrom.getProtocol() + "://" + remoteFrom.getIp() + ":" + remoteFrom.getPort() + "/adminPage/remote/getAsycPack?creditKey=" + remoteFrom.getCreditKey(), 500);
		}

		if (remoteId != null) {
			for (String remoteToId : remoteId) {
				if (remoteToId.equals("本地")) {
					System.err.println("同步到本地");
					setAsycPack(json);
				} else {
					Remote remoteTo = sqlHelper.findById(remoteToId, Remote.class);
					System.err.println("同步到" + remoteTo.getIp());
					try {
						String version = HttpUtil.get(remoteTo.getProtocol() + "://" + remoteTo.getIp() + ":" + remoteTo.getPort() + "/adminPage/remote/version?creditKey=" + remoteTo.getCreditKey(),
								500);
						if (StrUtil.isNotEmpty(version)) {
							// 在线
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("json", json);
							HttpUtil.post(remoteTo.getProtocol() + "://" + remoteTo.getIp() + ":" + remoteTo.getPort() + "/adminPage/remote/setAsycPack?creditKey=" + remoteTo.getCreditKey(), map);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}
		return renderSuccess();
	}

	@RequestMapping("getAsycPack")
	@ResponseBody
	public String getAsycPack() {
		AsycPack asycPack = confService.getAsycPack();

		return JSONUtil.toJsonStr(asycPack);
	}

	@RequestMapping("setAsycPack")
	@ResponseBody
	public JsonResult setAsycPack(String json) {
		System.err.println("收到同步信息");
		AsycPack asycPack = JSONUtil.toBean(json, AsycPack.class);

		confService.setAsycPack(asycPack);

		return renderSuccess();
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Remote remote) {

		remoteService.getCreditKey(remote);

		if (StrUtil.isNotEmpty(remote.getCreditKey())) {
			sqlHelper.insertOrUpdate(remote);
			return renderSuccess();
		} else {
			return renderError("远程授权未通过,请检查");
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

	@RequestMapping("version")
	@ResponseBody
	public String version() {
		return version;
	}

	@RequestMapping("readContent")
	@ResponseBody
	public String readContent() {

		String nginxPath = settingService.get("nginxPath");

		if (FileUtil.exist(nginxPath)) {
			String orgStr = FileUtil.readString(nginxPath, Charset.defaultCharset());
			return orgStr;
		} else {
			return "文件不存在";
		}

	}

	@RequestMapping("change")
	@ResponseBody
	public JsonResult change(String id, HttpSession httpSession) {
		Remote remote = sqlHelper.findById(id, Remote.class);

		if (remote == null) {
			httpSession.setAttribute("localType", "本地");
			httpSession.removeAttribute("remote");
		} else {
			httpSession.setAttribute("localType", "远程");
			httpSession.setAttribute("remote", remote);
		}

		return renderSuccess();
	}
}
