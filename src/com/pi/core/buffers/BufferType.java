package com.pi.core.buffers;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

public enum BufferType {
	ARRAY(GL15.GL_ARRAY_BUFFER), COPY_READ(GL31.GL_COPY_READ_BUFFER),
	COPY_WRITE(GL31.GL_COPY_WRITE_BUFFER), ELEMENT_ARRAY(
			GL15.GL_ELEMENT_ARRAY_BUFFER),
	PIXEL_PACK(GL21.GL_PIXEL_PACK_BUFFER), PIXEL_UNPACK(
			GL21.GL_PIXEL_UNPACK_BUFFER), TEXTURE(GL31.GL_TEXTURE_BUFFER),
	TRANSFORM_FEEDBACK(GL30.GL_TRANSFORM_FEEDBACK_BUFFER), UNIFORM(
			GL31.GL_UNIFORM_BUFFER);
	private final int code;

	private BufferType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}
}
