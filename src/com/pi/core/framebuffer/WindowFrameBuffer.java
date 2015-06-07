package com.pi.core.framebuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.pi.core.texture.Texture;
import com.pi.core.util.Bindable;
import com.pi.core.util.GPUObject;
import com.pi.core.wind.GLWindow;

public class WindowFrameBuffer extends GPUObject<WindowFrameBuffer> implements
		Bindable {
	private FrameBuffer target;
	private Texture color;
	private Texture depth;
	private final GLWindow gl;

	public WindowFrameBuffer(GLWindow gl) {
		this.gl = gl;
	}

	private void verify() {
		if (target == null || color.getWidth() != gl.getEvents().getWidth()
				|| color.getHeight() != gl.getEvents().getHeight()) {
			if (target != null) {
				target.gpuFree();
				color.gpuFree();
				depth.gpuFree();
			}
			color = new Texture(gl.getEvents().getWidth(), gl.getEvents()
					.getHeight(), GL11.GL_RGB).gpuAlloc();
			depth = new Texture(gl.getEvents().getWidth(), gl.getEvents()
					.getHeight(), GL30.GL_DEPTH_COMPONENT32F).gpuAlloc();
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
	protected WindowFrameBuffer me() {
		return this;
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
