package com.pi.core.texture;

import org.lwjgl.opengl.GL11;

public enum TextureFilter {
	NEAREST(GL11.GL_NEAREST), LINEAR(GL11.GL_LINEAR);
	public final int glID;

	private TextureFilter(int id) {
		this.glID = id;
	}
}
