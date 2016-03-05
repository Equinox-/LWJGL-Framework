package com.pi.util;

import java.lang.ref.WeakReference;

public class ReferenceTable<T> {
	private final WeakReference<T>[] table;

	@SuppressWarnings("unchecked")
	public ReferenceTable(int count) {
		table = new WeakReference[count];
	}

	public boolean isAttached(int id, T k) {
		return table[id] != null && table[id].get() == k;
	}

	public void attach(int id, T k) {
		table[id] = new WeakReference<>(k);
	}

	public boolean isEmpty(int id) {
		return table[id] == null || table[id].get() == null;
	}

	public void empty(int id) {
		table[id] = null;
	}

	@SuppressWarnings("rawtypes")
	public boolean isAttached(Enum id, T k) {
		return isAttached(id.ordinal(), k);
	}

	@SuppressWarnings("rawtypes")
	public void attach(Enum id, T k) {
		attach(id.ordinal(), k);
	}

	@SuppressWarnings("rawtypes")
	public boolean isEmpty(Enum id) {
		return isEmpty(id.ordinal());
	}

	@SuppressWarnings("rawtypes")
	public void empty(Enum id) {
		empty(id.ordinal());
	}

	public int size() {
		return table.length;
	}
}
