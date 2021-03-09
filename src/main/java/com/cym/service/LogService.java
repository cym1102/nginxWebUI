package com.cym.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cym.model.Log;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class LogService {
	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	JdbcTemplate jdbcTemplate;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public boolean hasDir(String path, String id) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq("path", path);
		if(StrUtil.isNotEmpty(id)) {
			conditionAndWrapper.ne("id", id);
		}
		return sqlHelper.findCountByQuery(conditionAndWrapper, Log.class) > 0;

	}

	public Page search(Page page) {
		return sqlHelper.findPage(page, Log.class);
	}
	
	
	
//	@Transactional
//	public DataGroup buildDataGroup(String path) {
//		if (!new File(path).exists()) {
//			return null;
//		}
//		insertIntoDb(path);
//
//		DataGroup dataGroup = new DataGroup();
//		// pvuv
//		dataGroup.setPv(jdbcTemplate.query("select hour as name,count(1) as value FROM log_info group by hour order by name", new BeanPropertyRowMapper<KeyValue>(KeyValue.class)));
//		dataGroup.setUv(jdbcTemplate.query("SELECT name, COUNT(value) as value " + //
//				" FROM ( " + //
//				"	SELECT hour AS name, COUNT(remote_addr) AS value " + //
//				"	FROM log_info " + //
//				"	GROUP BY hour, remote_addr " + //
//				"	ORDER BY name " + //
//				" ) tmp " + //
//				" GROUP BY name ", new BeanPropertyRowMapper<KeyValue>(KeyValue.class)));
//
//		// 状态
//		dataGroup.setStatus(jdbcTemplate.query("select status as name,count(1) as value FROM log_info group by status", new BeanPropertyRowMapper<KeyValue>(KeyValue.class)));
//
//		// 系统
//		dataGroup.setBrowser(new ArrayList<KeyValue>());
//		String[] browsers = new String[] { "Android", "iPhone", "Windows", "Macintosh" };
//		Integer allCount = 0;
//		for (String browser : browsers) {
//			KeyValue keyValue = new KeyValue();
//			keyValue.setName(browser);
//			keyValue.setValue(jdbcTemplate.queryForObject("select count(1) from log_info where http_user_agent like '%" + browser + "%'", Integer.class));
//			dataGroup.getBrowser().add(keyValue);
//			allCount += keyValue.getValue();
//		}
//
//		KeyValue keyValue = new KeyValue();
//		keyValue.setName("Other");
//		keyValue.setValue(sqlHelper.findCountByQuery(null, LogInfo.class).intValue() - allCount);
//		dataGroup.getBrowser().add(keyValue);
//
//		// 域名
//		List<KeyValue> httpReferer = jdbcTemplate.query("select http_host as name,count(1) as value FROM log_info group by http_host order by value DESC limit 10",
//				new BeanPropertyRowMapper<KeyValue>(KeyValue.class));
//		Collections.reverse(httpReferer);
//		dataGroup.setHttpReferer(httpReferer);
//
//		saveLog(dataGroup, path);
//
//		return dataGroup;
//	}
//
//	public void clearDb() {
//		sqlHelper.deleteByQuery(new ConditionAndWrapper(), LogInfo.class);
//		jdbcTemplate.execute("vacuum;"); // 缩小sqlite.db大小
//	}
//
//	private void insertIntoDb(String path) {
//		BufferedReader reader = null;
//		try {
//			File zipFile = new File(path);
//			File outFile = new File(path.replace(".zip", "") + File.separator + zipFile.getName().replace(".zip", ".log"));
//			ZipUtil.unzip(zipFile);
//
//			sqlHelper.deleteByQuery(new ConditionAndWrapper(), LogInfo.class);
//
//			Long count = 0l;
//
//			reader = FileUtil.getReader(outFile, "UTF-8");
//			List<Object> list = new ArrayList<Object>();
//			while (true) {
//				String json = reader.readLine();
//				if (StrUtil.isEmpty(json)) {
//					sqlHelper.insertAll(list);
//					count += list.size();
//					list.clear();
//
//					break;
//				}
//
//				json = json.replace("\\x", "");
//				if (JSONUtil.isJson(json)) {
//					try {
//						LogInfo logInfo = JSONUtil.toBean(json, LogInfo.class);
//						String[] str = logInfo.getTimeLocal().split(":");
//						logInfo.setHour(str[1]);
//						logInfo.setMinute(str[2]);
//						logInfo.setSecond(str[3].split(" ")[0]);
//
//						list.add(logInfo);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				} else {
//					System.err.println(json);
//				}
//
//				if (list.size() == 1000) {
//					sqlHelper.insertAll(list);
//					count += list.size();
//					list.clear();
//				}
//			}
//
//			logger.info("插入LogInfo:" + count + "条");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			IoUtil.close(reader);
//			FileUtil.del(path.replace(".zip", "") + File.separator);
//		}
//	}
//
//	private void saveLog(DataGroup dataGroup, String path) {
//		Log log = new Log();
//		File file = new File(path);
//		log.setDate(file.getName().replace("access.", "").replace(".zip", ""));
//		log.setJson(JSONUtil.toJsonStr(dataGroup));
//		log.setPath(path);
//
//		Log logOrg = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("date", log.getDate()), Log.class);
//		if (logOrg != null) {
//			DataGroup dataGroupOrg = JSONUtil.toBean(logOrg.getJson(), DataGroup.class);
//			sum(dataGroupOrg, dataGroup);
//			logOrg.setJson(JSONUtil.toJsonStr(dataGroupOrg));
//
//			sqlHelper.updateById(logOrg);
//		} else {
//			sqlHelper.insert(log);
//		}
//	}
//
//	private void sum(DataGroup dataGroupOrg, DataGroup dataGroup) {
//		addSum(dataGroupOrg.getPv(), dataGroup.getPv());
//		addSum(dataGroupOrg.getUv(), dataGroup.getUv());
//		addSum(dataGroupOrg.getStatus(), dataGroup.getStatus());
//		addSum(dataGroupOrg.getBrowser(), dataGroup.getBrowser());
//		addSum(dataGroupOrg.getHttpReferer(), dataGroup.getHttpReferer());
//	}
//
//	private void addSum(List<KeyValue> keyValuesOrg, List<KeyValue> keyValues) {
//		for (KeyValue keyValue : keyValues) {
//			boolean hasSame = false;
//			for (KeyValue keyValueOrg : keyValuesOrg) {
//				if (keyValueOrg.getName().equals(keyValue.getName())) {
//					keyValueOrg.setValue(keyValueOrg.getValue() + keyValue.getValue());
//					hasSame = true;
//				}
//			}
//
//			if (!hasSame) {
//				keyValuesOrg.add(keyValue);
//			}
//		}
//	}
//
//	public Page search(Page page) {
//		page = sqlHelper.findPage(page, Log.class);
//
//		return page;
//	}
//
//	public List<Log> findByDate(String startDate, String endDate) {
//		
//		return sqlHelper.findListByQuery(new ConditionAndWrapper().gte("date", startDate).lte("date", endDate), Log.class);
//	}

}
