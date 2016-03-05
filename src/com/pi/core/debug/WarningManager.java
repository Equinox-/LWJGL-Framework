package com.pi.core.debug;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.pi.core.util.GPUObject;

public class WarningManager {
	public static final boolean GPUOBJECT_REF_WATCHING = true;
	public static final boolean GPUOBJECT_METHOD_ELEVATION = true;
	public static final boolean GLSL_UNIFORM_TYPE_WATCHING = true;

	private static Map<WeakReference<?>, AllocationParams> watchReferences;
	@SuppressWarnings("rawtypes")
	public static ReferenceQueue<GPUObject> queue;
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
					System.out.println("Reference watcher begins");
					while (true) {
						try {
							Reference ref = queue.remove(10000L);
							AllocationParams e = watchReferences.remove(ref);
							if (e != null) {
								System.err.println("Reference to " + e.allocated.getName()
										+ " lost without freeing: (hash=" + Integer.toString(e.hash, 16) + ")");
								for (int i = 3; i < e.stackTraceOnAlloc.length; i++)
									if (!e.stackTraceOnAlloc[i].getClassName().endsWith("GPUObject"))
										System.err.println("\tat " + e.stackTraceOnAlloc[i]);
							}
						} catch (InterruptedException e1) {
							if (!referenceWatchState.get())
								break;
							// Skip
						}
					}
					System.out.println("Reference watcher finishes");
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

	public static void unwatchReference(WeakReference<?> ref) {
		watchReferences.remove(ref);
	}

	public static void watchReference(WeakReference<?> ref) {
		watchReferences.put(ref, new AllocationParams(ref.get(), Thread.currentThread().getStackTrace()));
	}

	private static class AllocationParams {
		private int hash;
		private StackTraceElement[] stackTraceOnAlloc;
		private Class<?> allocated;

		private AllocationParams(Object allocated, StackTraceElement[] stack) {
			this.allocated = allocated.getClass();
			this.hash = allocated.hashCode();
			this.stackTraceOnAlloc = stack;
		}
	}
}
