package com.cym.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.ext.DataGroup;
import com.cym.ext.KeyValue;
import com.cym.model.Admin;
import com.cym.model.Log;
import com.cym.model.LogInfo;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;

@Service
public class LogService {
	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	JdbcTemplate jdbcTemplate;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Transactional
	public DataGroup buildDataGroup(String path) {
		insertIntoDb(path);

		DataGroup dataGroup = new DataGroup();
		// pvuv
		dataGroup.setPv(jdbcTemplate.query("select hour as name,count(1) as value FROM log_info group by hour order by name", new BeanPropertyRowMapper<KeyValue>(KeyValue.class)));
		dataGroup.setUv(jdbcTemplate.query("SELECT name, COUNT(value) as value " + //
				"FROM ( " + //
				"	SELECT hour AS name, COUNT(remote_addr) AS value " + //
				"	FROM log_info " + //
				"	GROUP BY hour, remote_addr " + //
				"	ORDER BY name " + //
				") " + //
				"GROUP BY name", new BeanPropertyRowMapper<KeyValue>(KeyValue.class)));
		
		// 状态
		dataGroup.setStatus(jdbcTemplate.query("select status as name,count(1) as value FROM log_info group by status", new BeanPropertyRowMapper<KeyValue>(KeyValue.class)));

		// 系统
		dataGroup.setBrowser(new ArrayList<KeyValue>());
		String[] browsers = new String[] { "Android", "iPhone", "Windows","Macintosh" };
		Integer allCount = 0;
		for (String browser : browsers) {
			KeyValue keyValue = new KeyValue();
			keyValue.setName(browser);
			keyValue.setValue(jdbcTemplate.queryForObject("select count(1) from log_info where http_user_agent like '%" + browser + "%'", Integer.class));
			dataGroup.getBrowser().add(keyValue);
			allCount += keyValue.getValue();
		}

		KeyValue keyValue = new KeyValue();
		keyValue.setName("Other");
		keyValue.setValue(sqlHelper.findCountByQuery(null, LogInfo.class).intValue() - allCount);
		dataGroup.getBrowser().add(keyValue);

		// 域名
		dataGroup.setHttpReferer(
				jdbcTemplate.query("select http_host as name,count(1) as value FROM log_info group by http_host order by value asc", new BeanPropertyRowMapper<KeyValue>(KeyValue.class)));

		saveLog(dataGroup, path);
		return dataGroup;
	}

	private void insertIntoDb(String path) {
		BufferedReader reader = null;
		try {
			File zipFile = new File(path);
			File outFile = new File(path.replace(".zip", "") + File.separator + zipFile.getName().replace(".zip", ".log"));
			ZipUtil.unzip(zipFile);

			sqlHelper.deleteByQuery(new ConditionAndWrapper(), LogInfo.class);

			Long count = 0l;
			
			reader = FileUtil.getReader(outFile, "UTF-8");
			List<Object> list = new ArrayList<Object>();
			while (true) {
				String json = reader.readLine();
				if (StrUtil.isEmpty(json)) {
					sqlHelper.insertAll(list);
					count += list.size();
					list.clear();
					
					break;
				}

				json = json.replace("\\x", "");
				if (JSONUtil.isJson(json)) {
					LogInfo logInfo = JSONUtil.toBean(json, LogInfo.class);
					String[] str = logInfo.getTimeLocal().split(":");
					logInfo.setHour(str[1]);
					logInfo.setMinute(str[2]);
					logInfo.setSecond(str[3].split(" ")[0]);
					
					list.add(logInfo);
				} else {
					System.err.println(json);
				}

				if (list.size() == 1000) {
					sqlHelper.insertAll(list);
					count += list.size();
					list.clear();
				}
			}

			logger.info("插入LogInfo:" + count + "条"); 
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IoUtil.close(reader);
			FileUtil.del(path.replace(".zip", "") + File.separator);
		}
	}

	private void saveLog(DataGroup dataGroup, String path) {
		Log log = new Log();
		File file = new File(path);
		DateTime date = DateUtil.parse(file.getName().replace("access.", "").replace(".zip", ""), "yyyy-MM-dd_HH-mm-ss");

		log.setDate(DateUtil.format(date, "yyyy-MM-dd"));
		log.setJson(JSONUtil.toJsonStr(dataGroup));
		log.setPath(path);
		
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("date", log.getDate()), Log.class);
		
		sqlHelper.insert(log);
	}


	public Page search(Page page) {
		page = sqlHelper.findPage(page, Log.class);

		return page;
	}


}
