package com.pi.core.buffers;

public enum BufferAccessHint {
	/**
	 * The user will be writing data to the buffer, but the user will not read it. 
	 */
	DRAW,
	/**
	 * The user will not be writing data, but the user will be reading it back.
	 */
	READ,
	/**
	 * The user will be neither writing nor reading the data.
	 */
	COPY
}
