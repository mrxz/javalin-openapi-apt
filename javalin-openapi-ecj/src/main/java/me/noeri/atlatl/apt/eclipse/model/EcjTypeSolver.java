/*
 * Copyright 2019 Noeri Huisman
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

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class EcjTypeSolver implements TypeSolver {

	private final BaseProcessingEnvImpl processingEnvironment;
	private TypeSolver parent;

	public EcjTypeSolver(BaseProcessingEnvImpl processingEnvironment) {
		this.processingEnvironment = processingEnvironment;
	}

	@Override
	public TypeSolver getParent() {
		return parent;
	}

	@Override
	public void setParent(TypeSolver parent) {
		this.parent = parent;
	}

	@Override
	public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
		// FIXME: Cleanup logic
		String[] parts = name.split("\\.");

		char[][] fullName = new char[parts.length][];
		char[][] lookupArg = new char[parts.length][];
		fullName = lookupArg = Stream.of(parts).map(String::toCharArray).collect(Collectors.toList()).toArray(lookupArg);

		LookupEnvironment lookupEnvironment = processingEnvironment.getLookupEnvironment();
		ReferenceBinding referenceBinding = null;
		while(lookupArg.length > 0 && (referenceBinding = lookupEnvironment.getType(lookupArg)) == null) {
			lookupArg = Arrays.copyOf(lookupArg, lookupArg.length - 1);
		}
		for(int i = lookupArg.length; i < fullName.length; i++) {
			final char[] memberName = fullName[i];
			referenceBinding = Optional.ofNullable(referenceBinding).map(rb -> rb.getMemberType(memberName)).orElse(null);
		}

		if(referenceBinding != null) {
			return SymbolReference.solved(EcjFactory.toTypeDeclaration(referenceBinding, this));
		} else {
			return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
		}
	}

}
