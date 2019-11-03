package me.noeri.atlatl.apt;

import java.io.File;
import java.util.Collection;

public interface CompilerContext {

	public Collection<File> getDependencyJars();

	public String getSourcePath();

}
