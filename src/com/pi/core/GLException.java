package com.pi.core;

public class GLException extends RuntimeException {
	public GLException(String string, String log) {
		super(string + ":\n" + log);
	}

	public GLException(String string, Exception log) {
		super(string, log);
	}

	public GLException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = 1L;

}
