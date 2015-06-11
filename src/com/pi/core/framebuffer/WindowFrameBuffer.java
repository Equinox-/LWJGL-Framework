package com.pi.core.framebuffer;

import com.pi.core.wind.GLWindow;

public class WindowFrameBuffer extends ResizingFrameBuffer {
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

	@Override
	public void size(int w, int h) {
		throw new UnsupportedOperationException(
				"Can only resize non-window framebuffers");
	}

	@Override
	protected void verify() {
		super.size(gl.getEvents().getWidth() << shiftSize, gl.getEvents()
				.getHeight() << shiftSize);
		super.verify();
	}
}
