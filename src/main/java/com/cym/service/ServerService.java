package com.cym.service;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import cn.hutool.core.io.FileUtil;
import com.github.odiszapc.nginxparser.NgxBlock;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxEntry;
import com.github.odiszapc.nginxparser.NgxParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.model.Location;
import com.cym.model.Param;
import com.cym.model.Server;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.ConditionOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.util.StringUtils;

@Service
public class ServerService {
	private final static Logger log = LoggerFactory.getLogger(ServerService.class);

	@Autowired
	SqlHelper sqlHelper;

//	@Value("${project.home}")
//	private String tmpPath;

	public Page search(Page page, String sortColum, String direction, String keywords) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();
		if(StrUtil.isNotEmpty(keywords)) {
			conditionAndWrapper.and(new ConditionOrWrapper().like("serverName", keywords.trim()).like("listen", keywords.trim()));
		}
		
		Sort sort = null;
		if (StrUtil.isNotEmpty(sortColum)) {
			sort = new Sort(sortColum, "asc".equalsIgnoreCase(direction) ? Direction.ASC : Direction.DESC);
		}
		
		page = sqlHelper.findPage(conditionAndWrapper, sort, page, Server.class);

		return page;
	}

	@Transactional
	public void deleteById(String id) {
		sqlHelper.deleteById(id, Server.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("serverId", id), Location.class);
	}

	public List<Location> getLocationByServerId(String serverId) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("serverId", serverId), Location.class);
	}

	@Transactional
	public void addOver(Server server, String serverParamJson, List<Location> locations) throws Exception {
		Server tmpServer = sqlHelper.findOneByQuery(new ConditionOrWrapper().eq("serverName", server.getServerName()), Server.class);
		if (!StringUtils.isEmpty(server.getId())) {
			if (tmpServer != null) {
				if (!server.getId().equals(tmpServer.getId())) {
					throw new Exception("serverName:" + tmpServer.getServerName() + " 已经存在");
				}
			}
			// 修改操作
			sqlHelper.insertOrUpdate(server);
		} else {
			// 新增
			if (tmpServer != null) {
				throw new Exception("serverName:" + tmpServer.getServerName() + " 已经存在");
			}
			sqlHelper.insertOrUpdate(server);
		}
		List<Param> paramList = new ArrayList<Param>();
		if (StrUtil.isNotEmpty(serverParamJson) && JSONUtil.isJson(serverParamJson)) {
			paramList = JSONUtil.toList(JSONUtil.parseArray(serverParamJson), Param.class);
		}
		List<String> locationIds = sqlHelper.findIdsByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
		sqlHelper.deleteByQuery(new ConditionOrWrapper().eq("serverId", server.getId()).in("locationId", locationIds), Param.class);
		
		 // 反向插入,保证列表与输入框对应
		Collections.reverse(paramList);
		for (Param param : paramList) {
			param.setServerId(server.getId());
			sqlHelper.insert(param);
		}

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);

		if (locations!=null) {
			 // 反向插入,保证列表与输入框对应
			Collections.reverse(locations);
			
			for (Location location:locations) {
				location.setServerId(server.getId());

				sqlHelper.insert(location);

				paramList = new ArrayList<Param>();
				if (StrUtil.isNotEmpty(location.getLocationParamJson()) && JSONUtil.isJson(location.getLocationParamJson())) {
					paramList = JSONUtil.toList(JSONUtil.parseArray(location.getLocationParamJson()), Param.class);
				}
				
				 // 反向插入,保证列表与输入框对应
				Collections.reverse(paramList);
				for (Param param : paramList) {
					param.setLocationId(location.getId());
					sqlHelper.insert(param);
				}
			}
		}
	}

	@Transactional
	public void addOverTcp(Server server, String serverParamJson) {
		sqlHelper.insertOrUpdate(server);

		List<String> locationIds = sqlHelper.findIdsByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
		sqlHelper.deleteByQuery(new ConditionOrWrapper().eq("serverId", server.getId()).in("locationId", locationIds), Param.class);
		List<Param> paramList = new ArrayList<Param>();
		if (StrUtil.isNotEmpty(serverParamJson) && JSONUtil.isJson(serverParamJson)) {
			paramList = JSONUtil.toList(JSONUtil.parseArray(serverParamJson), Param.class);
		}

		for (Param param : paramList) {
			param.setServerId(server.getId());
			sqlHelper.insert(param);
		}

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
	}

	public List<Server> getListByProxyType(Integer proxyType) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("proxyType", proxyType), Server.class);
	}

	@Transactional
	public void clone(String id) {
		Server server = sqlHelper.findById(id, Server.class);

		List<Location> locations = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
		List<Param> params = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Param.class);

		server.setId(null);
		sqlHelper.insertOrUpdate(server);
		for (Param param : params) {
			param.setId(null);
			param.setServerId(server.getId());
			sqlHelper.insert(param);
		}

		for (Location location : locations) {
			params = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("locationId", location.getId()), Param.class);

			location.setId(null);
			location.setServerId(server.getId());
			sqlHelper.insert(location);

			for (Param param : params) {
				param.setId(null);
				param.setLocationId(location.getId());
				sqlHelper.insert(param);
			}
		}

	}

