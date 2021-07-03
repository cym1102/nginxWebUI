package com.cym.controller.adminPage;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cym.ext.TreeNode;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@Controller
@RequestMapping("/adminPage/root")
public class RootController extends BaseController {

	@ResponseBody
	@RequestMapping("getList")
	public List<TreeNode> getList(String id) {
		if (StrUtil.isEmpty(id)) {
			id = "/";
		}

		List<TreeNode> list = new ArrayList<TreeNode>();

		File[] fileList = null;
		if (SystemTool.isWindows() && id.equals("/")) {
			fileList = File.listRoots();
		} else {
			fileList = new File(id).listFiles();
		}

		for (File temp : fileList) {

			TreeNode treeNode = new TreeNode();
			treeNode.setId(temp.getPath());
			if(FileUtil.isDirectory(treeNode.getId())) {
				// 文件夹要加个/
				treeNode.setId(treeNode.getId() + "/");
			}
			if (StrUtil.isNotEmpty(temp.getName())) {
				treeNode.setName(temp.getName());
			} else {
				treeNode.setName(temp.getPath());
			}

			if (temp.isDirectory()) {
				treeNode.setIsParent("true");
			} else {
				treeNode.setIsParent("false");
			}

			list.add(treeNode);

		}

		// 按文件夹进行排序
		list.sort(new Comparator<TreeNode>() {

			@Override
			public int compare(TreeNode o1, TreeNode o2) {

				if (o1.getIsParent().equals("true") && o2.getIsParent().equals("false")) {
					return -1;
				}
				if (o1.getIsParent().equals("false") && o2.getIsParent().equals("true")) {
					return 1;
				}

				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		return list;
	}

	@ResponseBody
	@RequestMapping("mkdir")
	public JsonResult mkdir(String dir, String name) {

		FileUtil.mkdir(dir + name);

		return renderSuccess();
	}
}
