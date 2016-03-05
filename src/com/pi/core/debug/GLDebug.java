package com.pi.core.debug;

import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.MemoryUtil;

import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.framebuffer.FrameBuffer;
import com.pi.core.glsl.ShaderProgram;
import com.pi.core.misc.VertexArrayObject;
import com.pi.core.texture.Texture;

public class GLDebug {
	private static boolean reportStacks = false;
	private static DebugMode mode = DebugMode.NONE;
	public static boolean reportNotifications = false;
	private static int id = 0;

	private static final GLDebugMessageCallback debug_callback = new GLDebugMessageCallback() {
		@Override
		public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
			debug(source, type, id, severity, length, message, userParam);
		}
	};

	private static final GLDebugMessageARBCallback arb_debug_callback = new GLDebugMessageARBCallback() {
		@Override
		public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
			debug(source, type, id, severity, length, message, userParam);
		}
	};

	public static boolean checkError() {
		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR) {
			System.err.println("Internal OpenGL Error: \t" + error);
			System.err.println(Thread.currentThread().getStackTrace()[2]);
			return true;
		}
		return false;
	}

	private static void debug(int source, int type, int id, int severity, int length, long message, long userParam) {
		boolean error = (severity != KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION);
		if (!error && !reportNotifications)
			return;
		(error ? System.err : System.out).printf("Source:%s\tType:%s\tID:%d\tSeverity:%s\tMessage:%s\n",
				debugSource(source), debugType(type), id, debugSeverity(severity), MemoryUtil.memDecodeASCII(message));
		if (reportStacks && error) {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for (int i = 5; i < Math.min(15, stack.length); i++)
				System.err.println("\tat " + stack[i]);
		}
	}

	private static String debugSeverity(int severity) {
		switch (severity) {
		case ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB:
			return "High";
		case ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB:
			return "Medium";
		case ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB:
			return "Low";
		case KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION:
			return "Notification";
		default:
			return "Unknown[0x" + Integer.toString(severity, 16) + "]";
		}
	}

	private static String debugSource(int source) {
		switch (source) {
		case ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB:
			return "OpenGL";
		case ARBDebugOutput.GL_DEBUG_SOURCE_APPLICATION_ARB:
			return "Application";
		case ARBDebugOutput.GL_DEBUG_SOURCE_OTHER_ARB:
			return "Other";
		case ARBDebugOutput.GL_DEBUG_SOURCE_SHADER_COMPILER_ARB:
			return "Shader Compiler";
		case ARBDebugOutput.GL_DEBUG_SOURCE_THIRD_PARTY_ARB:
			return "Third Party";
		case ARBDebugOutput.GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB:
			return "Window System";
		default:
			return "Unknown[0x" + Integer.toString(source, 16) + "]";
		}
	}
	private static String debugType(int type) {
		switch (type) {
		case ARBDebugOutput.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB:
			return "Deprecated behavior";
		case ARBDebugOutput.GL_DEBUG_TYPE_ERROR_ARB:
			return "Error";
		case ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB:
			return "Other";
		case ARBDebugOutput.GL_DEBUG_TYPE_PERFORMANCE_ARB:
			return "Performance";
		case ARBDebugOutput.GL_DEBUG_TYPE_PORTABILITY_ARB:
			return "Portability";
		case ARBDebugOutput.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB:
			return "Undefined behavior";
		case KHRDebug.GL_DEBUG_TYPE_MARKER:
			return "Marker";
		case KHRDebug.GL_DEBUG_TYPE_POP_GROUP:
			return "Pop Group";
		case KHRDebug.GL_DEBUG_TYPE_PUSH_GROUP:
			return "Push Group";
		default:
			return "Unknown[0x" + Integer.toString(type, 16) + "]";
		}
	}

	public static boolean isDebugging() {
		return mode == DebugMode.ARB || mode == DebugMode.GL43 || mode == DebugMode.KHR;
	}

	public static void nameObject(FrameBuffer t, String name) {
		if (!t.allocated())
			throw new IllegalArgumentException("Can only name allocated objects.");
		nameObject(GL30.GL_FRAMEBUFFER, t.getID(), name);
	}

	public static void nameObject(GLGenericBuffer t, String name) {
		if (!t.allocated())
			throw new IllegalArgumentException("Can only name allocated objects.");
		nameObject(KHRDebug.GL_BUFFER, t.getID(), name);
	}

	public static void nameObject(int type, int id, String name) {
		switch (mode) {
		case GL43:
			GL43.glObjectLabel(type, id, name);
			break;
		case KHR:
			KHRDebug.glObjectLabel(type, id, name);
			break;
		default:
			// Ignore
		}
	}

	public static void nameObject(ShaderProgram t, String name) {
		if (!t.allocated())
			throw new IllegalArgumentException("Can only name allocated objects.");
		nameObject(KHRDebug.GL_PROGRAM, t.getID(), name);
	}

	public static void nameObject(Texture t, String name) {
		if (!t.allocated())
			throw new IllegalArgumentException("Can only name allocated objects.");
		nameObject(GL11.GL_TEXTURE, t.getID(), name);
	}

	public static void nameObject(VertexArrayObject t, String name) {
		if (!t.allocated())
			throw new IllegalArgumentException("Can only name allocated objects.");
		nameObject(GL11.GL_VERTEX_ARRAY, t.getID(), name);
	}

	public static void popDebug() {
		switch (mode) {
		case GL43:
			GL43.glPopDebugGroup();
			break;
		case KHR:
			KHRDebug.glPopDebugGroup();
			break;
		default:
			// Ignore
		}
	}

	public static void pushDebug(String group) {
		switch (mode) {
		case GL43:
			GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, id++, MemoryUtil.memEncodeASCII(group));
			break;
		case KHR:
			KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, id++, MemoryUtil.memEncodeASCII(group));
			break;
		default:
			// Ignore
		}
	}

	public static boolean setupDebugOutput(boolean stack) {
		if (GL.getCapabilities().OpenGL43) {
			if (stack)
				GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			else
				GL11.glDisable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			GL43.nglDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, 0, 0, true);
			GL43.glDebugMessageCallback(debug_callback, 0);
			if (!checkError()) {
				reportStacks = true;
				mode = DebugMode.GL43;
				return true;
			}
		}
		if (GL.getCapabilities().GL_KHR_debug) {
			if (stack)
				GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			else
				GL11.glDisable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			KHRDebug.nglDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, 0, 0, true);
			KHRDebug.glDebugMessageCallback(debug_callback, 0);
			if (!checkError()) {
				reportStacks = true;
				mode = DebugMode.KHR;
				return true;
			}
		}
		if (GL.getCapabilities().GL_ARB_debug_output) {
			if (stack)
				GL11.glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
			else
				GL11.glDisable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
			ARBDebugOutput.nglDebugMessageControlARB(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, 0, 0,
					true);
			ARBDebugOutput.glDebugMessageCallbackARB(arb_debug_callback, 0);
			if (!checkError()) {
				reportStacks = true;
				mode = DebugMode.ARB;
				return true;
			}
		}
		return false;
	}

	private static enum DebugMode {
		GL43,
		KHR,
		ARB,
		NONE;
	}
}
