package com.pi.core.vertex;

import java.awt.Color;
import java.nio.ByteBuffer;

public final class BufferColor {
	private final ByteBuffer backer;
	private final int offset;

	public BufferColor(ByteBuffer backer, int offset) {
		this.backer = backer;
		this.offset = offset;
	}

	public void set(int r, int g, int b, int a) {
		backer.put(offset, (byte) r);
		backer.put(offset + 1, (byte) g);
		backer.put(offset + 2, (byte) b);
		backer.put(offset + 3, (byte) a);
	}

	public void set(float r, float g, float b, float a) {
		set((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
	}

	public void set(int r, int g, int b) {
		set(r, g, b, 255);
	}

	public void set(float r, float g, float b) {
		set(r, g, b, 1);
	}

	public void set(int rgb) {
		set((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
	}

	public void setColor(Color src) {
		set(src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha());
	}
	
	public String toString() {
		return (backer.get(offset) & 0xFF) + ","
				+ (backer.get(offset + 1) & 0xFF) + ","
				+ (backer.get(offset + 2) & 0xFF) + ","
				+ (backer.get(offset + 3) & 0xFF);
	}
}