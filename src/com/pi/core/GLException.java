package com.pi.core;

public class GLException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GLException(Exception e) {
		super(e);
	}

	public GLException(String string, Exception log) {
		super(string, log);
	}

	public GLException(String string, String log) {
		super(string + ":\n" + log);
	}

}
