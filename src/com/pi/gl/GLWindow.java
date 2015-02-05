package com.pi.gl;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

public abstract class GLWindow {
	private long windowID;
	private boolean running;
	GLFWErrorCallback errorCallback;
	GLFWKeyCallback keyCallback;

	public GLWindow() {
		if (GLFW.glfwInit() != GL11.GL_TRUE)
			throw new RuntimeException("Unable to initialize GLFW");

		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,
				GLFW.GLFW_OPENGL_CORE_PROFILE);

		windowID = GLFW.glfwCreateWindow(640, 480, "Window", MemoryUtil.NULL,
				MemoryUtil.NULL);

		if (windowID == MemoryUtil.NULL) {
			System.err.println("Error creating a window");
			System.exit(1);
		}

		GLFW.glfwMakeContextCurrent(windowID);
		GLContext.createFromCurrent();

		GLFW.glfwSwapInterval(1);
	}

	public abstract void init();

	public abstract void render();

	public abstract void dispose();

	public void start() {

		errorCallback = Callbacks.errorCallbackPrint(System.err);
		keyCallback = GLFW.GLFWKeyCallback(this::onKeyEvent);
		GLFW.glfwSetErrorCallback(errorCallback);
		GLFW.glfwSetKeyCallback(windowID, keyCallback);

		init();

		running = true;
		while (running && GLFW.glfwWindowShouldClose(windowID) != GL11.GL_TRUE) {
			render();
			GLFW.glfwPollEvents();
			GLFW.glfwSwapBuffers(windowID);
		}

		dispose();
		keyCallback.release();
		errorCallback.release();

		GLFW.glfwDestroyWindow(windowID);
		GLFW.glfwTerminate();
		System.exit(0);
	}

	public long getWindowID() {
		return windowID;
	}

	public void shutdown() {
		running = false;
	}

	public void onKeyEvent(long window, int key, int scancode, int action,
			int mods) {
		if (key == GLFW.GLFW_KEY_ESCAPE && action != GLFW.GLFW_RELEASE)
			shutdown();
	}

}