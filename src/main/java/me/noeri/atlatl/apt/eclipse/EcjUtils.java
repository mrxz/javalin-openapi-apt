package me.noeri.atlatl.apt.eclipse;

import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public final class EcjUtils {

	public static String getFQN(TypeBinding typeBinding) {
		StringBuilder builder = new StringBuilder();
		builder.append(typeBinding.qualifiedPackageName());
		if(builder.length() > 0) {
			builder.append(".");
		}
		builder.append(typeBinding.qualifiedSourceName());
		return builder.toString();
	}

	public static Object getFQN(AnnotationBinding annotationBinding) {
		return getFQN(annotationBinding.getAnnotationType());
	}
}
