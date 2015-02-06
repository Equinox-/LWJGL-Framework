package com.pi.core.glsl;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.io.FileUtil;

public class ShaderProgram implements Bindable, GLIdentifiable {
	private static ShaderProgram currentShader;

	private int programID;
	private final List<Integer> attachedObjects;

	private Map<String, Integer> uniforms;

	public ShaderProgram() {
		this.uniforms = new HashMap<>();
		this.programID = GL20.glCreateProgram();
		this.attachedObjects = new ArrayList<>(2);
	}

	private static int compileShader(String src, int type)
			throws InstantiationException {
		int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, src);
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
			attachedObjects.add(compileShader(FileUtil.readStreamFully(src),
					GL20.GL_FRAGMENT_SHADER));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ShaderProgram vertex(InputStream src) {
		try {
			attachedObjects.add(compileShader(FileUtil.readStreamFully(src),
					GL20.GL_VERTEX_SHADER));
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
		loadUniforms();
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

	public boolean using() {
		return currentShader == this;
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
		currentShader = null;
	}

	public static ShaderProgram current() {
		return currentShader;
	}

	private void loadUniforms() {
		int uniformCount = GL20.glGetProgrami(programID,
				GL20.GL_ACTIVE_UNIFORMS);
		int uniformLength = GL20.glGetProgrami(programID,
				GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
		IntBuffer sizeBuff = BufferUtils.createIntBuffer(1);
		IntBuffer typeBuff = BufferUtils.createIntBuffer(1);
		for (int i = 0; i < uniformCount; i++) {
			String name = GL20.glGetActiveUniform(programID, i, uniformLength,
					sizeBuff, typeBuff);
			int location = GL20.glGetUniformLocation(programID, name);
			uniforms.put(name, location);
		}
	}

	public int uniform(String name) {
		Integer v = uniforms.get(name);
		if (v == null) {
			v = GL20.glGetUniformLocation(programID, name);
			if (v == -1)
				System.err
						.println("Tried to query shader for invalid uniform: "
								+ name);
			uniforms.put(name, v);
		}
		return v;
	}
}
