package com.github.zxkane.wechat.moments;

public class ExitException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int exitCode;

	public ExitException(int exitCode) {
		this.exitCode = exitCode;
	}
}
