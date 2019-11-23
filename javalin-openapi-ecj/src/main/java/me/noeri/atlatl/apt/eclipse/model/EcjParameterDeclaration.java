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

import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class EcjParameterDeclaration implements ResolvedParameterDeclaration {

	private ResolvedType type;
	private TypeSolver typeSolver;
	private boolean variadic;
	private String name;

	public EcjParameterDeclaration(TypeBinding type, TypeSolver typeSolver, boolean variadic, String name) {
		this(EcjFactory.typeUsageFor(type, typeSolver), typeSolver, variadic, name);
	}

	public EcjParameterDeclaration(ResolvedType type, TypeSolver typeSolver, boolean variadic, String name) {
		this.name = name;
		this.type = type;
		this.typeSolver = typeSolver;
		this.variadic = variadic;
	}

	@Override
	public String toString() {
		 return "EcjParameterDeclaration{" +
				 "type=" + type +
				 ", typeSolver=" + typeSolver +
				 ", variadic=" + variadic +
				 '}';
	}

	@Override
	public boolean hasName() {
		return name != null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isField() {
		return false;
	}

	@Override
	public boolean isParameter() {
		return true;
	}

	@Override
	public boolean isVariadic() {
		return variadic;
	}

	@Override
	public boolean isType() {
		return false;
	}

	@Override
	public ResolvedType getType() {
		return type;
	}

}
