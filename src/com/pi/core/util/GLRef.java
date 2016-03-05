package com.pi.core.util;

public class GLRef {
	public static final int NULL = 0;

	public static final boolean isNull(int r) {
		return r <= NULL;
	}

	public static final boolean notNull(int r) {
		return r > NULL;
	}
}
