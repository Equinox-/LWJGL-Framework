package com.pi.util;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.sun.glass.events.KeyEvent;

public class JavaToGLFW {
	private static final Map<Integer, Integer> jToG = new HashMap<>();
	private static final Map<Integer, Integer> gToJ = new HashMap<>();

	private static void ins(int java, int glfw) {
		if (jToG.containsKey(java))
			throw new IllegalStateException("Java key already registered");
		jToG.put(java, glfw);
		if (gToJ.containsKey(glfw))
			throw new IllegalStateException("OpenGL key already registered");
		gToJ.put(glfw, java);
	}

	public static int javaToGLFW(int java) {
		Integer i = jToG.get(java);
		return i != null ? i : -1;
	}

	public static int glfwToJava(int glfw) {
		return gToJ.get(glfw);
	}

	static {
		// ins(KeyEvent.VK_DEAD_TILDE, GLFW.GLFW_KEY_GRAVE_ACCENT);
		ins(KeyEvent.VK_1, GLFW.GLFW_KEY_1);
		ins(KeyEvent.VK_2, GLFW.GLFW_KEY_2);
		ins(KeyEvent.VK_3, GLFW.GLFW_KEY_3);
		ins(KeyEvent.VK_4, GLFW.GLFW_KEY_4);
		ins(KeyEvent.VK_5, GLFW.GLFW_KEY_5);
		ins(KeyEvent.VK_6, GLFW.GLFW_KEY_6);
		ins(KeyEvent.VK_7, GLFW.GLFW_KEY_7);
		ins(KeyEvent.VK_8, GLFW.GLFW_KEY_8);
		ins(KeyEvent.VK_9, GLFW.GLFW_KEY_9);
		ins(KeyEvent.VK_0, GLFW.GLFW_KEY_0);
		ins(KeyEvent.VK_UNDERSCORE, GLFW.GLFW_KEY_MINUS);
		ins(KeyEvent.VK_EQUALS, GLFW.GLFW_KEY_EQUAL);
		ins(KeyEvent.VK_BACKSPACE, GLFW.GLFW_KEY_BACKSPACE);
		ins(KeyEvent.VK_Q, GLFW.GLFW_KEY_Q);
		ins(KeyEvent.VK_W, GLFW.GLFW_KEY_W);
		ins(KeyEvent.VK_E, GLFW.GLFW_KEY_E);
		ins(KeyEvent.VK_R, GLFW.GLFW_KEY_R);
		ins(KeyEvent.VK_T, GLFW.GLFW_KEY_T);
		ins(KeyEvent.VK_Y, GLFW.GLFW_KEY_Y);
		ins(KeyEvent.VK_U, GLFW.GLFW_KEY_U);
		ins(KeyEvent.VK_I, GLFW.GLFW_KEY_I);
		ins(KeyEvent.VK_O, GLFW.GLFW_KEY_O);
		ins(KeyEvent.VK_P, GLFW.GLFW_KEY_P);
		// left square
		// right square
		ins(KeyEvent.VK_BACK_SLASH, GLFW.GLFW_KEY_BACKSLASH);
		ins(KeyEvent.VK_CAPS_LOCK, GLFW.GLFW_KEY_CAPS_LOCK);
		ins(KeyEvent.VK_A, GLFW.GLFW_KEY_A);
		ins(KeyEvent.VK_S, GLFW.GLFW_KEY_S);
		ins(KeyEvent.VK_D, GLFW.GLFW_KEY_D);
		ins(KeyEvent.VK_F, GLFW.GLFW_KEY_F);
		ins(KeyEvent.VK_G, GLFW.GLFW_KEY_G);
		ins(KeyEvent.VK_H, GLFW.GLFW_KEY_H);
		ins(KeyEvent.VK_J, GLFW.GLFW_KEY_J);
		ins(KeyEvent.VK_K, GLFW.GLFW_KEY_K);
		ins(KeyEvent.VK_L, GLFW.GLFW_KEY_L);
		ins(KeyEvent.VK_COLON, GLFW.GLFW_KEY_SEMICOLON);
		ins(KeyEvent.VK_DOUBLE_QUOTE, GLFW.GLFW_KEY_APOSTROPHE);
		ins(KeyEvent.VK_ENTER, GLFW.GLFW_KEY_ENTER);
		// ins(KeyEvent.VK_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
		ins(KeyEvent.VK_Z, GLFW.GLFW_KEY_Z);
		ins(KeyEvent.VK_X, GLFW.GLFW_KEY_X);
		ins(KeyEvent.VK_C, GLFW.GLFW_KEY_C);
		ins(KeyEvent.VK_V, GLFW.GLFW_KEY_V);
		ins(KeyEvent.VK_B, GLFW.GLFW_KEY_B);
		ins(KeyEvent.VK_N, GLFW.GLFW_KEY_N);
		ins(KeyEvent.VK_M, GLFW.GLFW_KEY_M);
		ins(KeyEvent.VK_COMMA, GLFW.GLFW_KEY_COMMA);
		ins(KeyEvent.VK_PERIOD, GLFW.GLFW_KEY_PERIOD);
		ins(KeyEvent.VK_SLASH, GLFW.GLFW_KEY_SLASH); // Forward slash
		// ins(KeyEvent.VK_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
		// ins(KeyEvent.VK_CONTROL, GLFW.GLFW_KEY_LEFT_CONTROL);
		// ins(KeyEvent.VK_WINDOWS, GLFW.GLFW_KEY_LEFT_SUPER);
		// ins(KeyEvent.VK_ALT, GLFW.GLFW_KEY_LEFT_ALT);
		ins(KeyEvent.VK_SPACE, GLFW.GLFW_KEY_SPACE);
		// ins(KeyEvent.VK_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
		// ins(KeyEvent.VK_WINDOWS, GLFW.GLFW_KEY_RIGHT_SUPER);
		// ins(KeyEvent.VK_CONTEXT_MENU, GLFW.GLFW_KEY_MENU);
		// ins(KeyEvent.VK_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL)
		ins(KeyEvent.VK_INSERT, GLFW.GLFW_KEY_INSERT);
		ins(KeyEvent.VK_HOME, GLFW.GLFW_KEY_HOME);
		ins(KeyEvent.VK_PAGE_UP, GLFW.GLFW_KEY_PAGE_UP);
		ins(KeyEvent.VK_PAGE_DOWN, GLFW.GLFW_KEY_PAGE_DOWN);
		ins(KeyEvent.VK_END, GLFW.GLFW_KEY_END);
		ins(KeyEvent.VK_DELETE, GLFW.GLFW_KEY_DELETE);
		ins(KeyEvent.VK_UP, GLFW.GLFW_KEY_UP);
		ins(KeyEvent.VK_DOWN, GLFW.GLFW_KEY_DOWN);
		ins(KeyEvent.VK_LEFT, GLFW.GLFW_KEY_LEFT);
		ins(KeyEvent.VK_RIGHT, GLFW.GLFW_KEY_RIGHT);
		ins(KeyEvent.VK_NUM_LOCK, GLFW.GLFW_KEY_NUM_LOCK);
		ins(KeyEvent.VK_NUMPAD0, GLFW.GLFW_KEY_KP_0);
		ins(KeyEvent.VK_NUMPAD1, GLFW.GLFW_KEY_KP_1);
		ins(KeyEvent.VK_NUMPAD2, GLFW.GLFW_KEY_KP_2);
		ins(KeyEvent.VK_NUMPAD3, GLFW.GLFW_KEY_KP_3);
		ins(KeyEvent.VK_NUMPAD4, GLFW.GLFW_KEY_KP_4);
		ins(KeyEvent.VK_NUMPAD5, GLFW.GLFW_KEY_KP_5);
		ins(KeyEvent.VK_NUMPAD6, GLFW.GLFW_KEY_KP_6);
		ins(KeyEvent.VK_NUMPAD7, GLFW.GLFW_KEY_KP_7);
		ins(KeyEvent.VK_NUMPAD8, GLFW.GLFW_KEY_KP_8);
		ins(KeyEvent.VK_NUMPAD9, GLFW.GLFW_KEY_KP_9);
		// ins(KeyEvent.VK_PLUS, GLFW.GLFW_KEY_KP_ADD);
		// ins(KeyEvent.VK_ASTERISK, GLFW.GLFW_KEY_KP_MULTIPLY);
		// ins(KeyEvent.VK_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT);
		// ins(KeyEvent.VK_SLASH, GLFW.GLFW_KEY_KP_DIVIDE);
		// ins(KeyEvent.VK_ENTER, GLFW.GLFW_KEY_KP_ENTER);
		// ins(KeyEvent.VK_PERIOD, GLFW.GLFW_KEY_KP_DOT);
		ins(KeyEvent.VK_ESCAPE, GLFW.GLFW_KEY_ESCAPE);
		ins(KeyEvent.VK_F1, GLFW.GLFW_KEY_F1);
		ins(KeyEvent.VK_F2, GLFW.GLFW_KEY_F2);
		ins(KeyEvent.VK_F3, GLFW.GLFW_KEY_F3);
		ins(KeyEvent.VK_F4, GLFW.GLFW_KEY_F4);
		ins(KeyEvent.VK_F5, GLFW.GLFW_KEY_F5);
		ins(KeyEvent.VK_F6, GLFW.GLFW_KEY_F6);
		ins(KeyEvent.VK_F7, GLFW.GLFW_KEY_F7);
		ins(KeyEvent.VK_F8, GLFW.GLFW_KEY_F8);
		ins(KeyEvent.VK_F9, GLFW.GLFW_KEY_F9);
		ins(KeyEvent.VK_F10, GLFW.GLFW_KEY_F10);
		ins(KeyEvent.VK_F11, GLFW.GLFW_KEY_F11);
		ins(KeyEvent.VK_F12, GLFW.GLFW_KEY_F12);
		ins(KeyEvent.VK_F13, GLFW.GLFW_KEY_F13);
		ins(KeyEvent.VK_F14, GLFW.GLFW_KEY_F14);
		ins(KeyEvent.VK_F15, GLFW.GLFW_KEY_F15);
		ins(KeyEvent.VK_F16, GLFW.GLFW_KEY_F16);
		ins(KeyEvent.VK_F17, GLFW.GLFW_KEY_F17);
		ins(KeyEvent.VK_F18, GLFW.GLFW_KEY_F18);
		ins(KeyEvent.VK_F19, GLFW.GLFW_KEY_F19);
		ins(KeyEvent.VK_F20, GLFW.GLFW_KEY_F20);
		ins(KeyEvent.VK_F21, GLFW.GLFW_KEY_F21);
		ins(KeyEvent.VK_F22, GLFW.GLFW_KEY_F22);
		ins(KeyEvent.VK_F23, GLFW.GLFW_KEY_F23);
		ins(KeyEvent.VK_F24, GLFW.GLFW_KEY_F24);
		// ins(KeyEvent.VK_F25, GLFW.GLFW_KEY_F25);
		ins(KeyEvent.VK_PRINTSCREEN, GLFW.GLFW_KEY_PRINT_SCREEN);
		ins(KeyEvent.VK_SCROLL_LOCK, GLFW.GLFW_KEY_SCROLL_LOCK);
		ins(KeyEvent.VK_PAUSE, GLFW.GLFW_KEY_PAUSE);
	}
}
