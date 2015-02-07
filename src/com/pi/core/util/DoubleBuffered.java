package com.pi.core.util;

public class DoubleBuffered<E> {
	private E front, back;

	public DoubleBuffered(E front, E back) {
		this.front = front;
		this.back = back;
	}

	public E getFront() {
		return front;
	}

	public E getBack() {
		return back;
	}

	public void flip() {
		E tmp = front;
		front = back;
		back = tmp;
	}
}
