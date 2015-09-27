package com.pi.core.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;

public enum PrimitiveType {
	POINTS(GL11.GL_POINTS, 1), LINES(GL11.GL_LINES, 2), LINE_STRIP(
			GL11.GL_LINE_STRIP, 1), LINE_LOOP(GL11.GL_LINE_LOOP, 1), TRIANGLES(
			GL11.GL_TRIANGLES, 3), TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP, 1), TRIANGLE_FAN(
			GL11.GL_TRIANGLE_FAN, 1), PATCHES(GL40.GL_PATCHES, 3);
	private final int glMode;
	private final int stride;

	private PrimitiveType(int mode, int stride) {
		this.glMode = mode;
		this.stride = stride;
	}

	public int mode() {
		return glMode;
	}

	public int stride() {
		return stride;
	}
}
