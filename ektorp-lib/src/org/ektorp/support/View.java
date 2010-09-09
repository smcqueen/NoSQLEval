package org.ektorp.support;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface View {
	
	String name();
	String map();
	String reduce() default "";
}
