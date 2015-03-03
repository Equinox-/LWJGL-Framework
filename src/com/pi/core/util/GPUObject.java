package com.pi.core.util;

public interface GPUObject {
	/**
	 * Allocates this object on the GPU.
	 */
	public void gpuAlloc();

	/**
	 * Allocates and then uploads this object to the GPU.
	 * 
	 * @see #gpuAlloc()
	 * @see #gpuUpload()
	 */
	public default void gpuAllocAndUpload() {
		gpuAlloc();
		gpuUpload();
	}

	/**
	 * Uploads this object to the GPU. (Not always supported)
	 */
	public default void gpuUpload() {
		throw new UnsupportedOperationException(getClass()
				+ " doesn't support uploading to GPU");
	}

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
