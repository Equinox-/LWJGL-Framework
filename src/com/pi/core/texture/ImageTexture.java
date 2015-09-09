package com.pi.core.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ImageTexture extends Texture {
	private BufferedImage img;

	private static BufferedImage loadImage(InputStream f) {
		try {
			return ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public ImageTexture(InputStream f) {
		this(f, GL11.GL_RGBA);
	}

	public ImageTexture(InputStream f, int internalFormat) {
		this(loadImage(f), internalFormat);
	}

	public ImageTexture(BufferedImage img, int internalFormat) {
		super(img.getWidth(), img.getHeight(), internalFormat);
		this.img = img;
		super.mipmapLevels(
				Math.min((int) Math.ceil(Math.log(Math.min(img.getWidth(), img.getHeight())) / Math.log(2)), 10));
	}

	public final BufferedImage getBacking() {
		return img;
	}

	public void cpuFree() {
		img = null;
	}

	@Override
	protected void gpuUploadInternal() {
		ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);
		for (int y = 0; y < getHeight(); ++y) {
			for (int x = 0; x < getWidth(); ++x) {
				int argb = img.getRGB(x, y);
				data.put((byte) ((argb >> 16) & 0xff));
				data.put((byte) ((argb >> 8) & 0xff));
				data.put((byte) (argb & 0xff));
				data.put((byte) ((argb >> 24) & 0xff));
			}
		}
		data.flip();
		bind();
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, getWidth(), getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
				data);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		unbind();
	}
}
