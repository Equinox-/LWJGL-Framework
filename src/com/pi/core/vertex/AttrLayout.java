package com.pi.core.vertex;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AttrLayout {
	public int layout();

	public int dimension() default -1;
	
	public int arraySize() default 1;
}
