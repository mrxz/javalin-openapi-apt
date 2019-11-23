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

import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import javassist.bytecode.AccessFlag;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;

public class EcjEnumConstantDeclaration implements ResolvedEnumConstantDeclaration {
	private final FieldBinding fieldBinding;
	private final TypeSolver typeSolver;
	private ResolvedType type;

	public EcjEnumConstantDeclaration(FieldBinding fieldBinding, TypeSolver typeSolver) {
		if(fieldBinding == null) {
			throw new IllegalArgumentException();
		}
		if((fieldBinding.getAccessFlags() & AccessFlag.ENUM) == 0) {
			throw new IllegalArgumentException("Trying to instantiate a EcjEnumConstantDeclaration with something which is not an enum field");
		}
		this.fieldBinding = fieldBinding;
		this.typeSolver = typeSolver;
	}

	@Override
	public String getName() {
		return new String(fieldBinding.readableName());
	}

	@Override
	public ResolvedType getType() {
		if(type == null) {
			type = new ReferenceTypeImpl(new EcjEnumDeclaration(fieldBinding.declaringClass, typeSolver), typeSolver);
		}
		return type;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + "fieldBinding=" + getName() + ", typeSolver=" + typeSolver + '}';
	}
}
