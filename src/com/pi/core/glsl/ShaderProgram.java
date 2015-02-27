package com.pi.core.glsl;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.pi.core.texture.Texture;
import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;
import com.pi.io.FileUtil;

public class ShaderProgram implements Bindable, GLIdentifiable, GPUObject {
	private static ShaderProgram currentShader;

	private int programID;
	private final List<Integer> attachedObjects;

	private Map<String, ShaderUniform> uniforms;

	private final static Map<String, Integer> SHADER_TYPE_MAP;
	static {
		SHADER_TYPE_MAP = new HashMap<>();
		SHADER_TYPE_MAP.put("GL_VERTEX_SHADER", GL20.GL_VERTEX_SHADER);
		SHADER_TYPE_MAP.put("GL_TESS_CONTROL_SHADER",
				GL40.GL_TESS_CONTROL_SHADER);
		SHADER_TYPE_MAP.put("GL_TESS_EVALUATION_SHADER",
				GL40.GL_TESS_EVALUATION_SHADER);
		SHADER_TYPE_MAP.put("GL_GEOMETRY_SHADER", GL32.GL_GEOMETRY_SHADER);
		SHADER_TYPE_MAP.put("GL_FRAGMENT_SHADER", GL20.GL_FRAGMENT_SHADER);
		SHADER_TYPE_MAP.put("GL_COMPUTE_SHADER", GL43.GL_COMPUTE_SHADER);
	}

	Texture[] samplers;

	public ShaderProgram() {
		this.uniforms = new HashMap<>();
		this.attachedObjects = new ArrayList<>(2);
		this.programID = -1;
		this.samplers = new Texture[16];
	}

	@Override
	public void gpuAlloc() {
		if (this.programID >= 0)
			gpuFree();
		this.programID = GL20.glCreateProgram();
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

	public ShaderProgram attach(int type, InputStream src) {
		try {
			attachedObjects.add(compileShader(FileUtil.readStreamFully(src),
					type));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ShaderProgram fragment(InputStream src) {
		return attach(GL20.GL_FRAGMENT_SHADER, src);
	}

	public ShaderProgram vertex(InputStream src) {
		return attach(GL20.GL_VERTEX_SHADER, src);
	}

	public ShaderProgram joined(InputStream src) {
		try {
			String[] lines = FileUtil.readStreamFully(src).split("(\r|)\n");
			StringBuilder tmp = new StringBuilder();
			String type = null;
			int typeV = 0;
			for (String line : lines) {
				if (line.trim().startsWith("//")) {
					if (tmp.length() > 0 && type != null) {
						attachedObjects
								.add(compileShader(tmp.toString(), typeV));
						tmp.setLength(0);
					}
					String ttype = line.trim().substring(2).trim()
							.toUpperCase();
					Integer rt = SHADER_TYPE_MAP.get(ttype);
					if (rt != null) {
						type = ttype;
						typeV = rt;
					}
				} else
					tmp.append(line).append("\n");
			}
			if (tmp.length() > 0 && type != null) {
				attachedObjects.add(compileShader(tmp.toString(), typeV));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ShaderProgram link() {
		if (programID < 0)
			gpuAlloc();
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

	@Override
	public void gpuFree() {
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
			uniforms.put(name, new ShaderUniform(this, name, sizeBuff.get(0),
					typeBuff.get(0), location));
		}
	}

	public ShaderUniform uniform(String name) {
		ShaderUniform v = uniforms.get(name);
		if (v == null) {
			int l = GL20.glGetUniformLocation(programID, name);
			if (l == -1)
				System.err
						.println("Tried to query shader for invalid uniform: "
								+ name);
			uniforms.put(name, v = new ShaderUniform(this, name, -1, -1, l));
		}
		return v;
	}

	public void bindSamplers() {
		for (int i = 0; i < samplers.length; i++) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
			if (samplers[i] == null)
				Texture.unbind();
			else
				samplers[i].bind();
		}
	}
}
