package com.pi.core.debug;

import java.awt.Color;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class FrameCounter {
	private static final ThreadLocal<FrameCounter> fCounts = ThreadLocal.withInitial(new Supplier<FrameCounter>() {
		@Override
		public FrameCounter get() {
			return new FrameCounter();
		}
	});

	public static FrameCounter counter() {
		return fCounts.get();
	}

	public static void increment(FrameParam p) {
		increment(p, 1);
	}

	public static void increment(FrameParam p, int c) {
		counter().inc(p, c);
	}

	public static enum FrameParam {
		FRAMES,
		BUFFER_BINDS,
		BUFFER_UPLOADS,
		BUFFER_THROUGHPUT("Bpf"),
		VAO_CHANGE,
		SHADER_CHANGE,
		SHADER_DATA_COMMIT,
		TEXTURE_BINDS,
		UNIFORM_BUFFER_INDEXED;

		private String namePad;

		private final String unit;

		private FrameParam(String unit) {
			this.unit = unit;
		}

		private FrameParam() {
			this("pf");
		}

		static {
			int mlen = 0;
			for (FrameParam p : values())
				mlen = Math.max(mlen, p.name().length());
			mlen += 4;
			StringBuilder bs = new StringBuilder(mlen);
			for (FrameParam p : values()) {
				bs.append(p.name());
				while (bs.length() < mlen)
					bs.append(' ');
				p.namePad = bs.toString();
				bs.delete(0, bs.length());
			}
		}
	}

	private final int[] counters = new int[FrameParam.values().length];
	private final int blend; // Blend stats over 10 frames.
	private final long[][] frameTimes;
	private final LivePlotter plot;
	private final PrintStream print;

	private final BlockingQueue<Integer> queueObjects = new LinkedBlockingQueue<>(50);

	private int genQuery() {
		Integer i = queueObjects.poll();
		if (i != null)
			return i.intValue();
		else
			return GL15.glGenQueries();
	}

	private void checkinQuery(int i) {
		if (!queueObjects.offer(i))
			GL15.glDeleteQueries(i);
	}

	private FrameCounter() {
		boolean plot = false;
		if (plot) {
			this.blend = 1;
			// CPU, GPU, UPDATE, SWAP, TOTAL, PRIMITIVES
			this.plot = new LivePlotter(128,
					new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.PINK, Color.MAGENTA, Color.CYAN },
					new int[] { 0, 0, 0, 0, 0, 1 });
		} else {
			this.blend = 10;
			this.plot = null;
		}
		this.print = null;
		this.frameTimes = new long[blend][6];
	}

	public void inc(FrameParam p, int c) {
		counters[p.ordinal()] += c;
		if (p == FrameParam.FRAMES)
			throw new IllegalArgumentException("Frames must use special updating system:  beginFrame/endFrame");
	}

	public void beginFrameRender() {
		checkPrint();
		try {
			Class.forName("com.pi.Core").getMethod("_resetAll").invoke(null);
		} catch (Exception e) {
		}
		final int frames = counters[FrameParam.FRAMES.ordinal()];
		frameTimes[frames][0] = genQuery();
		GL15.glBeginQuery(GL33.GL_TIME_ELAPSED, (int) frameTimes[frames][0]);
		frameTimes[frames][1] = genQuery();
		GL15.glBeginQuery(GL30.GL_PRIMITIVES_GENERATED, (int) frameTimes[frames][1]);
		frameTimes[frames][2] = System.currentTimeMillis();
	}

	public void switchRenderToUpdate() {
		final int frames = counters[FrameParam.FRAMES.ordinal()];
		GL15.glEndQuery(GL30.GL_PRIMITIVES_GENERATED);
		GL15.glEndQuery(GL33.GL_TIME_ELAPSED);

		GL11.glFinish();
		frameTimes[frames][3] = System.currentTimeMillis();

		// Thingy
		try {
			@SuppressWarnings("unchecked")
			List<Object[]> lst = (List<Object[]>) Class.forName("com.pi.Core").getMethod("_getFullReport").invoke(null);
			for (Iterator<Object[]> itr = lst.iterator(); itr.hasNext();) {
				if (!itr.next()[0].toString().startsWith("org.lwjgl.opengl.")) {
					itr.remove();
				}
			}
			// Class.forName("com.pi.Core").getMethod("_printReport",
			// List.class, PrintStream.class, boolean.class)
			// .invoke(null, lst, System.out, false);
			// System.out.println();
		} catch (Exception e) {
		}
	}

	public void switchUpdateToSwap() {
		final int frames = counters[FrameParam.FRAMES.ordinal()];
		frameTimes[frames][4] = System.currentTimeMillis();
	}

	public void endFrameSwap() {
		final int frames = counters[FrameParam.FRAMES.ordinal()];
		GL11.glFinish();
		frameTimes[frames][5] = System.currentTimeMillis();
		counters[FrameParam.FRAMES.ordinal()]++;
	}

	private float lastFPS = 0;

	public float fps() {
		return lastFPS;
	}

	private void checkPrint() {
		final int frames = counters[FrameParam.FRAMES.ordinal()];
		if (frames >= blend) {
			lastFPS = (frames * 1000 / (frameTimes[blend - 1][5] - frameTimes[0][2]));
			if (print != null) {
				print.println(frames + " frames at " + lastFPS + " fps");
				print.println(" frame no\tcpu\tgpu\tupdate\tswap\ttotal\tprimitives");
			}
			for (int i = 0; i < blend; i++) {
				long gpuTime = GL33.glGetQueryObjecti64((int) frameTimes[i][0], GL15.GL_QUERY_RESULT);
				checkinQuery((int) frameTimes[i][0]);
				long primitives = GL33.glGetQueryObjecti64((int) frameTimes[i][1], GL15.GL_QUERY_RESULT);
				checkinQuery((int) frameTimes[i][1]);

				if (print != null) {
					System.out.println(" frame " + i + "\t" + (frameTimes[i][3] - frameTimes[i][2]) + "ms\t"
							+ (int) Math.round(gpuTime / 1000000.0) + "ms\t" + (frameTimes[i][4] - frameTimes[i][3])
							+ "ms\t" + (frameTimes[i][5] - frameTimes[i][4]) + "ms\t"
							+ (frameTimes[i][5] - frameTimes[i][2]) + "ms\t" + primitives);
				}
				if (plot != null) {
					// CPU, GPU, UPDATE, SWAP, PRIMITIVES
					plot.log(frameTimes[i][3] - frameTimes[i][2], Math.round(gpuTime / 1000000.0),
							frameTimes[i][4] - frameTimes[i][3], frameTimes[i][5] - frameTimes[i][4],
							frameTimes[i][5] - frameTimes[i][2], primitives);
				}
			}
			for (FrameParam p : FrameParam.values()) {
				if (p != FrameParam.FRAMES && print != null)
					print.println(p.namePad + (counters[p.ordinal()] / (float) frames) + " " + p.unit);
				counters[p.ordinal()] = 0;
			}
			if (print != null)
				print.println();
		}
	}
}
