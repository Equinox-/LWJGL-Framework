package com.pi.core.wind;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import com.pi.core.debug.FrameCounter;
import com.pi.core.debug.WarningManager;
import com.pi.core.model.BasicShapes;
import com.pi.core.texture.ColorTextures;
import com.pi.math.BufferProvider;

public abstract class GLWindow {
	private final long windowID;
	private boolean running;

	private final GLWindowEvents windowEvents;
	private GLFWErrorCallback errorCallback;

	private final boolean fullscreen;
	private final boolean doubleBuffered;

	protected boolean frameCounter = false;

	public GLWindow() {
		this(3, 3);
	}

	public GLWindow(int major, int minor) {
		this(major, minor, 0, false, false, true);
	}

	public GLWindow(int major, int minor, int samples, boolean debug, boolean fullscreen, boolean doubleBuffered) {
		if (GLFW.glfwInit() != GL11.GL_TRUE)
			throw new RuntimeException("Unable to initialize GLFW");
		BufferProvider.provider(new BufferProvider() {
			@Override
			protected FloatBuffer nFloatBuffer(int n) {
				return BufferUtils.createFloatBuffer(n);
			}

			@Override
			protected ByteBuffer nByteBuffer(int n) {
				return BufferUtils.createByteBuffer(n);
			}
		});

		if (!Sys.getVersion().equals("3.0.0b build 35"))
			System.err.println(
					"Warn: LWJGL-Framework was designed for LWJGL 3.0.0b build 35, this version of LWJGL is version "
							+ Sys.getVersion());

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, major);
		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, samples);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, minor);
		if (this.doubleBuffered = doubleBuffered)
			GLFW.glfwWindowHint(GLFW.GLFW_DOUBLE_BUFFER, GL11.GL_TRUE);
		else
			GLFW.glfwWindowHint(GLFW.GLFW_DOUBLE_BUFFER, GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, debug ? GL11.GL_TRUE : GL11.GL_FALSE);
		if (major >= 3)
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
		if (major > 4 || (major == 3 && minor >= 3))
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		else
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_ANY_PROFILE);

		this.fullscreen = fullscreen;
		int w = 1280;
		int h = 720;
		long monitor = fullscreen ? GLFW.glfwGetPrimaryMonitor() : MemoryUtil.NULL;
		if (monitor != MemoryUtil.NULL) {
			IntBuffer tmp = GLFW.glfwGetVideoMode(monitor).asIntBuffer();
			w = tmp.get(0);
			h = tmp.get(1);
		}
		windowID = GLFW.glfwCreateWindow(w, h, "Window", monitor, MemoryUtil.NULL);

		if (windowID == MemoryUtil.NULL) {
			System.err.println("Error creating a window");
			System.exit(1);
		}

		windowEvents = new GLWindowEvents(this);

		GLFW.glfwMakeContextCurrent(windowID);
		if (doubleBuffered)
			GLFW.glfwSwapInterval(1);
		GLFW.glfwShowWindow(windowID);
	}

	public abstract void init();

	public abstract void render();

	public abstract void update();

	public abstract void dispose();

	public void start() {
		errorCallback = Callbacks.errorCallbackPrint(System.err);
		GLFW.glfwSetErrorCallback(errorCallback);
		running = true;

		windowEvents.bind();

		GL.createCapabilities();
		init();

		while (valid()) {
			boolean fc = frameCounter;
			if (fc)
				FrameCounter.counter().beginFrameRender();
			render();
			if (fc)
				FrameCounter.counter().switchRenderToUpdate();
			GLFW.glfwPollEvents();
			update();
			if (fc)
				FrameCounter.counter().switchUpdateToSwap();
			if (doubleBuffered)
				GLFW.glfwSwapBuffers(windowID);
			else
				GL11.glFinish();
			if (fc)
				FrameCounter.counter().endFrameSwap();
		}

		dispose();
		errorCallback.release();
		windowEvents.release();

		// Kill BasicShapes
		BasicShapes.removeShapes();
		ColorTextures.removeTextures();

		GLFW.glfwDestroyWindow(windowID);
		GLFW.glfwTerminate();
		WarningManager.termReferenceWatch();
	}

	public float fps() {
		return FrameCounter.counter().fps();
	}

	public boolean valid() {
		return running && GLFW.glfwWindowShouldClose(windowID) != GL11.GL_TRUE;
	}

	public GLWindowEvents getEvents() {
		return windowEvents;
	}

	public long getWindowID() {
		return windowID;
	}

	public void shutdown() {
		running = false;
	}

	public void setTitle(String s) {
		GLFW.glfwSetWindowTitle(windowID, s);
	}

	public void setSize(int w, int h) {
		if (fullscreen)
			throw new UnsupportedOperationException("Not allowing resolution changes in full screen context");
		GLFW.glfwSetWindowSize(windowID, w, h);
	}

	public void setPosition(int x, int y) {
		GLFW.glfwSetWindowPos(windowID, x, y);
	}
}
