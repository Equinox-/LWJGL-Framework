package com.pi.core.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class WarningManager {
	public static final boolean GPUOBJECT_REFERENCE_WATCHING = System
			.getProperty("DEBUG") != null;
	public static final boolean GPUOBJECT_METHOD_ELEVATION = true;
	public static final boolean GLSL_UNIFORM_TYPE_WATCHING = true;

	static {
		if (GPUOBJECT_REFERENCE_WATCHING)
			System.out.println("Using Debug Warnings!");
	}

	private static class AllocationParams {
		StackTraceElement[] stackTraceOnAlloc;
		Class<?> allocated;

		AllocationParams(Class<?> allocated, StackTraceElement[] stack) {
			this.allocated = allocated;
			this.stackTraceOnAlloc = stack;
		}
	}

	static Map<WeakReference<?>, AllocationParams> watchReferences;
	@SuppressWarnings("rawtypes")
	static ReferenceQueue queue;

	static {
		if (GPUOBJECT_REFERENCE_WATCHING) {
			watchReferences = new HashMap<>();
			queue = new ReferenceQueue<>();
			new Thread() {
				@Override
				@SuppressWarnings("rawtypes")
				public void run() {
					while (true) {
						try {
							Reference ref = queue.remove(2000L);
							AllocationParams e = watchReferences.remove(ref);
							if (e != null) {
								System.err.println("Reference to "
										+ e.allocated.getName()
										+ " lost without freeing: ");
								for (int i = 3; i < e.stackTraceOnAlloc.length; i++)
									System.err.println("\t"
											+ e.stackTraceOnAlloc[i]);
							}
						} catch (InterruptedException e1) {
							// Skip
						}
						System.gc(); // Collect, then check again
					}
				}
			}.start();
		}
	}

	static void watchReference(WeakReference<?> ref) {
		if (!GPUOBJECT_REFERENCE_WATCHING)
			return;
		watchReferences.put(ref, new AllocationParams(ref.get().getClass(),
				Thread.currentThread().getStackTrace()));
	}

	static void unwatchReference(WeakReference<?> ref) {
		if (!GPUOBJECT_REFERENCE_WATCHING)
			return;
		watchReferences.remove(ref);
	}
}
