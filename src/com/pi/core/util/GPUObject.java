package com.pi.core.util;

public interface GPUObject {
	/**
	 * Allocates this object on the GPU.
	 */
	public void gpuAlloc();

	/**
	 * Uploads this object to the GPU.
	 */
	public void gpuUpload();

	/**
	 * Downloads this object from the GPU. (Not always supported)
	 */
	public default void gpuDownload() {
		throw new UnsupportedOperationException(getClass()
				+ " doesn't support downloading from GPU");
	}

	/**
	 * Frees this object on the GPU.
	 */
	public void gpuFree();
}