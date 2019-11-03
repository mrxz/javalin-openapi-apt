package me.noeri.atlatl.apt.javac;

import com.sun.tools.javac.code.Lint;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.file.FSInfo;
import com.sun.tools.javac.file.Locations;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import java.io.File;
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
	public Collection<File> getDependencyJars() {
		Context context = processingEnvironment.getContext();
		Locations locations = new Locations();
		locations.update(Log.instance(context), Options.instance(context), Lint.instance(context), FSInfo.instance(context));

		return locations.userClassPath();
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
}
