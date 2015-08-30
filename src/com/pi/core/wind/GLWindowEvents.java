package com.pi.core.wind;

import java.lang.reflect.Method;
import java.nio.IntBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class GLWindowEvents {
	private final GLWindow attached;
	private final List<EventListener> listeners = Collections.synchronizedList(new ArrayList<EventListener>());
	private GLFWKeyCallback keyCallback;
	private GLFWScrollCallback scrollCallback;
	private GLFWWindowSizeCallback sizeCallback;
	private GLFWCursorPosCallback cursorCallback;
	private GLFWCharCallback charCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;
	private GLFWFramebufferSizeCallback fbSizeCallback;

	private BlockingQueue<Entry<Method, Object[]>> eventQueue = new LinkedBlockingQueue<>();

	private static Method onKeyEvent, onScrollEvent, onSizeEvent, onCursorPosEvent, onCharEvent, onMouseButtonEvent;

	static {
		try {
			onKeyEvent = GLWindowEvents.class.getDeclaredMethod("onKeyEvent", long.class, int.class, int.class,
					int.class, int.class);
			onScrollEvent = GLWindowEvents.class.getDeclaredMethod("onScrollEvent", long.class, double.class,
					double.class);
			onSizeEvent = GLWindowEvents.class.getDeclaredMethod("onSizeEvent", long.class, int.class, int.class);
			onCursorPosEvent = GLWindowEvents.class.getDeclaredMethod("onCursorPosEvent", long.class, double.class,
					double.class, int.class);
			onCharEvent = GLWindowEvents.class.getDeclaredMethod("onCharEvent", long.class, int.class);
			onMouseButtonEvent = GLWindowEvents.class.getDeclaredMethod("onMouseButtonEvent", long.class, int.class,
					int.class, int.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Thread eventProcessor;

	public GLWindowEvents(final GLWindow window) {
		this.attached = window;
	}

	@SuppressWarnings({ "synthetic-access", "deprecation" })
	public void bind() {
		keyCallback = GLFW.GLFWKeyCallback(new GLFWKeyCallback.SAM() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (window != attached.getWindowID())
					return;
				eventQueue.add(new AbstractMap.SimpleEntry<>(onKeyEvent,
						new Object[] { System.currentTimeMillis(), key, scancode, action, mods }));
			}
		});
		scrollCallback = GLFW.GLFWScrollCallback(new GLFWScrollCallback.SAM() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				if (window != attached.getWindowID())
					return;
				eventQueue.add(new AbstractMap.SimpleEntry<>(onScrollEvent,
						new Object[] { System.currentTimeMillis(), xoffset, yoffset }));
			}
		});
		fbSizeCallback = GLFW.GLFWFramebufferSizeCallback(new GLFWFramebufferSizeCallback.SAM() {
			@Override
			public void invoke(long window, int width, int height) {
				if (window != attached.getWindowID())
					return;
				eventQueue.add(new AbstractMap.SimpleEntry<>(onSizeEvent,
						new Object[] { System.currentTimeMillis(), width, height }));
			}
		});
		sizeCallback = GLFW.GLFWWindowSizeCallback(new GLFWWindowSizeCallback.SAM() {
			@Override
			public void invoke(long window, int width, int height) {
				if (window != attached.getWindowID())
					return;
				GLWindowEvents.this.width = width;
				GLWindowEvents.this.height = height;
			}
		});
		cursorCallback = GLFW.GLFWCursorPosCallback(new GLFWCursorPosCallback.SAM() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				if (window != attached.getWindowID())
					return;
				int mods = 0;
				if (isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))
					mods |= GLFW.GLFW_MOD_SHIFT;
				if (isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) || isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT))
					mods |= GLFW.GLFW_MOD_ALT;
				if (isKeyDown(GLFW.GLFW_KEY_LEFT_SUPER) || isKeyDown(GLFW.GLFW_KEY_RIGHT_SUPER))
					mods |= GLFW.GLFW_MOD_SUPER;
				if (isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL))
					mods |= GLFW.GLFW_MOD_CONTROL;
				eventQueue.add(new AbstractMap.SimpleEntry<>(onCursorPosEvent,
						new Object[] { System.currentTimeMillis(), xpos, ypos, mods }));
			}
		});
		charCallback = GLFW.GLFWCharCallback(new GLFWCharCallback.SAM() {
			@Override
			public void invoke(long window, int codepoint) {
				if (window != attached.getWindowID())
					return;
				eventQueue.add(new AbstractMap.SimpleEntry<>(onCharEvent,
						new Object[] { System.currentTimeMillis(), codepoint }));
			}
		});
		mouseButtonCallback = GLFW.GLFWMouseButtonCallback(new GLFWMouseButtonCallback.SAM() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				if (window != attached.getWindowID())
					return;
				eventQueue.add(new AbstractMap.SimpleEntry<>(onMouseButtonEvent,
						new Object[] { System.currentTimeMillis(), button, action, mods }));
			}
		});
		GLFW.glfwSetKeyCallback(attached.getWindowID(), keyCallback);
		GLFW.glfwSetScrollCallback(attached.getWindowID(), scrollCallback);
		GLFW.glfwSetWindowSizeCallback(attached.getWindowID(), sizeCallback);
		GLFW.glfwSetFramebufferSizeCallback(attached.getWindowID(), fbSizeCallback);
		GLFW.glfwSetCursorPosCallback(attached.getWindowID(), cursorCallback);
		GLFW.glfwSetCharCallback(attached.getWindowID(), charCallback);
		GLFW.glfwSetMouseButtonCallback(attached.getWindowID(), mouseButtonCallback);

		// Init values
		scrollPosX = scrollPosY = 0;
		IntBuffer tmpA = BufferUtils.createIntBuffer(1);
		IntBuffer tmpB = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(attached.getWindowID(), tmpA, tmpB);
		width = tmpA.get(0);
		height = tmpB.get(0);

		mouseX = width / 2;
		mouseY = height / 2;

		eventQueue.clear();
		if (eventProcessor != null && eventProcessor.isAlive()) {
			eventProcessor.stop();
		}

		this.eventProcessor = new Thread("Event Processor") {
			@Override
			public void run() {
				System.out.println("Event processor beginning...");
				while (attached.valid()) {
					try {
						Entry<Method, Object[]> event = eventQueue.take();
						event.getKey().invoke(GLWindowEvents.this, event.getValue());
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Event processing failed.");
					}
				}
				System.out.println("Event processor ending...");
			}
		};
		this.eventProcessor.start();
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

	private int mouseButtonStates;
	private float dragStartX, dragStartY;

	protected void onMouseButtonEvent(long time, int button, int action, int mods) {
		for (EventListener l : listeners)
			if (action == GLFW.GLFW_PRESS) {
				mouseButtonStates |= (1 << button);
				dragStartX = mouseX;
				dragStartY = mouseY;
				if (l.mousePressed(time, button, mouseX, mouseY, mods))
					return;
			} else if (action == GLFW.GLFW_RELEASE) {
				mouseButtonStates &= ~(1 << button);
				if (l.mouseReleased(time, button, mouseX, mouseY, mods))
					return;
			}
	}

	protected void onCharEvent(long time, int codepoint) {
		for (EventListener l : listeners)
			if (l.charTyped(time, codepoint))
				return;
	}

	protected void onKeyEvent(long time, int key, int scancode, int action, int mods) {
		for (EventListener l : listeners)
			if (action == GLFW.GLFW_PRESS) {
				if (l.keyPressed(time, key, mods))
					return;
			} else if (action == GLFW.GLFW_RELEASE) {
				if (l.keyReleased(time, key, mods))
					return;
			}
	}

	private float scrollPosX, scrollPosY;

	protected void onScrollEvent(long time, double dx, double dy) {
		scrollPosX += dx;
		scrollPosY += dy;
		for (EventListener l : listeners)
			if (l.scrollChanged(time, (float) dx, (float) dy))
				return;
	}

	public float getScrollX() {
		return scrollPosX;
	}

	public float getScrollY() {
		return scrollPosY;
	}

	private int width, height;

	public int getWindowWidth() {
		return width;
	}

	public int getWindowHeight() {
		return height;
	}

	private int fbWidth, fbHeight;

	// In practice we care about the frame buffer width, not window width
	protected void onSizeEvent(long time, int width, int height) {
		this.fbWidth = width;
		this.fbHeight = height;
		for (EventListener l : listeners)
			if (l.sizeChanged(time, width, height))
				break;
	}

	public int getWidth() {
		return fbWidth;
	}

	public int getHeight() {
		return fbHeight;
	}

	private float mouseX, mouseY;

	protected void onCursorPosEvent(long time, double x, double y, int mods) {
		this.mouseX = (float) x;
		this.mouseY = (float) y;
		for (EventListener l : listeners)
			if (l.mouseMoved(time, (float) x, (float) y, mods))
				return;
	}

	public float getMouseX() {
		return mouseX;
	}

	public float getMouseY() {
		return mouseY;
	}

	public float getDragStartX() {
		return mouseButtonStates == 0 ? mouseX : dragStartX;
	}

	public float getDragStartY() {
		return mouseButtonStates == 0 ? mouseY : dragStartY;
	}

	public boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(attached.getWindowID(), key) == GLFW.GLFW_PRESS;
	}

	public boolean isMouseButtonDown(int btn) {
		return GLFW.glfwGetMouseButton(attached.getWindowID(), btn) == GLFW.GLFW_PRESS;
	}

	private final IntBuffer tmpX = BufferUtils.createIntBuffer(1), tmpY = BufferUtils.createIntBuffer(1);

	public int getWindowX() {
		GLFW.glfwGetWindowPos(attached.getWindowID(), tmpX, tmpY);
		return tmpX.get(0);
	}

	public int getWindowY() {
		GLFW.glfwGetWindowPos(attached.getWindowID(), tmpX, tmpY);
		return tmpY.get(0);
	}
}
