package me.noeri.atlatl.apt;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public interface CompilerContext {

	public TypeSolver getTypeSolver();

	public String getSourcePath();

}
