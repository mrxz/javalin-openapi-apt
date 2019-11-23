package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.types.ResolvedType;
import java.util.stream.Collectors;

public class SimpleModelNamingStrategy implements ModelNamingStrategy {

	@Override
	public String convert(ResolvedType resolvedType) {
		String[] parts = getName(resolvedType).split("\\.");
		String simpleName = parts[parts.length - 1];
		// Note: strip DTO
		if(simpleName.endsWith("Dto")) {
			simpleName = simpleName.replaceAll("Dto$", "");
		}

		if(resolvedType.isReferenceType() && !resolvedType.asReferenceType().getTypeParametersMap().isEmpty()) {
			simpleName += "_";
			simpleName += resolvedType.asReferenceType().typeParametersMap().getTypes().stream()
				.map(this::convert)
				.collect(Collectors.joining());
			simpleName += "_";
		}
		return simpleName;
	}

	private String getName(ResolvedType resolvedType) {
		if(resolvedType.isReferenceType()) {
			return resolvedType.asReferenceType().getId();
		}
		// FIXME
		return resolvedType.isWildcard() ? "Wildcard" : "???";
	}
}
