package com.pi.core.framebuffer;

import com.pi.core.util.GPUObject;

// Stubclass declared abstract until it is implemented
public abstract class RenderBuffer implements GPUObject, FrameBufferAttachable {
	@Override
	public int getID() {
		return -1; // TODO
	}

	@Override
	public void gpuAlloc() {
		// TODO
	}

	@Override
	public void gpuUpload() {
		// TODO
	}

	@Override
	public void gpuFree() {
		// TODO
	}

}
