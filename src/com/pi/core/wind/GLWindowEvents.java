package com.pi.core.wind;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class GLWindowEvents {
	private final GLWindow attached;
	private GLFWKeyCallback keyCallback;
	private GLFWScrollCallback scrollCallback;
	private GLFWWindowSizeCallback sizeCallback;
	private GLFWCursorPosCallback cursorCallback;

	public GLWindowEvents(GLWindow window) {
		this.attached = window;
	}

	public void bind() {
		keyCallback = GLFW.GLFWKeyCallback(this::onKeyEvent);
		scrollCallback = GLFW.GLFWScrollCallback(this::onScrollEvent);
		sizeCallback = GLFW.GLFWWindowSizeCallback(this::onSizeEvent);
		cursorCallback = GLFW.GLFWCursorPosCallback(this::onCursorPosEvent);
		GLFW.glfwSetKeyCallback(attached.getWindowID(), keyCallback);
		GLFW.glfwSetScrollCallback(attached.getWindowID(), scrollCallback);
		GLFW.glfwSetWindowSizeCallback(attached.getWindowID(), sizeCallback);
		GLFW.glfwSetCursorPosCallback(attached.getWindowID(), cursorCallback);

		// Init values
		scrollPosX = scrollPosY = 0;
		IntBuffer tmpA = BufferUtils.createIntBuffer(1);
		IntBuffer tmpB = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(attached.getWindowID(), tmpA, tmpB);
		width = tmpA.get(0);
		height = tmpB.get(0);

		mouseX = width / 2;
		mouseY = height / 2;
	}

	public void release() {
		keyCallback.release();
		keyCallback = null;
		scrollCallback.release();
		scrollCallback = null;
		cursorCallback.release();
		cursorCallback = null;
	}

	private void onKeyEvent(long window, int key, int scancode, int action,
			int mods) {
		if (window != attached.getWindowID())
			return;
		if (key == GLFW.GLFW_KEY_ESCAPE && action != GLFW.GLFW_RELEASE)
			attached.shutdown();
	}

	private double scrollPosX, scrollPosY;

	private void onScrollEvent(long window, double dx, double dy) {
		if (window != attached.getWindowID())
			return;
		// TODO Precision loss if there is a lot of scrolling in one direction
		scrollPosX += dx;
		scrollPosY += dy;
	}

	public double getScrollX() {
		return scrollPosX;
	}

	public double getScrollY() {
		return scrollPosY;
	}

	private int width, height;

	private void onSizeEvent(long window, int width, int height) {
		if (window != attached.getWindowID())
			return;
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private double mouseX, mouseY;

	private void onCursorPosEvent(long window, double x, double y) {
		if (window != attached.getWindowID())
			return;
		this.mouseX = x;
		this.mouseY = y;
	}

	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

	public boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(attached.getWindowID(), key) == GLFW.GLFW_PRESS;
	}

	public boolean isMouseButtonDown(int btn) {
		return GLFW.glfwGetMouseButton(attached.getWindowID(), btn) == GLFW.GLFW_PRESS;
	}
}
