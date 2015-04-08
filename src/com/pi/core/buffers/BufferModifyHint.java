package com.pi.core.buffers;

public enum BufferModifyHint {
	/**
	 * The user will set the data once.
	 */
	STATIC,
	/**
	 * The user will set the data occasionally.
	 */
	DYNAMIC,
	/**
	 * The user will be changing the data after every use. Or almost every use.
	 */
	STREAM;
}
