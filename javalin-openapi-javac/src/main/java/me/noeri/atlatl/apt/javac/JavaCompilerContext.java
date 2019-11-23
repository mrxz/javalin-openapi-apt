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
package me.noeri.atlatl.apt.javac;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.file.FSInfo;
import com.sun.tools.javac.file.Locations;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.JavaFileObject;
import me.noeri.atlatl.apt.CompilerContext;

public class JavaCompilerContext implements CompilerContext {

	private final JavacProcessingEnvironment processingEnvironment;
	private final RoundEnvironment roundEnvironment;

	public JavaCompilerContext(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
		if(!(processingEnvironment instanceof JavacProcessingEnvironment)) {
			throw new IllegalArgumentException("Processing environment is invalid");
		}
		this.processingEnvironment = (JavacProcessingEnvironment) processingEnvironment;
		this.roundEnvironment = roundEnvironment;
	}

	@Override
	public String getSourcePath() {
		Optional<Symbol.ClassSymbol> classSymbol = roundEnvironment.getRootElements()
				.stream()
				.filter(element -> element instanceof Symbol.ClassSymbol)
				.findAny()
				.map(element -> ((Symbol.ClassSymbol)element));
		if(!classSymbol.isPresent()) {
			throw new RuntimeException("Unable to derive source path without root elements");
		}

		JavaFileObject sourceFileObject = classSymbol.get().sourcefile;
		String sourceFilePath = sourceFileObject.toUri().getPath();
		String relativeSourceFilePath = Stream.of(classSymbol.get().getQualifiedName().toString().split("\\."))
			.collect(Collectors.joining("/", "", ".java"));
		return sourceFilePath.substring(0, sourceFilePath.length() - relativeSourceFilePath.length());
	}

	@Override
	public TypeSolver getTypeSolver() {
		CombinedTypeSolver result = new CombinedTypeSolver();
		getDependencyJars().stream().filter(File::exists).forEach(dependencyJar -> {
			try {
				result.add(new JarTypeSolver(dependencyJar));
			} catch(IOException e) {
				throw new RuntimeException("Failed to load dependency jar: " + dependencyJar.getPath());
			}
		});
		return result;
	}

	private Collection<File> getDependencyJars() {
		Context context = processingEnvironment.getContext();
		Locations locations = new Locations();
		locations.update(Log.instance(context), Options.instance(context), Lint.instance(context), FSInfo.instance(context));

		return locations.userClassPath();
	}
}
