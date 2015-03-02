package com.pi.core.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

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
	}

	@Override
	public void gpuUpload() {
		boolean hasAlpha = img.getColorModel().hasAlpha();
		ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth()
				* img.getHeight() * (hasAlpha ? 4 : 3));
		for (int y = 0; y < getHeight(); ++y) {
			for (int x = 0; x < getWidth(); ++x) {
				int argb = img.getRGB(x, y);
				data.put((byte) ((argb >> 16) & 0xff));
				data.put((byte) ((argb >> 8) & 0xff));
				data.put((byte) (argb & 0xff));
				if (hasAlpha) {
					data.put((byte) ((argb >> 24) & 0xff));
				}
			}
		}
		data.flip();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA
				: GL11.GL_RGB, getWidth(), getHeight(), 0,
				hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
				data);

	}
}
