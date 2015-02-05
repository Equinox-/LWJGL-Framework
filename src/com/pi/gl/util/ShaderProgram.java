package com.pi.gl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.io.FileUtil;

public class ShaderProgram implements Bindable, GLIdentifiable {
	private static ShaderProgram currentShader;
	private int programID;
	private final List<Integer> attachedObjects;

	public ShaderProgram() {
		this.programID = GL20.glCreateProgram();
		attachedObjects = new ArrayList<>(2);
	}

	private static int compileShader(InputStream src, int type)
			throws InstantiationException, IOException {
		int shader = GL20.glCreateShader(type);
		try {
			GL20.glShaderSource(shader, FileUtil.readStreamFully(src));
		} catch (IOException e) {
			GL20.glDeleteShader(shader);
			throw e;
		}
		GL20.glCompileShader(shader);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			String log = GL20.glGetShaderInfoLog(shader, 1024);
			GL20.glDeleteShader(shader);
			throw new InstantiationException("Shader compile failure: \n" + log);
		}
		return shader;
	}

	public ShaderProgram fragment(InputStream src) {
		try {
			attachedObjects.add(compileShader(src, GL20.GL_FRAGMENT_SHADER));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ShaderProgram vertex(InputStream src) {
		try {
			attachedObjects.add(compileShader(src, GL20.GL_VERTEX_SHADER));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ShaderProgram link() {
		for (int obj : attachedObjects)
			GL20.glAttachShader(programID, obj);
		GL20.glLinkProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println("Shader program wasn't linked correctly.");
			System.err.println(GL20.glGetProgramInfoLog(programID, 1024));
			throw new RuntimeException();
		}
		return this;
	}

	public void dispose() {
		unbind();
		for (int obj : attachedObjects) {
			GL20.glDetachShader(programID, obj);
			GL20.glDeleteShader(obj);
		}
		GL20.glDeleteProgram(programID);
		programID = -1;
	}

	@Override
	public int getID() {
		return programID;
	}

	@Override
	public void bind() {
		if (programID == -1)
			throw new RuntimeException("Attempted to bind a disposed shader.");
		GL20.glUseProgram(programID);
		currentShader = this;
	}

	public static void unbind() {
		GL20.glUseProgram(0);
	}

	public static ShaderProgram current() {
		return currentShader;
	}
}
