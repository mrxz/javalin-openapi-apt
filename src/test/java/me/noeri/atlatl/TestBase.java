package me.noeri.atlatl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.InputStream;

public abstract class TestBase {
	protected final TypeSolver typeSolver;
	protected final JavaParser parser;

	public TestBase() {
		typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(false));
		JavaSymbolSolver symbolResolver = new JavaSymbolSolver(typeSolver);
		parser = new JavaParser(new ParserConfiguration()
				.setSymbolResolver(symbolResolver));
	}

	protected InputStream getResource(String filename) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
	}
}
