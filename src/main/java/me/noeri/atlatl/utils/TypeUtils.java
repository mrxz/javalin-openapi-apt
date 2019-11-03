package me.noeri.atlatl.utils;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

public final class TypeUtils {

	public static boolean isAssignable(ResolvedType reference, ResolvedReferenceTypeDeclaration type) {
		// Note: for some reasons JP/JSS' isAssignableBy methods don't seem to work
		if(!reference.isReferenceType()) {
			return false;
		}
		ResolvedReferenceType referenceType = reference.asReferenceType();
		if(referenceType.getId().equals(type.getId())) {
			return true;
		}
		return referenceType.getTypeDeclaration().getAllAncestors().stream()
				.anyMatch(ancestorReferenceType -> ancestorReferenceType.getId().equals(type.getId()));
	}

}
