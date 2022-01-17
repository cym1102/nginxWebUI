package com.cym.controller.adminPage;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.ServerEndpoint;
import org.noear.solon.core.message.Listener;
import org.noear.solon.core.message.Message;
import org.noear.solon.core.message.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.Log;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.SystemTool;
import com.cym.utils.TailLogThread;

@ServerEndpoint("/adminPage/logTail/{id}/{guid}")
public class LogTailController implements Listener {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	Map<String, Process> processMap = new HashMap<>();
	Map<String, InputStream> inputStreamMap = new HashMap<>();

	@Inject
	SqlHelper sqlHelper;

	/**
	 * 新的WebSocket请求开启
	 */
	@Override
	public void onOpen(Session session) {
		String id = session.param("id");
		String guid =  session.param("guid");
		try {
			// 执行tail -f命令
			Log log = sqlHelper.findById(id, Log.class);
			if (log == null) {
				return;
			}

			Process process = null;
			InputStream inputStream = null;

			if (SystemTool.isWindows()) {
				process = Runtime.getRuntime().exec("powershell Get-Content " + log.getPath() + " -Tail 20");
			} else {
				process = Runtime.getRuntime().exec("tail -f " + log.getPath() + " -n 20");
			}
			inputStream = process.getInputStream();

			processMap.put(guid, process);
			inputStreamMap.put(guid, inputStream);

			// 一定要启动新的线程，防止InputStream阻塞处理WebSocket的线程
			TailLogThread thread = new TailLogThread(inputStream, session);
			thread.start();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void onMessage(Session session, Message message) {

	}
	
	
	/**
	 * WebSocket请求关闭
	 */
	@Override
	public void onClose(Session session) {
		String guid = session.param("guid");
		try {
			InputStream inputStream = inputStreamMap.get(guid);
			Process process = processMap.get(guid);

			if (inputStream != null) {
				inputStream.close();
			}

			if (process != null) {
				process.destroy();
			}

			inputStreamMap.remove(guid);
			processMap.remove(guid);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void onError(Session session, Throwable thr) {
		logger.error(thr.getMessage(), thr);
	}
}
