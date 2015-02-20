package com.pi.core.model;

import org.lwjgl.opengl.GL11;

public enum PrimitiveType {
	POINTS(GL11.GL_POINTS), LINES(GL11.GL_LINES),
	LINE_STRIP(GL11.GL_LINE_STRIP), LINE_LOOP(GL11.GL_LINE_LOOP), TRIANGLES(
			GL11.GL_TRIANGLES), TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
	TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN);
	private final int glMode;

	private PrimitiveType(int mode) {
		this.glMode = mode;
	}

	public int mode() {
		return glMode;
	}
}
