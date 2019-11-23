/*
 * Copyright 2016 Federico Tomassetti
 * Copyright 2019 Noeri Huisman; modified to work with ECJ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.noeri.atlatl.apt.eclipse.model;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParametrizable;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistUtilsTrampoline;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;

public class EcjFieldDeclaration implements ResolvedFieldDeclaration {

	private final FieldBinding fieldBinding;
	private final TypeSolver typeSolver;

	public EcjFieldDeclaration(FieldBinding fieldBinding, TypeSolver typeSolver) {
		this.fieldBinding = fieldBinding;
		this.typeSolver = typeSolver;
	}

	@Override
	public ResolvedType getType() {
		try {
			if(fieldBinding.genericSignature() != null && declaringType() instanceof ResolvedTypeParametrizable) {
				javassist.bytecode.SignatureAttribute.Type genericSignatureType = SignatureAttribute.toFieldSignature(new String(fieldBinding.genericSignature()));
				return JavassistUtilsTrampoline.signatureTypeToType(genericSignatureType, typeSolver, (ResolvedTypeParametrizable) declaringType());
			} else {
				return EcjFactory.typeUsageFor(fieldBinding.type, typeSolver);
			}
		} catch(BadBytecode e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isStatic() {
		return fieldBinding.isStatic();
	}

	@Override
	public String getName() {
		return new String(fieldBinding.readableName());
	}

	@Override
	public boolean isField() {
		return true;
	}

	@Override
	public boolean isParameter() {
		return false;
	}

	@Override
	public boolean isType() {
		return false;
	}

	@Override
	public AccessSpecifier accessSpecifier() {
		return EcjFactory.modifiersToAccessLevel(fieldBinding.getAccessFlags());
	}

	@Override
	public ResolvedTypeDeclaration declaringType() {
		return EcjFactory.toTypeDeclaration(fieldBinding.declaringClass, typeSolver);
	}

}
