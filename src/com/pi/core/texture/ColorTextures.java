package com.pi.core.texture;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class ColorTextures {
	private static Map<Long, ColorTextures> basic = new HashMap<>();

	public static ColorTextures textures() {
		long ctx = GL.getCurrent().getPointer();
		ColorTextures res = basic.get(ctx);
		if (res == null)
			basic.put(ctx, res = new ColorTextures());
		return res;
	}

	public static void removeTextures() {
		textures().gpuFree();
		basic.remove(GL.getCurrent().getPointer());
	}

	private Map<Integer, Texture> colorTextures = new HashMap<>();

	public Texture getColorTexture(int argb) {
		Texture t = colorTextures.get(argb);
		if (t == null) {
			colorTextures.put(argb, t = new Texture(1, 1, GL11.GL_RGBA));
			t.gpuAllocInternal();
			ByteBuffer data = BufferUtils.createByteBuffer(4);
			data.put((byte) ((argb >> 16) & 0xFF));
			data.put((byte) ((argb >> 8) & 0xFF));
			data.put((byte) ((argb >> 0) & 0xFF));
			data.put((byte) ((argb >> 24) & 0xFF));
			data.flip();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, t.getID());
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 1, 1,
					GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
		}
		return t;
	}

	public void gpuFree() {
		for (Texture t : colorTextures.values())
			t.gpuFreeInternal();
		colorTextures.clear();
	}
}
