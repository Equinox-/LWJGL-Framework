package com.pi.core.debug;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.Arrays;

import javax.swing.JFrame;

public class LivePlotter extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final int Y_TOP_PAD = 50;
	private static final int GEN_PAD = 20;

	private BufferStrategy strat;
	private final float[][] data;
	private final Color[] legend;
	private final int[] scale;
	private final int chunks;
	private int curr = 0;

	public LivePlotter(int chunks, Color[] leg, int[] scale) {
		setSize(500, 500);
		setVisible(true);
		createBufferStrategy(2);
		strat = getBufferStrategy();
		this.legend = leg;
		this.chunks = chunks;
		this.data = new float[chunks][leg.length];
		this.scale = scale;
	}

	private int x(int x) {
		return (getWidth() - 2 * GEN_PAD) * (x - 1) / (chunks - 1) + GEN_PAD;
	}

	private int y(float y, float min, float max) {
		if (max == min)
			return (getHeight() - 2 * GEN_PAD - Y_TOP_PAD) / 2 + Y_TOP_PAD + GEN_PAD;
		return (int) ((getHeight() - 2 * GEN_PAD - Y_TOP_PAD) * (1 - (y - min) / (max - min))) + Y_TOP_PAD + GEN_PAD;
	}

	public void log(float... v) {
		if (v.length != legend.length)
			throw new IllegalArgumentException("Must provide " + v.length + " entries");
		data[curr] = v;
		float[] minima = new float[chunks];
		float[] maxima = new float[chunks];
		Arrays.fill(minima, Float.MAX_VALUE);
		Arrays.fill(maxima, Float.MIN_VALUE);
		for (int j = 0; j < legend.length; j++)
			for (int i = 0; i < chunks; i++) {
				minima[scale[j]] = Math.min(minima[scale[j]], data[i][j]);
				maxima[scale[j]] = Math.max(maxima[scale[j]], data[i][j]);
			}

		Graphics g = strat.getDrawGraphics();
		g.clearRect(0, 0, getWidth(), getHeight());
		for (int j = 0; j < legend.length; j++) {
			g.setColor(legend[j]);
			for (int i = 1; i < chunks; i++) {
				int l = (i + curr) % chunks;
				int l2 = (i + 1 + curr) % chunks;
				g.drawLine(x(i), y(data[l][j], minima[scale[j]], maxima[scale[j]]), x(i + 1),
						y(data[l2][j], minima[scale[j]], maxima[scale[j]]));
			}
		}
		g.dispose();
		strat.show();

		curr = (curr + 1) % chunks;
	}
}
