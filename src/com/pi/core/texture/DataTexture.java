package com.pi.core.texture;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.pi.math.vector.Vector;
import com.pi.math.vector.VectorBuff;

public class DataTexture extends Texture {
	private FloatBuffer backing;
	public final Vector[][] vectors;

	private int stashFormat;

	private static int floatFormatForDimension(int dimension) {
		if (dimension > 4 || dimension < 1)
			throw new IllegalArgumentException(
					"Vector textures must have a dimension between 1 and 4");
		switch (dimension) {
		case 1:
			return GL30.GL_R32F;
		case 2:
			return GL30.GL_RG32F;
		case 3:
			return GL30.GL_RGB32F;
		case 4:
			return GL30.GL_RGBA32F;
		default:
			throw new IllegalArgumentException("Illegal dimension"); // This should never happen
		}
	}

	public DataTexture(int dimension, int width, int height) {
		super(width, height, floatFormatForDimension(dimension));
		super.wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE); // Typical, but can be changed.
		super.filter(null, TextureFilter.NEAREST, TextureFilter.NEAREST);
		super.mipmapLevels(0);

		switch (dimension) {
		case 1:
			stashFormat = GL11.GL_RED;
			break;
		case 2:
			stashFormat = GL30.GL_RG;
			break;
		case 3:
			stashFormat = GL11.GL_RGB;
			break;
		case 4:
			stashFormat = GL11.GL_RGBA;
			break;
		}

		vectors = new Vector[width][height];
		backing = BufferUtils.createFloatBuffer(width * height * dimension);
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				vectors[i][j] = new VectorBuff(backing, ((j * width) + i)
						* dimension, dimension);
			}
		}
	}

	@Override
	public void gpuUpload() {
		super.bind();
		super.commitParameters();
		backing.position(0);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, super.getWidth(),
				super.getHeight(), stashFormat, GL11.GL_FLOAT, backing);
		super.unbind();
	}

	@Override
	public void gpuDownload() {
		super.bind();
		backing.position(0);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, stashFormat, GL11.GL_FLOAT,
				backing);
		super.unbind();
	}

	@Override
	public Texture mipmapLevels(int mipmapLevels) {
		throw new UnsupportedOperationException();
	}
}
