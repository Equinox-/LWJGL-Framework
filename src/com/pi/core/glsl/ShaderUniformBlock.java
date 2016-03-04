package com.pi.core.glsl;

import java.lang.ref.WeakReference;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.pi.core.buffers.BufferAccessHint;
import com.pi.core.buffers.BufferModifyHint;
import com.pi.core.buffers.BufferType;
import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.debug.FrameCounter;
import com.pi.core.debug.FrameCounter.FrameParam;

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

	@SuppressWarnings("unchecked")
	private static final WeakReference<GLGenericBuffer>[] bound_ubos = new WeakReference[128];

	private final int blockIndex;
	private final String blockName;
	private final int length;
	private GLGenericBuffer bound;

	public ShaderUniformBlock(ShaderProgram parent, int blockIndex, String blockName) {
		this.blockName = blockName;
		this.blockIndex = blockIndex;

		this.length = GL31.glGetActiveUniformBlocki(parent.getID(), blockIndex, GL31.GL_UNIFORM_BLOCK_DATA_SIZE);
		GL31.glUniformBlockBinding(parent.getID(), blockIndex, blockIndex);

		// System.out.println(
		// "Shader uniform block by the name of " + blockName + " [index=" +
		// blockIndex + ", len=" + length + "]");
	}

	public String name() {
		return blockName;
	}

	public int length() {
		return length;
	}

	public void bound(GLGenericBuffer b) {
		if (b.type() != BufferType.UNIFORM)
			throw new IllegalArgumentException("Invalid buffer type.");
		if (b.size() < length)
			throw new IllegalArgumentException("Invalid buffer length.");
		this.bound = b;
	}

	public GLGenericBuffer bound() {
		if (bound == null) {
			bound(new GLGenericBuffer(length).type(BufferType.UNIFORM).modify(BufferModifyHint.STREAM)
					.access(BufferAccessHint.DRAW).gpuAlloc());
		}
		return bound;
	}

	public int dirtyMin;
	public int dirtyMax;

	public void markDirty(int min, int max) {
		this.dirtyMin = Math.min(dirtyMin, min);
		this.dirtyMax = Math.max(dirtyMax, max);
	}

	public void recheckBinding() {
		if (blockIndex < bound_ubos.length) {
			if (bound_ubos[blockIndex] != null && bound_ubos[blockIndex].get() == bound)
				return;
			bound_ubos[blockIndex] = new WeakReference<>(bound);
		}
		GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, blockIndex, bound.getID());
		FrameCounter.increment(FrameParam.UNIFORM_BUFFER_INDEXED);
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

	public void upload() {
		bound().gpuUpload();
		dirtyMin = Integer.MAX_VALUE;
		dirtyMax = Integer.MIN_VALUE;
	}
}
