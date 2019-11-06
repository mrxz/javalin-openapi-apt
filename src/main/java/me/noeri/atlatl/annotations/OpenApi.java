package me.noeri.atlatl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
public @interface OpenApi {

	String title();
	String description();
	String version();
	String outputFile() default "openapi.yaml";

}
