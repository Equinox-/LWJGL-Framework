package com.pi.core.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class GLError {
	public static boolean checkError() {
		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR) {
			System.err.println("Internal OpenGL Error: \t"
					+ GLContext.translateGLErrorString(error));
			System.err.println(Thread.currentThread().getStackTrace()[2]);
			return true;
		}
		return false;
	}
}
