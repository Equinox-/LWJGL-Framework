package com.pi.core.glsl;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.pi.core.buffers.BufferAccessHint;
import com.pi.core.buffers.BufferModifyHint;
import com.pi.core.buffers.BufferType;
import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.debug.FrameCounter;
import com.pi.core.debug.FrameCounter.FrameParam;
import com.pi.util.ReferenceTable;

public class ShaderUniformBlock {
	/**
	 * A persistent buffer means that the buffer only gets refreshed if it is
	 * changed.
	 */
	public static final boolean PERSISTENT_BUFFER_STATE = true;
	/**
	 * If true allow the usage of the ShaderUniform family of functions to
	 * assign to uniform blocks.
	 */
	public static final boolean ALLOW_UTILITY_ACCESS = false;

	private static final ReferenceTable<GLGenericBuffer> bound_ubos = new ReferenceTable<>(128);

	private final int blockIndex;
	private final String blockName;
	private final int length;
	private GLGenericBuffer bound;

	public int dirtyMin;

	public int dirtyMax;

	public ShaderUniformBlock(ShaderProgram parent, int blockIndex, String blockName) {
		this.blockName = blockName;
		this.blockIndex = blockIndex;

		this.length = GL31.glGetActiveUniformBlocki(parent.getID(), blockIndex, GL31.GL_UNIFORM_BLOCK_DATA_SIZE);
		GL31.glUniformBlockBinding(parent.getID(), blockIndex, blockIndex);

		// System.out.println(
		// "Shader uniform block by the name of " + blockName + " [index=" +
		// blockIndex + ", len=" + length + "]");
	}

	public GLGenericBuffer bound() {
		if (bound == null) {
			bound(new GLGenericBuffer(length).type(BufferType.UNIFORM).modify(BufferModifyHint.STREAM)
					.access(BufferAccessHint.DRAW).gpuAlloc());
		}
		return bound;
	}

	public void bound(GLGenericBuffer b) {
		if (b.type() != BufferType.UNIFORM)
			throw new IllegalArgumentException("Invalid buffer type.");
		if (b.size() < length)
			throw new IllegalArgumentException("Invalid buffer length.");
		this.bound = b;
	}

	public int length() {
		return length;
	}

	public void markDirty(int min, int max) {
		this.dirtyMin = Math.min(dirtyMin, min);
		this.dirtyMax = Math.max(dirtyMax, max);
	}

	public String name() {
		return blockName;
	}

	public void recheckBinding() {
		if (blockIndex < bound_ubos.size()) {
			if (bound_ubos.isAttached(blockIndex, bound))
				return;
			bound_ubos.attach(blockIndex, bound);
		}
		GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, blockIndex, bound.getID());
		FrameCounter.increment(FrameParam.UNIFORM_BUFFER_INDEXED);
	}

	public void upload() {
		bound().gpuUpload();
		dirtyMin = Integer.MAX_VALUE;
		dirtyMax = Integer.MIN_VALUE;
	}

	public void uploadIfNeeded() {
		if (ShaderUniformBlock.PERSISTENT_BUFFER_STATE) {
			if (dirtyMin >= dirtyMax)
				return;
			bound().gpuUploadPartial(dirtyMin, dirtyMax);
			dirtyMin = Integer.MAX_VALUE;
			dirtyMax = Integer.MIN_VALUE;
		} else {
			upload();
		}
	}
}
