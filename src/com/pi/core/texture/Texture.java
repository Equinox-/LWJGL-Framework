package com.pi.core.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL45;

import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;

public abstract class Texture implements GPUObject, GLIdentifiable, Bindable {
	private static Texture currentTexture;

	private static final int[][] MIPMAP_FILTER_TABLE;
	static {
		TextureFilter[] filters = TextureFilter.values();
		MIPMAP_FILTER_TABLE = new int[filters.length][filters.length];
		for (int i = 0; i < filters.length; i++) {
			for (int j = 0; j < filters.length; j++) {
				String fName = "GL_" + filters[i].name() + "_MIPMAP_"
						+ filters[j].name();
				try {
					MIPMAP_FILTER_TABLE[i][j] = GL11.class.getField(fName)
							.getInt(null);
				} catch (Exception e) {
					System.err.println("Failed to find value for " + fName);
					MIPMAP_FILTER_TABLE[i][j] = -1;
				}
			}
		}
	}

	private int texture;
	private int internalFormat, mipmapLevels;

	private final int width, height;

	private TextureWrap sWrap, tWrap;
	private TextureFilter mipmapFilter;
	private TextureFilter minFilter, magFilter;

	public Texture(int width, int height) {
		this.width = width;
		this.height = height;
		this.texture = -1;

		this.sWrap = TextureWrap.REPEAT;
		this.tWrap = TextureWrap.REPEAT;

		this.mipmapFilter = TextureFilter.NEAREST;
		this.minFilter = TextureFilter.LINEAR;
		this.magFilter = TextureFilter.LINEAR;
	}

	public Texture wrap(TextureWrap sWrap, TextureWrap tWrap) {
		if (sWrap == null)
			throw new IllegalArgumentException("SWrap can't be null");
		if (tWrap == null)
			throw new IllegalArgumentException("TWrap can't be null");
		this.sWrap = sWrap;
		this.tWrap = tWrap;
		return this;
	}

	public Texture filter(TextureFilter mipmap, TextureFilter minFilter,
			TextureFilter magFilter) {
		if (minFilter == null)
			throw new IllegalArgumentException("Minifying filter can't be null");
		if (magFilter == null)
			throw new IllegalArgumentException(
					"Magnification filter can't be null");
		if (mipmap != null) {
			// Validate the minFilter
			if (MIPMAP_FILTER_TABLE[mipmap.ordinal()][minFilter.ordinal()] == -1)
				throw new IllegalArgumentException("The mimap filter "
						+ mipmap.name() + " and minifying filter "
						+ minFilter.name() + " aren't compatible");
		}
		this.mipmapFilter = mipmap;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		return this;
	}

	public Texture mipmapLevels(int mipmapLevels) {
		if (texture >= 0)
			throw new IllegalStateException(
					"Can't change the number of mipmap levels when texture is allocated.");
		this.mipmapLevels = mipmapLevels;
		return this;
	}

	public Texture internalFormat(int format) {
		if (texture >= 0)
			throw new IllegalStateException(
					"Can't change the internal format when texture is allocated.");
		this.internalFormat = format;
		return this;
	}

	/**
	 * The texture MUST be bound for this to work.
	 */
	protected void commitParameters() {
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				sWrap.glID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				tWrap.glID);
		GL11.glTexParameteri(
				GL11.GL_TEXTURE_2D,
				GL11.GL_TEXTURE_MIN_FILTER,
				mipmapFilter != null ? MIPMAP_FILTER_TABLE[mipmapFilter
						.ordinal()][minFilter.ordinal()] : minFilter.glID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				magFilter.ordinal());
	}

	@Override
	public void gpuAlloc() {
		if (texture >= 0)
			gpuFree();
		texture = GL11.glGenTextures();
		bind();
		if (GL.getCapabilities().OpenGL45) {
			GL45.glTextureStorage2D(GL11.GL_TEXTURE_2D, mipmapLevels,
					internalFormat, width, height);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL12.GL_TEXTURE_BASE_LEVEL, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL,
					mipmapLevels);
			int tmpW = width;
			int tmpH = height;
			for (int level = 0; level <= mipmapLevels; level++) {
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, internalFormat,
						tmpW, tmpH, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
						(ByteBuffer) null);
				tmpW >>= 1;
				tmpH >>= 1;
			}
		}
		unbind();
	}

	@Override
	public void gpuFree() {
		if (texture < 0)
			return;
		GL11.glDeleteTextures(texture);
		texture = -1;
	}

	@Override
	public int getID() {
		return texture;
	}

	@Override
	public void bind() {
		if (texture < 0)
			throw new RuntimeException("Can't bind an unallocated texture.");
		if (currentTexture == this)
			return;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		currentTexture = this;
	}

	public static void unbind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		currentTexture = null;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
