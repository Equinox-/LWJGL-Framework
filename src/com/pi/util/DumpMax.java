package com.pi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;

import com.pi.core.wind.GLWindow;

@SuppressWarnings("rawtypes")
public class DumpMax {
	private static final Class[] clazz = { GL11.class, GL12.class, GL13.class, GL14.class, GL15.class, GL20.class,
			GL21.class, GL30.class, GL31.class, GL32.class, GL33.class, GL40.class, GL41.class, GL42.class, GL43.class,
			GL44.class, GL45.class };

	public static void main(String[] args) {
		GLWindow tmp = new GLWindow() {
			@Override
			public void init() {
				for (Class c : clazz) {
					for (Field f : c.getFields()) {
						if ((f.getModifiers() & Modifier.STATIC) > 0 && f.getName().startsWith("GL_MAX_")) {
							try {
								System.out.println(
										c.getName() + "#" + f.getName() + "\t" + GL11.glGetInteger(f.getInt(null)));
							} catch (Exception e) {
							}
						}
					}
				}
				super.shutdown();
			}

			@Override
			public void render() {
			}

			@Override
			public void update() {
			}

			@Override
			public void dispose() {
			}
		};
		tmp.start();
	}
}
