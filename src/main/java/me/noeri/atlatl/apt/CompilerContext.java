package me.noeri.atlatl.apt;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.io.File;
import java.util.Collection;

public interface CompilerContext {

	public Collection<File> getDependencyJars();
	public TypeSolver getTypeSolver();

	public String getSourcePath();

}
