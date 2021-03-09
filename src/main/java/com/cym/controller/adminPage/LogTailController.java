package com.cym.controller.adminPage;

import java.io.IOException;
import java.io.InputStream;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import com.cym.model.Log;
import com.cym.utils.ApplicationContextRegister;
import com.cym.utils.TailLogThread;

import cn.craccd.sqlHelper.utils.SqlHelper;

@ServerEndpoint("/adminPage/logTail/{id}")
@Controller
public class LogTailController {

	private Process process;
	private InputStream inputStream;


	/**
	 * 新的WebSocket请求开启
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("id") String id) {

		ApplicationContext act = ApplicationContextRegister.getApplicationContext();
		SqlHelper sqlHelper = act.getBean(SqlHelper.class);

		try {
			// 执行tail -f命令
			Log log = sqlHelper.findById(id, Log.class);
			process = Runtime.getRuntime().exec("tail -f " + log.getPath());
			inputStream = process.getInputStream();

			// 一定要启动新的线程，防止InputStream阻塞处理WebSocket的线程
			TailLogThread thread = new TailLogThread(inputStream, session);
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * WebSocket请求关闭
	 */
	@OnClose
	public void onClose() {
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (process != null)
			process.destroy();
	}

	@OnError
	public void onError(Throwable thr) {
		thr.printStackTrace();
	}
}
