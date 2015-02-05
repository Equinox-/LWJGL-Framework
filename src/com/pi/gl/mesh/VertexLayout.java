package com.pi.gl.mesh;

import java.lang.reflect.Field;

public class VertexLayout {
	public VertexLayout(Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields()) {
			AttrLayout layout = f.getAnnotation(AttrLayout.class);
			if (layout != null) {
				Class<?> type = f.getType();

			}
		}
	}
}
