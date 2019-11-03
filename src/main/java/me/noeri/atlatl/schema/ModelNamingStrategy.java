package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.types.ResolvedType;

@FunctionalInterface
public interface ModelNamingStrategy {
	public String convert(ResolvedType resolvedType);
}
