package com.pi.core.framebuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.pi.core.texture.Texture;
import com.pi.core.util.Bindable;
import com.pi.core.util.GPUObject;

public class ResizingFrameBuffer extends GPUObject<ResizingFrameBuffer>
		implements Bindable {
	private FrameBuffer target;
	private Texture color;
	private Texture depth;

	public ResizingFrameBuffer() {
	}

	private int width, height;

	public void size(int w, int h) {
		this.width = w;
		this.height = h;
	}

	protected void verify() {
		if (target == null || color.getWidth() != width
				|| color.getHeight() != height) {
			if (target != null) {
				target.gpuFree();
				color.gpuFree();
				depth.gpuFree();
			}
			color = new Texture(width, height, GL11.GL_RGB).gpuAlloc();
			depth = new Texture(width, height, GL30.GL_DEPTH_COMPONENT32F)
					.gpuAlloc();
			target = new FrameBuffer();
			target.attachColor(color);
			target.attachDepth(depth);
			target.gpuAlloc();
		}
	}

	@Override
	protected void gpuAllocInternal() {
		// Let verify do its work
	}

	@Override
	protected void gpuFreeInternal() {
		if (target != null)
			target.gpuFree();
		if (color != null)
			color.gpuFree();
		if (depth != null)
			depth.gpuFree();
	}


	@Override
	public void bind() {
		verify();
		target.bind();
	}

	public Texture color() {
		verify();
		return color;
	}

	public Texture depth() {
		verify();
		return depth;
	}

	public FrameBuffer fbo() {
		verify();
		return target;
	}
}
