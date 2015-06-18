package com.pi.core.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class WarningManager {
	public static final boolean GPUOBJECT_REF_WATCHING = true;
	public static final boolean GPUOBJECT_METHOD_ELEVATION = true;
	public static final boolean GLSL_UNIFORM_TYPE_WATCHING = true;

	private static class AllocationParams {
		int hash;
		StackTraceElement[] stackTraceOnAlloc;
		Class<?> allocated;

		AllocationParams(Object allocated, StackTraceElement[] stack) {
			this.allocated = allocated.getClass();
			this.hash = allocated.hashCode();
			this.stackTraceOnAlloc = stack;
		}
	}

	static Map<WeakReference<?>, AllocationParams> watchReferences;
	@SuppressWarnings("rawtypes")
	static ReferenceQueue<GPUObject> queue;

	private static AtomicBoolean referenceWatchState = new AtomicBoolean(true);
	private static Thread referenceWatchThread;

	static {
		if (GPUOBJECT_REF_WATCHING) {
			watchReferences = new HashMap<>();
			queue = new ReferenceQueue<>();
			(referenceWatchThread = new Thread("Ref Watcher") {
				@Override
				@SuppressWarnings("rawtypes")
				public void run() {
					while (true) {
						try {
							Reference ref = queue.remove(1000L);
							AllocationParams e = watchReferences.remove(ref);
							if (e != null) {
								System.err.println("Reference to "
										+ e.allocated.getName()
										+ " lost without freeing: (hash="
										+ Integer.toString(e.hash, 16) + ")");
								for (int i = 3; i < e.stackTraceOnAlloc.length; i++)
									if (!e.stackTraceOnAlloc[i].getClassName()
											.endsWith("GPUObject"))
										System.err.println("\tat "
												+ e.stackTraceOnAlloc[i]);
							}
						} catch (InterruptedException e1) {
							if (!referenceWatchState.get())
								break;
							// Skip
						}
						System.gc(); // Collect, then check again
					}
					System.out.println("Thread finish");
				}
			}).start();
		}
	}

	public static void termReferenceWatch() {
		referenceWatchState.set(false);
		referenceWatchThread.interrupt();
		try {
			referenceWatchThread.join();
		} catch (InterruptedException e) {
		}
	}

	static void watchReference(WeakReference<?> ref) {
		watchReferences.put(ref, new AllocationParams(ref.get(), Thread
				.currentThread().getStackTrace()));
	}

	static void unwatchReference(WeakReference<?> ref) {
		watchReferences.remove(ref);
	}
}
