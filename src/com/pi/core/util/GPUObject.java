package com.pi.core.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public abstract class GPUObject<K> {
	// Used by the Warning Manager system
	private static final Set<Class<?>> scanned = new HashSet<>();

	private boolean allocated = false;
	private WeakReference<GPUObject<K>> ref;

	public GPUObject() {
		if (WarningManager.DEBUG_WARNINGS) {
			Class<?> base = getClass();
			if (scanned.add(base)) {
				Method[] mm = base.getMethods();
				for (Method k : mm) {
					if (k.getName().endsWith("Internal")
							|| k.getName().equals("me")) {
						System.err.println("Internal GPU method is public:\t"
								+ k);
					}
				}
			}
		}
		if (WarningManager.DEBUG_WARNINGS)
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
		if (!allocated)
			throw new IllegalStateException("Can't upload when not allocated.");
		gpuUploadInternal();
		return me();
	}

	/**
	 * Downloads this object from the GPU. (Not always supported)
	 */
	public final K gpuDownload() {
		if (!allocated)
			throw new IllegalStateException(
					"Can't download when not allocated.");
		gpuDownloadInternal();
		return me();
	}

	/**
	 * Allocates this object on the GPU.
	 */
	public final K gpuAlloc() {
		if (allocated)
			gpuFree();
		gpuAllocInternal();
		allocated = true;
		if (WarningManager.DEBUG_WARNINGS)
			WarningManager.watchReference(ref);
		return me();
	}

	/**
	 * Frees this object on the GPU.
	 */
	public final K gpuFree() {
		if (allocated)
			gpuFreeInternal();
		allocated = false;
		if (WarningManager.DEBUG_WARNINGS)
			WarningManager.unwatchReference(ref);
		return me();
	}
}
