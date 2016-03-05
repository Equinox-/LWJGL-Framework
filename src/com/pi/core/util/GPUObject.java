package com.pi.core.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.pi.core.debug.WarningManager;

public abstract class GPUObject<K extends GPUObject<K>> {
	// Used by the Warning Manager system
	private static final Set<Method> scanned = new HashSet<>();

	private Thread allocThread = null;
	private WeakReference<GPUObject<K>> ref;

	public GPUObject() {
		if (WarningManager.GPUOBJECT_METHOD_ELEVATION) {
			Class<?> base = getClass();
			Method[] mm = base.getMethods();
			for (Method k : mm) {
				if ((k.getName().endsWith("Internal") || k.getName().equals("me")) && scanned.add(k))
					System.err.println("Internal GPU method is public:\t" + k);
			}
		}
		if (WarningManager.GPUOBJECT_REF_WATCHING)
			ref = new WeakReference<>(this, WarningManager.queue);
	}

	public final boolean allocated() {
		return allocThread != null;
	}

	public final boolean allocatedOnThisThread() {
		return allocated() && allocThread == Thread.currentThread();
	}

	/**
	 * Allocates this object on the GPU.
	 */
	@SuppressWarnings("unchecked")
	public final K gpuAlloc() {
		if (allocThread != null)
			gpuFree();
		gpuAllocInternal();
		allocThread = Thread.currentThread();
		if (ref != null)
			WarningManager.watchReference(ref);
		return (K) this;
	}

	/**
	 * Allocates and then uploads this object to the GPU.
	 * 
	 * @see #gpuAllocInternal()
	 * @see #gpuUploadInternal()
	 */
	@SuppressWarnings("unchecked")
	public final K gpuAllocAndUpload() {
		gpuAlloc();
		gpuUpload();
		return (K) this;
	}

	protected abstract void gpuAllocInternal();

	/**
	 * Downloads this object from the GPU. (Not always supported)
	 */
	@SuppressWarnings("unchecked")
	public final K gpuDownload() {
		if (!allocated())
			throw new IllegalStateException("Can't download when not allocated.");
		gpuDownloadInternal();
		return (K) this;
	}

	protected void gpuDownloadInternal() {
		throw new UnsupportedOperationException(getClass() + " doesn't support downloading from GPU");
	}

	/**
	 * Frees this object on the GPU.
	 */
	@SuppressWarnings("unchecked")
	public final K gpuFree() {
		if (allocThread != null)
			gpuFreeInternal();
		allocThread = null;
		if (ref != null)
			WarningManager.unwatchReference(ref);
		return (K) this;
	}

	protected abstract void gpuFreeInternal();

	/**
	 * Uploads this object to the GPU. (Not always supported)
	 */
	@SuppressWarnings("unchecked")
	public final K gpuUpload() {
		if (!allocated())
			throw new IllegalStateException("Can't upload when not allocated.");
		gpuUploadInternal();
		return (K) this;
	}

	protected void gpuUploadInternal() {
		throw new UnsupportedOperationException(getClass() + " doesn't support uploading to GPU");
	}
}
