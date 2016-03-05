package com.pi.core.texture;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class ColorTextures {
	private static ThreadLocal<ColorTextures> basic = ThreadLocal
			.withInitial(new Supplier<ColorTextures>() {
				@Override
				public ColorTextures get() {
					return new ColorTextures();
				}
			});

	public static void removeTextures() {
		textures().gpuFree();
		basic.remove();
	}

	public static ColorTextures textures() {
		return basic.get();
	}

	private final Map<Integer, Texture> colorTextures = new HashMap<>();

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
