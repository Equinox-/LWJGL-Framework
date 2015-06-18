package com.pi.core.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public abstract class GPUObject<K> {
	// Used by the Warning Manager system
	private static final Set<Method> scanned = new HashSet<>();

	private Thread allocThread = null;
	private WeakReference<GPUObject<K>> ref;

	public GPUObject() {
		if (WarningManager.GPUOBJECT_METHOD_ELEVATION) {
			Class<?> base = getClass();
			Method[] mm = base.getMethods();
			for (Method k : mm) {
				if (k.getName().endsWith("Internal")
						|| k.getName().equals("me")) {
					if (scanned.add(k))
						System.err.println("Internal GPU method is public:\t"
								+ k);
				}
			}
		}
		if (WarningManager.GPUOBJECT_REF_WATCHING)
			ref = new WeakReference<>(this, WarningManager.queue);
	}

	protected abstract void gpuAllocInternal();

	protected void gpuUploadInternal() {
		throw new UnsupportedOperationException(getClass()
				+ " doesn't support uploading to GPU");
	}

	protected void gpuDownloadInternal() {
		throw new UnsupportedOperationException(getClass()
				+ " doesn't support downloading from GPU");
	}

	protected abstract void gpuFreeInternal();

	protected abstract K me();

	/**
	 * Allocates and then uploads this object to the GPU.
	 * 
	 * @see #gpuAllocInternal()
	 * @see #gpuUploadInternal()
	 */
	public final K gpuAllocAndUpload() {
		gpuAlloc();
		gpuUpload();
		return me();
	}

	/**
	 * Uploads this object to the GPU. (Not always supported)
	 */
	public final K gpuUpload() {
		if (!allocated())
			throw new IllegalStateException("Can't upload when not allocated.");
		gpuUploadInternal();
		return me();
	}

	/**
	 * Downloads this object from the GPU. (Not always supported)
	 */
	public final K gpuDownload() {
		if (!allocated())
			throw new IllegalStateException(
					"Can't download when not allocated.");
		gpuDownloadInternal();
		return me();
	}

	/**
	 * Allocates this object on the GPU.
	 */
	public final K gpuAlloc() {
		if (allocThread != null)
			gpuFree();
		gpuAllocInternal();
		allocThread = Thread.currentThread();
		if (ref != null)
			WarningManager.watchReference(ref);
		return me();
	}

	/**
	 * Frees this object on the GPU.
	 */
	public final K gpuFree() {
		if (allocThread != null)
			gpuFreeInternal();
		allocThread = null;
		if (ref != null)
			WarningManager.unwatchReference(ref);
		return me();
	}

	public final boolean allocated() {
		return allocThread != null;
	}

	public final boolean allocatedOnThisThread() {
		return allocated() && allocThread == Thread.currentThread();
	}
}