//	public boolean hasListen(String listen, String serverId) {
//		ConditionAndWrapper conditionAndWrapper =  new ConditionAndWrapper().eq("listen", listen);
//		if(StrUtil.isNotEmpty(serverId)) {
//			conditionAndWrapper.ne("id", serverId);
//		}
//		return sqlHelper.findCountByQuery(conditionAndWrapper, Server.class) > 0;
//	}

	public void importServer(String nginxPath) throws Exception {
		String initNginxPath = initNginx(nginxPath);
		NgxConfig conf = null;
		try {
			conf = NgxConfig.read(initNginxPath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("文件读取失败");
		}
		assert conf != null;
		List<NgxEntry> servers;
		if (nginxPath.contains("/etc/nginx/conf.d") || nginxPath.contains("/etc/nginx/sites-available") || nginxPath.contains("default")) {
			servers = conf.findAll(NgxConfig.BLOCK, "server");
		} else {
			servers = conf.findAll(NgxConfig.BLOCK, "http", "server");
		}
		Server server;
		for (NgxEntry ngxEntry : servers) {
			NgxBlock serverNgx = (NgxBlock) ngxEntry;
			NgxParam serverName = serverNgx.findParam("server_name");
			server = new Server();
			if (serverName == null) {
				server.setServerName("");
			} else {
				server.setServerName(serverName.getValue());
			}

			server.setProxyType(0);

			// 设置监听端口，先设置为80端口，再判断是否含有443 如果有，则给ssl相关信息赋值上
			server.setListen("80");
			server.setSsl(0);
			List<NgxEntry> listens = serverNgx.findAll(NgxConfig.PARAM, "listen");
			for (NgxEntry item: listens) {
				NgxParam param = (NgxParam) item;
				if (param.getTokens().stream().anyMatch(item2 -> "443".equals(item2.getToken()))) {
					server.setListen("443");
					server.setSsl(1);
					NgxParam key = serverNgx.findParam("ssl_certificate_key");
					NgxParam perm = serverNgx.findParam("ssl_certificate");
					server.setKey(key == null ? "" : key.getValue());
					server.setPem(perm == null ? "" : perm.getValue());
					break;
				}
			}

			server.setHttp2(1);

			long rewriteCount = serverNgx.getEntries().stream().filter(item -> {
				if (item instanceof NgxBlock) {
					NgxBlock itemNgx = (NgxBlock)item;
					if (itemNgx.getEntries().toString().contains("rewrite")) {
						return true;
					}
					return false;
				}
				return false;
			}).count();
			if (rewriteCount > 0) {
				server.setRewrite(1);
			} else {
				server.setRewrite(0);
			}

			List<Location> locations = new ArrayList<>();
			List<NgxEntry> locationBlocks = serverNgx.findAll(NgxBlock.class, "location");
			for (NgxEntry item : locationBlocks) {
				Location location = new Location();
				// 目前只支持http段的 proxy_pass
				NgxParam proxyPassParam = ((NgxBlock)item).findParam("proxy_pass");

				location.setPath(((NgxBlock) item).getValue());
				// 如果没有proxy_pass type 0,说明可能是静态文件夹映射 type 1
				if (proxyPassParam != null) {
					location.setValue(proxyPassParam.getValue());
					location.setType(0);
				} else {
					NgxParam rootParam = ((NgxBlock)item).findParam("root");
					if (rootParam == null) {
						continue;
					}
					location.setValue(rootParam.getValue());
					location.setType(1);
				}
				location.setLocationParamJson(null);
				locations.add(location);
			}

			try {
				this.addOver(server, "", locations);
			} catch (Exception e) {
				if ("已经存在".equals(e.getMessage())) {
					log.info("serverName为 {} 已经存在", server);
				}
			}

		}
		// 删除临时文件再
		FileUtil.del(initNginxPath);
	}

	/**
	 * 重新创建配置文件，删除影响解析的行（比如#号开头，但是此行没有其他内容）
	 *
	 * @param nginxPath
	 * @return java.lang.String
	 * @author by yanglei 2020/7/5 21:17
	 */
	private String initNginx(String nginxPath) {
		//删除一行内容（java本身没有删除的方法，本方法通过先读取文件的内容（需删除的行数除外），放到list中，在重新写入）
		String initNginxPath = FileUtil.getTmpDirPath() + UUID.randomUUID().toString();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(nginxPath));
			BufferedWriter bw = new BufferedWriter(new FileWriter(initNginxPath));
			String str = null;
			int num = 0;
			while ((str = br.readLine()) != null) {
				num ++;
				if (num == 686) {
					log.info("111");
				}
				if (str.trim().indexOf("#") == 0) {
					continue;
				}
				bw.write(str);
				bw.newLine();
			}
			bw.flush();
			bw.close();
			log.info("读取行数{}", num);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return initNginxPath;
	}
}
