package com.pi.core.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Screenshot {
	public static void screenshot(File file, int x0, int y0, int w, int h) throws IOException {
		GL11.glReadBuffer(GL11.GL_FRONT);
		ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);
		GL11.glReadPixels(x0, y0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
				buffer);
		BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int i = (x + (w * y)) * 4;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				image.setRGB(x, h - (y + 1), (0xFF << 24) | (r << 16)
						| (g << 8) | b);
			}
		}

		ImageIO.write(image, "PNG", file);
	}
}
