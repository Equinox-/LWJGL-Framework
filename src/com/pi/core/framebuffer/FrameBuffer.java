package com.pi.core.framebuffer;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import com.pi.core.texture.Texture;
import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;

public class FrameBuffer extends GPUObject<FrameBuffer> implements
		GLIdentifiable, Bindable {
	private static final int DEFAULT_COLOR_ATTACHMENT_LIMIT = 16;

	private static WeakReference<FrameBuffer> current;

	private static void attachObject(int attachmentPoint,
			FrameBufferAttachable t) {
		if (t.getID() < 0)
			throw new IllegalStateException("Can't bind an unallocated object");
		if (t instanceof Texture)
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachmentPoint,
					GL11.GL_TEXTURE_2D, t.getID(), 0);
		else if (t instanceof RenderBuffer)
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER,
					attachmentPoint, GL30.GL_RENDERBUFFER, t.getID());
	}
	public static FrameBuffer current() {
		return current == null ? null : current.get();
	}
	public static void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		current = null;
	}
	private int fbo;

	private Set<FrameBufferAttachable> colorAttachments;

	private FrameBufferAttachable depthAttachment;

	private FrameBufferAttachable stencilAttachment;

	public FrameBuffer() {
		this.fbo = -1;
		this.colorAttachments = new HashSet<>();
		this.depthAttachment = null;
		this.stencilAttachment = null;
	}

	public boolean attachColor(FrameBufferAttachable t) {
		ensureModify();
		if (t == null)
			throw new IllegalArgumentException("The object can't be null");
		if (colorAttachments.size() >= DEFAULT_COLOR_ATTACHMENT_LIMIT)
			throw new IllegalStateException("You already have "
					+ colorAttachments.size() + " you can't add more.");
		return colorAttachments.add(t);
	}

	public void attachDepth(FrameBufferAttachable t) {
		ensureModify();
		if (t == null)
			throw new IllegalArgumentException("The object can't be null");
		if (depthAttachment != null)
			throw new IllegalStateException(
					"You already have a depth object attached!");
		depthAttachment = t;
	}

	public void attachDepthStencil(FrameBufferAttachable t) {
		attachStencil(t);
		attachDepth(t);
	}

	public void attachStencil(FrameBufferAttachable t) {
		ensureModify();
		if (t == null)
			throw new IllegalArgumentException("The object can't be null");
		if (stencilAttachment != null)
			throw new IllegalStateException(
					"You already have a stencil object attached!");
		stencilAttachment = t;
	}

	@Override
	public void bind() {
		if (fbo < 0)
			throw new IllegalStateException(
					"Can't bind an unallocated framebuffer");
		if (current != null && current.get() == this)
			return;
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		current = new WeakReference<>(this);
	}

	public boolean detachColor(FrameBufferAttachable t) {
		ensureModify();
		return colorAttachments.remove(t);
	}

	public FrameBufferAttachable detachDepth() {
		ensureModify();
		FrameBufferAttachable a = depthAttachment;
		depthAttachment = null;
		return a;
	}

	public FrameBufferAttachable detachDepthStencil() {
		if (stencilAttachment != depthAttachment)
			throw new IllegalStateException(
					"The depth and stencil attachments aren't the same; detach them separately");
		FrameBufferAttachable a = depthAttachment;
		depthAttachment = null;
		stencilAttachment = null;
		return a;
	}

	public FrameBufferAttachable detachStencil() {
		ensureModify();
		FrameBufferAttachable a = stencilAttachment;
		stencilAttachment = null;
		return a;
	}

	private void ensureModify() {
		if (fbo >= 0)
			throw new IllegalStateException(
					"Can't modify an allocated framebuffer.");
	}

	public Set<FrameBufferAttachable> getColorAttachments() {
		return colorAttachments;
	}

	public FrameBufferAttachable getDepthAttachment() {
		return depthAttachment;
	}

	@Override
	public int getID() {
		return fbo;
	}

	public FrameBufferAttachable getStencilAttachment() {
		return stencilAttachment;
	}

	@Override
	protected void gpuAllocInternal() {
		if (fbo >= 0)
			gpuFreeInternal();
		fbo = GL30.glGenFramebuffers();
		if (colorAttachments.size() > GL11
				.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS))
			throw new IllegalStateException("You have "
					+ colorAttachments.size()
					+ " color attachments.  The GPU limit is "
					+ GL11.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS));

		bind();
		int colorID = 0;
		for (FrameBufferAttachable t : colorAttachments) {
			attachObject(GL30.GL_COLOR_ATTACHMENT0 + colorID, t);
			colorID++;
		}
		if (depthAttachment != null && depthAttachment == stencilAttachment) {
			attachObject(GL30.GL_DEPTH_STENCIL_ATTACHMENT, depthAttachment);
		} else {
			if (depthAttachment != null)
				attachObject(GL30.GL_DEPTH_ATTACHMENT, depthAttachment);
			if (stencilAttachment != null)
				attachObject(GL30.GL_STENCIL_ATTACHMENT, stencilAttachment);
		}
		int state = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		switch (state) {
		case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
			System.err.println("Framebuffer: Incomplete attachment");
			break;
		case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
			System.err
					.println("Framebuffer: Incomplete for missing attachment");
			break;
		case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
			System.err.println("Framebuffer; Incomplete draw buffer");
			break;
		case GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
			System.err.println("Framebuffer: Incomplete multisample");
			break;
		case GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
			System.err.println("Framebuffer: Incomplete layer targets");
			break;
		case GL30.GL_FRAMEBUFFER_COMPLETE:
			// All good
			break;
		default:
			System.err.println("Framebuffer: Unknown error");
		}
		unbind();
	}

	@Override
	protected void gpuFreeInternal() {
		if (fbo >= 0)
			GL30.glDeleteFramebuffers(fbo);
		fbo = -1;
	}
}
