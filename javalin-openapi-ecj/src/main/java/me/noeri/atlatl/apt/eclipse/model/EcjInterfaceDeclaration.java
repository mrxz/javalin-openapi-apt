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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.core.resolution.MethodUsageResolutionCapability;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistTypeParameter;
import com.github.javaparser.symbolsolver.logic.AbstractTypeDeclaration;
import com.github.javaparser.symbolsolver.logic.MethodResolutionCapability;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.resolution.MethodResolutionLogic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class EcjInterfaceDeclaration extends AbstractTypeDeclaration implements ResolvedInterfaceDeclaration, MethodResolutionCapability, MethodUsageResolutionCapability {

	private ReferenceBinding referenceBinding;
    private TypeSolver typeSolver;

	public EcjInterfaceDeclaration(ReferenceBinding typeBinding, TypeSolver typeSolver) {
        if(!typeBinding.isInterface()) {
            throw new IllegalArgumentException("Not an interface: " + new String(typeBinding.readableName()));
        }
		this.referenceBinding = typeBinding;
		this.typeSolver = typeSolver;
	}

	@Override
	public List<ResolvedReferenceType> getAncestors(boolean acceptIncompleteList) {
		List<ResolvedReferenceType> ancestors = new ArrayList<>();
		for(ReferenceBinding interfaze : referenceBinding.superInterfaces()) {
			try {
				ResolvedReferenceType superInterfaze = EcjFactory.typeUsageFor(interfaze, typeSolver).asReferenceType();
				ancestors.add(superInterfaze);
			} catch(UnsolvedSymbolException e) {
				if(!acceptIncompleteList) {
					// we only throw an exception if we require a complete
					// list; otherwise, we attempt to continue gracefully
					throw e;
				}
			}
		}

		ancestors = ancestors.stream().filter(a -> !a.getQualifiedName().equals(Object.class.getCanonicalName()))
				.collect(Collectors.toList());
		ancestors.add(new ReferenceTypeImpl(typeSolver.solveType(Object.class.getCanonicalName()), typeSolver));
		return ancestors;
	}

	@Override
	public List<ResolvedFieldDeclaration> getAllFields() {
		// TODO Implement getAllFields for interface
		return new ArrayList<>();
	}

	@Override
	public Set<ResolvedMethodDeclaration> getDeclaredMethods() {
		return Arrays.stream(referenceBinding.methods())
				.map(m -> new EcjMethodDeclaration(m, typeSolver))
				.collect(Collectors.toSet());
	}

	@Override
	public boolean isAssignableBy(ResolvedType type) {
        throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAssignableBy(ResolvedReferenceTypeDeclaration other) {
        throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasDirectlyAnnotation(String qualifiedName) {
		return Stream.of(referenceBinding.getAnnotations())
				.anyMatch(binding -> EcjUtils.getFQN(binding).equals(qualifiedName));
	}

	@Override
	public List<ResolvedConstructorDeclaration> getConstructors() {
		return Collections.emptyList();
	}

	@Override
	public Optional<ResolvedReferenceTypeDeclaration> containerType() {
		// TODO Implement containerType for interface
		return Optional.empty();
	}

	@Override
	public String getPackageName() {
		return new String(referenceBinding.qualifiedPackageName());
	}

	@Override
	public String getClassName() {
		return new String(referenceBinding.qualifiedSourceName()); // ?
	}

	@Override
	public String getQualifiedName() {
		return EcjUtils.getFQN(referenceBinding);
	}

	@Override
	public String getName() {
		return new String(referenceBinding.readableName());
	}

	@Override
	public List<ResolvedTypeParameterDeclaration> getTypeParameters() {
		// FIXME: Move implementation to common utility
		if(referenceBinding.isParameterizedType() || referenceBinding.typeVariables().length > 0) {
			String genericSignature = EcjUtils.getGenericSignature(referenceBinding);
			try {
				SignatureAttribute.ClassSignature classSignature = SignatureAttribute.toClassSignature(genericSignature);
				return Arrays.<SignatureAttribute.TypeParameter> stream(classSignature.getParameters())
						.map((tp) -> new JavassistTypeParameter(tp, EcjFactory.toTypeDeclaration(referenceBinding, typeSolver), typeSolver))
						.collect(Collectors.toList());
			} catch(BadBytecode e) {
				throw new RuntimeException(e);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public AccessSpecifier accessSpecifier() {
		return EcjFactory.modifiersToAccessLevel(referenceBinding.getAccessFlags());
	}

	@Override
	public Optional<MethodUsage> solveMethodAsUsage(String name, List<ResolvedType> argumentTypes, Context invocationContext, List<ResolvedType> typeParameters) {
		return EcjUtils.getMethodUsage(referenceBinding, name, argumentTypes, typeSolver, getTypeParameters(), typeParameters);
	}

	@Override
	public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes, boolean staticOnly) {
		// TODO: Copied from EcjClassDeclaration
        List<ResolvedMethodDeclaration> candidates = new ArrayList<>();
		for(MethodBinding methodBinding : referenceBinding.methods()) {
			// Ensure name matches
			if(!new String(methodBinding.constantPoolName()).equals(name)) {
				continue;
			}

			// TODO: synthetic and bridge??
			if(methodBinding.isSynthetic() || methodBinding.isBridge()) {
				continue;
			}

			ResolvedMethodDeclaration candidate = new EcjMethodDeclaration(methodBinding, typeSolver);
			candidates.add(candidate);

			// no need to search for overloaded/inherited methods if the method has no parameters
            if(argumentsTypes.isEmpty() && candidate.getNumberOfParams() == 0) {
                return SymbolReference.solved(candidate);
            }
		}

		return MethodResolutionLogic.findMostApplicable(candidates, name, argumentsTypes, typeSolver);
	}

	@Override
	public List<ResolvedReferenceType> getInterfacesExtended() {
		// TODO implement getInterfacesExtended for interface
		return Collections.emptyList();
	}

    @Override
    public ResolvedInterfaceDeclaration asInterface() {
        return this;
    }

	@Override
	public String toString() {
		return "EcjInterfaceDeclaration{" +
				"referenceBinding=" + new String(referenceBinding.readableName()) +
				", typeSolver=" +
				typeSolver +
				'}';
	}

	@Override
	public Optional<ClassOrInterfaceDeclaration> toAst() {
		return Optional.empty();
	}

}
