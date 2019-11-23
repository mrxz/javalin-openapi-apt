package com.github.javaparser.symbolsolver.javassistmodel;

import com.github.javaparser.resolution.declarations.ResolvedTypeParametrizable;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import javassist.bytecode.SignatureAttribute;

public class JavassistUtilsTrampoline {

	public static ResolvedType signatureTypeToType(SignatureAttribute.Type signatureType, TypeSolver typeSolver, ResolvedTypeParametrizable typeParametrizable) {
		return JavassistUtils.signatureTypeToType(signatureType, typeSolver, typeParametrizable);
	}

}
