/*
 * Copyright (c) 2019 Noeri Huisman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.noeri.atlatl.apt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

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
			getClass().getClassLoader().loadClass("org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl");
			return true;
		} catch(NoClassDefFoundError | ClassNotFoundException e) {
			return false;
		}
	}
}
