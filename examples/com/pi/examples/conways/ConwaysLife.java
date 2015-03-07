package com.pi.examples.conways;

import org.lwjgl.opengl.GL11;

import com.pi.core.framebuffer.FrameBuffer;
import com.pi.core.glsl.ShaderProgram;
import com.pi.core.model.BasicShapes;
import com.pi.core.texture.DataTexture;
import com.pi.core.texture.Texture;
import com.pi.core.texture.TextureWrap;
import com.pi.core.util.DoubleBuffered;
import com.pi.core.wind.GLWindow;
import com.pi.math.vector.Vector;

public class ConwaysLife extends GLWindow {
	private static final int W = 256, H = 256;

	private DoubleBuffered<FrameBuffer> frameBuffers;
	private DoubleBuffered<DataTexture> textures;

	private ShaderProgram conway;
	private ShaderProgram render;

	private static ShaderProgram createShader(String name) {
		ShaderProgram p = new ShaderProgram();
		p.vertex(ConwaysLife.class.getResourceAsStream(name + ".vs"));
		p.fragment(ConwaysLife.class.getResourceAsStream(name + ".fs"));
		p.gpuAlloc();
		p.link();
		return p;
	}

	@Override
	public void init() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		super.setSize(W, H);

		textures = new DoubleBuffered<>(new DataTexture(1, W, H),
				new DataTexture(1, W, H));
		textures.getFront().wrap(TextureWrap.REPEAT, TextureWrap.REPEAT)
				.gpuAllocInternal();
		textures.getBack().wrap(TextureWrap.REPEAT, TextureWrap.REPEAT)
				.gpuAllocInternal();

		for (Vector[] vv : textures.getBack().vectors)
			for (Vector v : vv)
				v.setV(Math.random() > 0.75 ? 1 : 0);
		textures.getBack().gpuUploadInternal();

		frameBuffers = new DoubleBuffered<>(new FrameBuffer(),
				new FrameBuffer());
		frameBuffers.getFront().attachColor(textures.getFront());
		frameBuffers.getFront().gpuAlloc();

		frameBuffers.getBack().attachColor(textures.getBack());
		frameBuffers.getBack().gpuAlloc();

		conway = createShader("conway");
		conway.bind();
		conway.uniform("viewport").vector(W, H);
		ShaderProgram.unbind();

		render = createShader("render");
		render.bind();
		render.uniform("viewport").vector(W, H);
		ShaderProgram.unbind();

		GL11.glViewport(0, 0, W, H);
	}

	@Override
	public void render() {
		// Run update
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		conway.bind();
		conway.uniform("state").texture(textures.getBack());
		conway.bindSamplers();
		frameBuffers.getFront().bind();
		BasicShapes.shapes().getNDCScreenQuad().render();
		FrameBuffer.unbind();
		Texture.unbind();
		ShaderProgram.unbind();

		render.bind();
		render.uniform("state").texture(textures.getFront());
		render.bindSamplers();
		BasicShapes.shapes().getNDCScreenQuad().render();
		Texture.unbind();
		ShaderProgram.unbind();

	}

	@Override
	public void update() {
		textures.flip();
		frameBuffers.flip();
	}

	@Override
	public void dispose() {
		frameBuffers.getFront().gpuFree();
		frameBuffers.getBack().gpuFree();
		textures.getFront().gpuFree();
		textures.getBack().gpuFree();
		render.gpuFree();
		conway.gpuFree();
	}

	public static void main(String[] args) {
		new ConwaysLife().start();
	}
}
