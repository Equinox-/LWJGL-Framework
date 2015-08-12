package com.pi.core.glsl;

import org.lwjgl.opengl.GL11;

import com.pi.core.framebuffer.FrameBuffer;
import com.pi.core.model.BasicShapes;
import com.pi.core.texture.Texture;
import com.pi.core.util.GPUObject;

public class ShaderOnTexture extends GPUObject<ShaderOnTexture> {
	private FrameBuffer fbo;
	private Texture result;

	public ShaderOnTexture(Texture result) {
		this.result = result;
		this.fbo = new FrameBuffer();
		this.fbo.attachColor(result);
	}

	@Override
	protected void gpuAllocInternal() {
		this.result.gpuAlloc();
		this.fbo.gpuAlloc();
	}

	@Override
	protected void gpuFreeInternal() {
		this.fbo.gpuFree();
		this.result.gpuFree();
	}

	public Texture getResult() {
		return result;
	}

	public void render(ShaderProgram prog) {
		GL11.glViewport(0, 0, result.getWidth(), result.getHeight());
		fbo.bind();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		prog.bind();
		BasicShapes.shapes().getNDCScreenQuad().render();
		ShaderProgram.unbind();
		FrameBuffer.unbind();
	}

	public FrameBuffer getFBO() {
		return fbo;
	}

}
