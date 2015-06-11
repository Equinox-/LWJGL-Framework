package com.pi.core.glsl;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.pi.core.buffers.BufferAccessHint;
import com.pi.core.buffers.BufferModifyHint;
import com.pi.core.buffers.BufferType;
import com.pi.core.buffers.GLGenericBuffer;

public class ShaderUniformBlock {
	private final int blockIndex;
	private final String blockName;
	private final ShaderProgram parent;
	private final int length;
	private GLGenericBuffer bound;

	public ShaderUniformBlock(ShaderProgram parent, int blockIndex,
			String blockName) {
		this.parent = parent;
		this.blockName = blockName;
		this.blockIndex = blockIndex;

		this.length = GL31.glGetActiveUniformBlocki(parent.getID(), blockIndex,
				GL31.GL_UNIFORM_BLOCK_DATA_SIZE);

		System.out.println("Shader uniform block by the name of " + blockName
				+ " [index=" + blockIndex + ", len=" + length + "]");

	}

	public String name() {
		return blockName;
	}

	public void bound(GLGenericBuffer b) {
		if (b.type() != BufferType.UNIFORM)
			throw new IllegalArgumentException("Invalid buffer type.");
		if (b.size() < length)
			throw new IllegalArgumentException("Invalid buffer length.");
		this.bound = b;
		GL31.glUniformBlockBinding(parent.getID(), blockIndex, blockIndex);
		GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, blockIndex, b.getID());
	}

	public GLGenericBuffer bound() {
		if (bound == null) {
			bound(new GLGenericBuffer(length).type(BufferType.UNIFORM)
					.modify(BufferModifyHint.STREAM)
					.access(BufferAccessHint.DRAW).gpuAlloc());
		}
		return bound;
	}

	public boolean dirty;

	public void upload() {
		dirty = false;
		bound().gpuUpload();
	}
}
