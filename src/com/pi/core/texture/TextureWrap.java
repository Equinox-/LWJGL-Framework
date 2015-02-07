package com.pi.core.texture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

public enum TextureWrap {
	CLAMP_TO_EDGE(GL12.GL_CLAMP_TO_EDGE), REPEAT(GL11.GL_REPEAT), MIRRORED_REPEAT(
			GL14.GL_MIRRORED_REPEAT);
	public final int glID;

	private TextureWrap(int id) {
		this.glID = id;
	}
}