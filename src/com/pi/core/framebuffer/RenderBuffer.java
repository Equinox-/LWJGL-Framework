package com.pi.core.framebuffer;

import org.lwjgl.opengl.GL30;

import com.pi.core.util.GPUObject;

public class RenderBuffer extends GPUObject<RenderBuffer>implements FrameBufferAttachable {
	private int glID;
	private final int width, height;
	private final int internalFormat;
	private int msaaSamples;

	public RenderBuffer(int width, int height, int internalFormat) {
		this.width = width;
		this.height = height;
		this.glID = -1;
		this.internalFormat = internalFormat;
		this.msaaSamples = 0;
	}

	public RenderBuffer msaaSamples(int msaaSamples) {
		if (glID >= 0)
			throw new IllegalStateException("Can't change the sample count when the render buffer is allocated.");
		this.msaaSamples = msaaSamples;
		return this;
	}

	public boolean isMultiSampled() {
		return msaaSamples > 0;
	}

	@Override
	public int getID() {
		return glID;
	}

	@Override
	protected void gpuAllocInternal() {
		if (glID >= 0)
			gpuFreeInternal();
		glID = GL30.glGenRenderbuffers();
		if (msaaSamples > 0) {
			GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, msaaSamples, internalFormat, width, height);
		} else {
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, internalFormat, width, height);
		}
	}

	@Override
	protected void gpuFreeInternal() {
		if (glID >= 0)
			GL30.glDeleteRenderbuffers(glID);
		glID = -1;
	}
}
