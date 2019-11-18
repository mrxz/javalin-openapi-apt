package me.noeri.atlatl.apt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;

public final class CompilerContextFactory {

	public CompilerContext fromProcessingEnvironment(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
		if(isEcj(processingEnv)) {
			if(isEcjIde(processingEnv)) {
				return instantiate(processingEnv, roundEnv, "me.noeri.atlatl.apt.eclipse.IdeEclipseCompilerContext");
			}
			return instantiate(processingEnv, roundEnv, "me.noeri.atlatl.apt.eclipse.BatchEclipseCompilerContext");
		}

		return instantiate(processingEnv, roundEnv, "me.noeri.atlatl.apt.javac.JavaCompilerContext");
	}

	private CompilerContext instantiate(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, String className) {
		try {
			Constructor<?> constructor = getClass().getClassLoader().loadClass(className)
					.getConstructor(ProcessingEnvironment.class, RoundEnvironment.class);
			return (CompilerContext) constructor.newInstance(processingEnv, roundEnv);
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | ClassNotFoundException | NoSuchMethodException e) {
			throw new RuntimeException("Failed to instantiate compiler context", e);
		}
	}

	public boolean isEcj(ProcessingEnvironment processingEnv) {
		return processingEnv.getClass().getName().startsWith("org.eclipse.jdt.");
	}

	public boolean isEcjIde(ProcessingEnvironment processingEnv) {
		return isEcj(processingEnv) && processingEnv.getClass().getSimpleName().equals("IdeBuildProcessingEnvImpl");
	}

	public boolean classLoaderSetupCorrectly(ProcessingEnvironment processingEnv) {
		try {
			getClass().getClassLoader().loadClass(BaseProcessingEnvImpl.class.getCanonicalName());
			return true;
		} catch(NoClassDefFoundError | ClassNotFoundException e) {
			return false;
		}
	}
}
