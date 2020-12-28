package com.cym.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.Ssh;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SSHClientUtils {
	private static Logger logger = LoggerFactory.getLogger(SSHClientUtils.class);

	public static class MyUserInfo implements UserInfo {
		private Logger logger = LoggerFactory.getLogger(getClass());
		private String password;

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public boolean promptPassword(String message) {
			return true;
		}

		@Override
		public boolean promptPassphrase(String message) {
			return true;
		}

		@Override
		public boolean promptYesNo(String message) {
			return true;
		}

		@Override
		public void showMessage(String message) {
			logger.info(message);
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	/**
	 * 直接执行命令
	 */
	public static String exec(Ssh ssh, String cmd) {
		JSch jsch = new JSch();
		String outStr = "";
		try {
			Session session = jsch.getSession(ssh.getUsername(), ssh.getHost(), ssh.getPort());
			MyUserInfo ui = new MyUserInfo();
			ui.setPassword(ssh.getPassword());
			session.setUserInfo(ui);
			session.connect();
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(cmd);
			InputStream in = channel.getInputStream();
			OutputStream out = channel.getOutputStream();
			((ChannelExec) channel).setErrStream(System.err);

			channel.connect();
			out.flush();

			outStr = IOUtils.toString(in, Charset.forName("utf-8"));
			logger.info(outStr);
			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outStr;
	}

}