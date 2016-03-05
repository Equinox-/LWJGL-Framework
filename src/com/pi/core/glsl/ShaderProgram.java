package com.pi.core.glsl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.pi.core.GLException;
import com.pi.core.debug.FrameCounter;
import com.pi.core.debug.FrameCounter.FrameParam;
import com.pi.core.texture.Texture;
import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;
import com.pi.io.FileUtil;

public class ShaderProgram extends GPUObject<ShaderProgram> implements Bindable, GLIdentifiable {
	static final int MAX_TEXTURE_UNITS = 16;
	private static ShaderProgram currentShader;

	private final static Map<String, Integer> SHADER_TYPE_MAP;
	private final static Map<Integer, String> SHADER_TYPE_MAP_INVERSE;

	// Limits rebinding
	private static final Texture[] ACTIVE_TEXTURE_UNITS = new Texture[MAX_TEXTURE_UNITS];
	static {
		SHADER_TYPE_MAP = new HashMap<>();
		SHADER_TYPE_MAP_INVERSE = new HashMap<>();
		insertShaderType("GL_VERTEX_SHADER", GL20.GL_VERTEX_SHADER);
		insertShaderType("GL_TESS_CONTROL_SHADER", GL40.GL_TESS_CONTROL_SHADER);
		insertShaderType("GL_TESS_EVALUATION_SHADER", GL40.GL_TESS_EVALUATION_SHADER);
		insertShaderType("GL_GEOMETRY_SHADER", GL32.GL_GEOMETRY_SHADER);
		insertShaderType("GL_FRAGMENT_SHADER", GL20.GL_FRAGMENT_SHADER);
		insertShaderType("GL_COMPUTE_SHADER", GL43.GL_COMPUTE_SHADER);
	}
	@SuppressWarnings("unused")
	private static int compileShader(String src, int type) {
		String processedSource = ShaderPreprocessor.preprocess(src);
		if (false) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("/tmp/shader_src", true));
				for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
					out.write(e.toString());
					out.newLine();
				}
				out.write(SHADER_TYPE_MAP_INVERSE.get(type));
				out.write(processedSource);
				out.newLine();
				out.newLine();
				out.newLine();
				out.newLine();
				out.close();
			} catch (Exception e) {
			}
		}
		String[] lines = processedSource.split("\n");

		int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, processedSource);
		GL20.glCompileShader(shader);
		String log = ShaderLogParser.shaderCompileLog(lines, shader);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			GL20.glDeleteShader(shader);
			throw new GLException("Shader compile failure", log);
		} else if (log.trim().length() > 0) {
			System.err.println("Shader compile log: \n" + log);
		}
		return shader;
	}
	public static ShaderProgram current() {
		return currentShader;
	}

	private static void insertShaderType(String t, int id) {
		SHADER_TYPE_MAP.put(t, id);
		SHADER_TYPE_MAP_INVERSE.put(id, t);
	}
	public static void unbind() {
		GL20.glUseProgram(0);
		FrameCounter.increment(FrameParam.SHADER_CHANGE);
		currentShader = null;
	}
	private int programID;
	private final List<Integer> attachedObjects;
	private final Map<String, ShaderUniform> uniformsByName;

	private ShaderUniform[] uniformsByID;

	private final Map<String, ShaderUniformBlock> uniformBlocksByName;

	private ShaderUniformBlock[] uniformBlocksByID;

	// Needs to be accessed by ShaderUniform; therefore not private
	public Texture[] textureUnit;

	public int[] textureUnitRefCount;

	public ShaderProgram() {
		this.uniformsByName = new HashMap<>();
		this.uniformBlocksByName = new HashMap<>();
		this.attachedObjects = new ArrayList<>(2);
		this.programID = -1;
		this.textureUnit = new Texture[MAX_TEXTURE_UNITS];
		this.textureUnitRefCount = new int[MAX_TEXTURE_UNITS];
	}

	public ShaderProgram attach(int type, InputStream src) {
		try {
			attachedObjects.add(compileShader(FileUtil.readStreamFully(src), type));
		} catch (IOException e) {
			throw new GLException("Shader stream load failure", e);
		}
		return this;
	}

	@Override
	public void bind() {
		if (programID == -1)
			throw new RuntimeException("Attempted to bind an unallocated shader.");
		if (currentShader == this)
			return;
		GL20.glUseProgram(programID);
		FrameCounter.increment(FrameParam.SHADER_CHANGE);
		currentShader = this;
	}

	public void commitData() {
		for (int i = 0; i < textureUnit.length; i++) {
			if (ACTIVE_TEXTURE_UNITS[i] != textureUnit[i]) {
				if (textureUnit[i] != null)
					textureUnit[i].bind(i);
				// Unbound textures just have an undefined state.
				// else
				// Texture.unbind(i);
				ACTIVE_TEXTURE_UNITS[i] = textureUnit[i];
			}
		}
		for (ShaderUniformBlock block : uniformBlocksByID) {
			if (ShaderUniformBlock.ALLOW_UTILITY_ACCESS)
				block.uploadIfNeeded();
			block.recheckBinding();
		}
		FrameCounter.increment(FrameParam.SHADER_DATA_COMMIT);
	}

	public ShaderProgram fragment(InputStream src) {
		return attach(GL20.GL_FRAGMENT_SHADER, src);
	}

	@Override
	public int getID() {
		return programID;
	}

	@Override
	protected void gpuAllocInternal() {
		if (this.programID >= 0)
			gpuFreeInternal();
		this.programID = GL20.glCreateProgram();
	}

	@Override
	protected void gpuFreeInternal() {
		unbind();
		for (int obj : attachedObjects) {
			GL20.glDetachShader(programID, obj);
			GL20.glDeleteShader(obj);
		}
		GL20.glDeleteProgram(programID);
		programID = -1;
	}

	public ShaderProgram joined(InputStream src) {
		try {
			String[] lines = FileUtil.readStreamFully(src).split("(\r|)\n");
			StringBuilder tmp = new StringBuilder();
			String type = null;
			int typeV = 0;
			for (String line : lines) {
				if (line.trim().startsWith("//")) {
					String ttype = line.trim().substring(2).trim().toUpperCase();
					Integer rt = SHADER_TYPE_MAP.get(ttype);
					if (rt != null && tmp.length() > 0 && type != null) {
						attachedObjects.add(compileShader(tmp.toString(), typeV));
						tmp.setLength(0);
					}
					if (rt != null) {
						type = ttype;
						typeV = rt;
					}
				} else
					tmp.append(line).append('\n');
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
			gpuAllocInternal();
		for (int obj : attachedObjects)
			GL20.glAttachShader(programID, obj);
		GL20.glLinkProgram(programID);
		String info = GL20.glGetProgramInfoLog(programID, 1024);
		if (info.trim().length() > 0)
			System.err.println("Program log: " + info);
		if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println("Shader program wasn't linked correctly.");
			throw new RuntimeException();
		}
		loadUniforms();
		return this;
	}

	private void loadUniforms() {
		int uniformBlockCount = GL20.glGetProgrami(programID, GL31.GL_ACTIVE_UNIFORM_BLOCKS);
		uniformBlocksByID = new ShaderUniformBlock[uniformBlockCount];
		uniformBlocksByName.clear();
		for (int i = 0; i < uniformBlockCount; i++) {
			String name = GL31.glGetActiveUniformBlockName(programID, i);
			uniformBlocksByName.put(name, uniformBlocksByID[i] = new ShaderUniformBlock(this, i, name));
		}

		int uniformCount = GL20.glGetProgrami(programID, GL20.GL_ACTIVE_UNIFORMS);
		int uniformMaxNameLength = GL20.glGetProgrami(programID, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
		uniformsByID = new ShaderUniform[uniformCount];
		uniformsByName.clear();

		for (int i = 0; i < uniformCount; i++) {
			uniformsByID[i] = new ShaderUniform(this, i, uniformMaxNameLength);
			uniformsByName.put(uniformsByID[i].name(), uniformsByID[i]);
		}
	}

	public ShaderUniform uniform(int id) {
		return uniformsByID[id];
	}

	public ShaderUniform uniform(String name) {
		ShaderUniform v = uniformsByName.get(name);
		// if (v == null)
		// System.err.println("Tried to query shader for invalid uniform: "
		// + name);
		return v;
	}

	public ShaderUniformBlock uniformBlock(int id) {
		return uniformBlocksByID[id];
	}

	public ShaderUniformBlock uniformBlock(String name) {
		return uniformBlocksByName.get(name);
	}

	public boolean using() {
		return currentShader == this;
	}

	public ShaderProgram vertex(InputStream src) {
		return attach(GL20.GL_VERTEX_SHADER, src);
	}
}
