package com.pi.core.wind;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import com.pi.core.model.BasicShapes;
import com.pi.core.texture.ColorTextures;
import com.pi.math.BufferProvider;

public abstract class GLWindow {
	private final long windowID;
	private boolean running;

	private final GLWindowEvents windowEvents;
	private GLFWErrorCallback errorCallback;

	public GLWindow() {
		this(3, 3);
	}

	public GLWindow(int major, int minor) {
		this(major, minor, 0);
	}

	public GLWindow(int major, int minor, int samples) {
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

		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, major);
		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, samples);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, minor);
		if (major >= 3)
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
		if (major > 4 || (major == 3 && minor >= 3))
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,
					GLFW.GLFW_OPENGL_CORE_PROFILE);
		else
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,
					GLFW.GLFW_OPENGL_ANY_PROFILE);

		windowID = GLFW.glfwCreateWindow(640, 480, "Window", MemoryUtil.NULL,
				MemoryUtil.NULL);

		if (windowID == MemoryUtil.NULL) {
			System.err.println("Error creating a window");
			System.exit(1);
		}

		windowEvents = new GLWindowEvents(this);

		GLFW.glfwMakeContextCurrent(windowID);
		GLContext.createFromCurrent();
		GLFW.glfwSwapInterval(1);
	}

	public abstract void init();

	public abstract void render();

	public abstract void update();

	public abstract void dispose();

	public void start() {
		errorCallback = Callbacks.errorCallbackPrint(System.err);
		GLFW.glfwSetErrorCallback(errorCallback);
		windowEvents.bind();

		init();

		running = true;
		while (running && GLFW.glfwWindowShouldClose(windowID) != GL11.GL_TRUE) {
			render();
			GLFW.glfwPollEvents();
			update();
			GLFW.glfwSwapBuffers(windowID);
		}

		dispose();
		errorCallback.release();
		windowEvents.release();

		// Kill BasicShapes
		BasicShapes.removeShapes();
		ColorTextures.removeTextures();

		GLFW.glfwDestroyWindow(windowID);
		GLFW.glfwTerminate();
		System.exit(0);
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
		GLFW.glfwSetWindowSize(windowID, w, h);
	}

	public void setPosition(int x, int y) {
		GLFW.glfwSetWindowPos(windowID, x, y);
	}
}
