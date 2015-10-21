package com.pi.core.texture;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import com.pi.core.debug.FrameCounter;
import com.pi.core.debug.FrameCounter.FrameParam;
import com.pi.core.framebuffer.FrameBufferAttachable;
import com.pi.core.util.Bindable;
import com.pi.core.util.GPUObject;

public class Texture extends GPUObject<Texture> implements Bindable, FrameBufferAttachable {
	private static final int MAX_TEXTURE_UNITS = 64;
	private static int activeTextureUnit = 0;
	@SuppressWarnings("unchecked")
	private static final WeakReference<Texture>[] currentTexture = new WeakReference[MAX_TEXTURE_UNITS];

	private static final int[][] MIPMAP_FILTER_TABLE;

	static {
		TextureFilter[] filters = TextureFilter.values();
		MIPMAP_FILTER_TABLE = new int[filters.length][filters.length];
		for (int i = 0; i < filters.length; i++) {
			for (int j = 0; j < filters.length; j++) {
				String fName = "GL_" + filters[i].name() + "_MIPMAP_" + filters[j].name();
				try {
					MIPMAP_FILTER_TABLE[i][j] = GL11.class.getField(fName).getInt(null);
				} catch (Exception e) {
					System.err.println("Failed to find value for " + fName);
					MIPMAP_FILTER_TABLE[i][j] = -1;
				}
			}
		}
	}

	private int texture;
	private final int internalFormat;

	private final int width, height;
	private int mipmapLevels;

	private TextureWrap sWrap, tWrap;
	private TextureFilter mipmapFilter;
	private TextureFilter minFilter, magFilter;

	public Texture(int width, int height, int internalFormat) {
		this.width = width;
		this.height = height;
		this.internalFormat = internalFormat;

		this.texture = -1;

		this.sWrap = TextureWrap.REPEAT;
		this.tWrap = TextureWrap.REPEAT;

		this.mipmapFilter = TextureFilter.NEAREST;
		this.minFilter = TextureFilter.LINEAR;
		this.magFilter = TextureFilter.LINEAR;

		this.mipmapLevels = 0;
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

	public Texture filter(TextureFilter mipmap, TextureFilter minFilter, TextureFilter magFilter) {
		if (minFilter == null)
			throw new IllegalArgumentException("Minifying filter can't be null");
		if (magFilter == null)
			throw new IllegalArgumentException("Magnification filter can't be null");
		if (mipmap != null) {
			// Validate the minFilter
			if (MIPMAP_FILTER_TABLE[mipmap.ordinal()][minFilter.ordinal()] == -1)
				throw new IllegalArgumentException("The mimap filter " + mipmap.name() + " and minifying filter "
						+ minFilter.name() + " aren't compatible");
		}
		this.mipmapFilter = mipmap;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		return this;
	}

	public Texture mipmapLevels(int mipmapLevels) {
		if (texture >= 0)
			throw new IllegalStateException("Can't change the number of mipmap levels when texture is allocated.");
		this.mipmapLevels = mipmapLevels;
		return this;
	}

	/**
	 * The texture MUST be bound for this to work.
	 */
	protected void commitParameters() {
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, sWrap.glID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, tWrap.glID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmapFilter != null
				? MIPMAP_FILTER_TABLE[mipmapFilter.ordinal()][minFilter.ordinal()] : minFilter.glID);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter.glID);
	}

	private static final int[] DEPTH_FORMATS = { GL11.GL_DEPTH_COMPONENT, GL14.GL_DEPTH_COMPONENT16,
			GL14.GL_DEPTH_COMPONENT24, GL14.GL_DEPTH_COMPONENT32, GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH32F_STENCIL8,
			GL30.GL_DEPTH_COMPONENT32F };

	static {
		Arrays.sort(DEPTH_FORMATS);
	}

	@Override
	protected void gpuAllocInternal() {
		if (texture >= 0)
			gpuFreeInternal();
		texture = GL11.glGenTextures();
		bind();
		if (GL.getCapabilities().OpenGL45) {
			GL45.glTextureStorage2D(GL11.GL_TEXTURE_2D, mipmapLevels, internalFormat, width, height);
		} else {
			int allocFmt = GL11.GL_RED;
			if (Arrays.binarySearch(DEPTH_FORMATS, internalFormat) >= 0)
				allocFmt = GL11.GL_DEPTH_COMPONENT;

			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipmapLevels);
			int tmpW = width;
			int tmpH = height;
			for (int level = 0; level <= mipmapLevels; level++) {
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, internalFormat, tmpW, tmpH, 0, allocFmt,
						GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				tmpW >>= 1;
				tmpH >>= 1;
			}
		}
		commitParameters();
		unbind();
	}

	@Override
	protected void gpuFreeInternal() {
		if (texture < 0)
			return;
		GL11.glDeleteTextures(texture);
		texture = -1;
	}

	@Override
	public int getID() {
		return texture;
	}

	public static void glActiveTexture(int n) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + n);
		activeTextureUnit = n;
	}

	@Override
	public void bind() {
		if (texture < 0)
			throw new RuntimeException("Can't bind an unallocated texture.");
		if (currentTexture[activeTextureUnit] != null && currentTexture[activeTextureUnit].get() == this)
			return;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		currentTexture[activeTextureUnit] = new WeakReference<>(this);
		FrameCounter.increment(FrameParam.TEXTURE_BINDS);
	}

	public void bind(int unit) {
		if (texture < 0)
			throw new RuntimeException("Can't bind an unallocated texture.");
		if (currentTexture[unit] != null && currentTexture[unit].get() == this)
			return;
		glActiveTexture(unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		currentTexture[unit] = new WeakReference<>(this);
		FrameCounter.increment(FrameParam.TEXTURE_BINDS);
	}

	public static void unbind() {
		if (currentTexture[activeTextureUnit] == null || currentTexture[activeTextureUnit].get() == null)
			return;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		currentTexture[activeTextureUnit] = null;
		FrameCounter.increment(FrameParam.TEXTURE_BINDS);
	}

	public static void unbind(int unit) {
		if (currentTexture[unit] == null || currentTexture[unit].get() == null)
			return;
		glActiveTexture(unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		currentTexture[activeTextureUnit] = null;
		FrameCounter.increment(FrameParam.TEXTURE_BINDS);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void cpuFree() {
		
	}
}
