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

	private int shiftSize = 0;

	/**
	 * The shift size is the bitwise shift applied to dimensions. Size multiplier is 2^shift.
	 * 
	 * @param shift
	 *            bitwise shift.
	 * @return this
	 */
	public WindowFrameBuffer shiftSize(int shift) {
		if (shift < 0)
			throw new IllegalArgumentException("Shift must be >= 0");
		this.shiftSize = shift;
		return this;
	}

	public int shiftSize() {
		return shiftSize;
	}

	private void verify() {
		int desW = gl.getEvents().getWidth() << shiftSize;
		int desH = gl.getEvents().getHeight() << shiftSize;
		if (target == null || color.getWidth() != desW
				|| color.getHeight() != desH) {
			if (target != null) {
				target.gpuFree();
				color.gpuFree();
				depth.gpuFree();
			}
			color = new Texture(desW, desH, GL11.GL_RGB).gpuAlloc();
			depth = new Texture(desW, desH, GL30.GL_DEPTH_COMPONENT32F)
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
