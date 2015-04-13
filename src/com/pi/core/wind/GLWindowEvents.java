package com.pi.core.wind;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class GLWindowEvents {
	private final GLWindow attached;
	private final List<EventListener> listeners = Collections
			.synchronizedList(new ArrayList<EventListener>());
	private GLFWKeyCallback keyCallback;
	private GLFWScrollCallback scrollCallback;
	private GLFWWindowSizeCallback sizeCallback;
	private GLFWCursorPosCallback cursorCallback;
	private GLFWCharCallback charCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;

	public GLWindowEvents(GLWindow window) {
		this.attached = window;
	}

	@SuppressWarnings("synthetic-access")
	public void bind() {
		keyCallback = GLFW.GLFWKeyCallback(new GLFWKeyCallback.SAM() {
			@Override
			public void invoke(long window, int key, int scancode, int action,
					int mods) {
				onKeyEvent(window, key, scancode, action, mods);
			}
		});
		scrollCallback = GLFW.GLFWScrollCallback(new GLFWScrollCallback.SAM() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				onScrollEvent(window, xoffset, yoffset);
			}
		});
		sizeCallback = GLFW
				.GLFWWindowSizeCallback(new GLFWWindowSizeCallback.SAM() {
					@Override
					public void invoke(long window, int width, int height) {
						onSizeEvent(window, width, height);

					}
				});
		cursorCallback = GLFW
				.GLFWCursorPosCallback(new GLFWCursorPosCallback.SAM() {
					@Override
					public void invoke(long window, double xpos, double ypos) {
						onCursorPosEvent(window, xpos, ypos);
					}
				});
		charCallback = GLFW.GLFWCharCallback(new GLFWCharCallback.SAM() {
			@Override
			public void invoke(long window, int codepoint) {
				onCharEvent(window, codepoint);
			}
		});
		mouseButtonCallback = GLFW
				.GLFWMouseButtonCallback(new GLFWMouseButtonCallback.SAM() {
					@Override
					public void invoke(long window, int button, int action,
							int mods) {
						onMouseButtonEvent(window, button, action, mods);
					}
				});
		GLFW.glfwSetKeyCallback(attached.getWindowID(), keyCallback);
		GLFW.glfwSetScrollCallback(attached.getWindowID(), scrollCallback);
		GLFW.glfwSetWindowSizeCallback(attached.getWindowID(), sizeCallback);
		GLFW.glfwSetCursorPosCallback(attached.getWindowID(), cursorCallback);
		GLFW.glfwSetCharCallback(attached.getWindowID(), charCallback);
		GLFW.glfwSetMouseButtonCallback(attached.getWindowID(),
				mouseButtonCallback);

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
		charCallback.release();
		charCallback = null;
		mouseButtonCallback.release();
		mouseButtonCallback = null;
	}

	public void register(EventListener e) {
		listeners.add(e);
	}

	public boolean deregister(EventListener e) {
		return listeners.remove(e);
	}

	private void onMouseButtonEvent(long window, int button, int action,
			int mods) {
		if (window != attached.getWindowID())
			return;
		for (EventListener l : listeners)
			if (action == GLFW.GLFW_PRESS) {
				if (l.mousePressed(button, (float) mouseX, (float) mouseY, mods))
					return;
			} else if (action == GLFW.GLFW_RELEASE) {
				if (l.mouseReleased(button, (float) mouseX, (float) mouseY,
						mods))
					return;
			}
	}

	private void onCharEvent(long window, int codepoint) {
		if (window != attached.getWindowID())
			return;
		for (EventListener l : listeners)
			if (l.charTyped(codepoint))
				return;
	}

	private void onKeyEvent(long window, int key, int scancode, int action,
			int mods) {
		if (window != attached.getWindowID())
			return;
		if (key == GLFW.GLFW_KEY_ESCAPE && action != GLFW.GLFW_RELEASE) {
			attached.shutdown();
			return;
		}
		for (EventListener l : listeners)
			if (action == GLFW.GLFW_PRESS) {
				if (l.keyPressed(key, mods))
					return;
			} else if (action == GLFW.GLFW_RELEASE) {
				if (l.keyReleased(key, mods))
					return;
			}
	}

	private double scrollPosX, scrollPosY;

	private void onScrollEvent(long window, double dx, double dy) {
		if (window != attached.getWindowID())
			return;
		// TODO Precision loss if there is a lot of scrolling in one direction
		scrollPosX += dx;
		scrollPosY += dy;
		for (EventListener l : listeners)
			if (l.scrollChanged((float) dx, (float) dy))
				return;
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
		for (EventListener l : listeners)
			if (l.sizeChanged(width, height))
				break;
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
		int mods = 0;
		if (isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)
				|| isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))
			mods |= GLFW.GLFW_MOD_SHIFT;
		if (isKeyDown(GLFW.GLFW_KEY_LEFT_ALT)
				|| isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT))
			mods |= GLFW.GLFW_MOD_ALT;
		if (isKeyDown(GLFW.GLFW_KEY_LEFT_SUPER)
				|| isKeyDown(GLFW.GLFW_KEY_RIGHT_SUPER))
			mods |= GLFW.GLFW_MOD_SUPER;
		if (isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)
				|| isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL))
			mods |= GLFW.GLFW_MOD_CONTROL;
		for (EventListener l : listeners)
			if (l.mouseMoved((float) x, (float) y, mods))
				return;
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
