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
import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.core.resolution.MethodUsageResolutionCapability;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistTypeParameter;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistUtilsTrampoline;
import com.github.javaparser.symbolsolver.logic.AbstractClassDeclaration;
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

public class EcjClassDeclaration extends AbstractClassDeclaration implements MethodUsageResolutionCapability {

	private final ReferenceBinding referenceBinding;
	private final TypeSolver typeSolver;

	public EcjClassDeclaration(ReferenceBinding type, TypeSolver typeSolver) {
		this.referenceBinding = type;
		this.typeSolver = typeSolver;
	}

	@Override
	public ResolvedReferenceType getSuperClass() {
		if(!referenceBinding.isGenericType()) {
			return new ReferenceTypeImpl(typeSolver.solveType(EcjUtils.getFQN(referenceBinding.superclass())), typeSolver);
		} else {
			return new ReferenceTypeImpl(typeSolver.solveType(EcjUtils.getFQN(referenceBinding.superclass())), typeSolver);
		}
	}

	@Override
	public List<ResolvedReferenceType> getInterfaces() {
		try {
			if(!referenceBinding.isParameterizedType()) {
				return Arrays.stream(referenceBinding.superInterfaces())
						.map(i -> typeSolver.solveType(EcjUtils.getFQN(i)))
						.map(i -> new ReferenceTypeImpl(i, typeSolver)).collect(Collectors.toList());
			} else {
				SignatureAttribute.ClassSignature classSignature = SignatureAttribute
						.toClassSignature(EcjUtils.getGenericSignature(referenceBinding));
				return Arrays.stream(classSignature.getInterfaces())
						.map(i -> JavassistUtilsTrampoline.signatureTypeToType(i, typeSolver, this).asReferenceType())
						.collect(Collectors.toList());
			}
		} catch(BadBytecode e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ResolvedConstructorDeclaration> getConstructors() {
		// TODO Support constructors
		return new ArrayList<>();
	}

	@Override
	public List<ResolvedReferenceType> getAncestors(boolean acceptIncompleteList) {
		List<ResolvedReferenceType> ancestors = new ArrayList<>();
		try {
			ResolvedReferenceType superClass = getSuperClass();
			if(superClass != null) {
				ancestors.add(superClass);
			}
		} catch(UnsolvedSymbolException e) {
			if(!acceptIncompleteList) {
				// we only throw an exception if we require a complete list;
				// otherwise, we attempt to continue gracefully
				throw e;
			}
		}
		try {
			ancestors.addAll(getInterfaces());
		} catch(UnsolvedSymbolException e) {
			if(!acceptIncompleteList) {
				// we only throw an exception if we require a complete list;
				// otherwise, we attempt to continue gracefully
				throw e;
			}
		}
		return ancestors;
	}

	@Override
	public List<ResolvedFieldDeclaration> getAllFields() {
		List<ResolvedFieldDeclaration> fieldDecls = new ArrayList<>();
		collectDeclaredFields(referenceBinding, fieldDecls);
		return fieldDecls;
	}

	private void collectDeclaredFields(ReferenceBinding referenceBinding, List<ResolvedFieldDeclaration> fieldDecls) {
		if(referenceBinding != null) {
			Arrays.stream(referenceBinding.fields())
					.forEach(f -> fieldDecls.add(new EcjFieldDeclaration(f, typeSolver)));
			collectDeclaredFields(referenceBinding.superclass(), fieldDecls);
		}
	}

	@Override
	public Set<ResolvedMethodDeclaration> getDeclaredMethods() {
		return Arrays.stream(referenceBinding.methods())
				.map(m -> new EcjMethodDeclaration(m, typeSolver))
				.collect(Collectors.toSet());
	}

	@Override
	public boolean isAssignableBy(ResolvedType type) {
		if(type.isNull()) {
			return true;
		}

		// TODO look into generics
		if(type.describe().equals(this.getQualifiedName())) {
			return true;
		}

		// TODO: Super classes / interfaces :-/
		return false;
	}

	@Override
	public boolean isAssignableBy(ResolvedReferenceTypeDeclaration other) {
		return isAssignableBy(new ReferenceTypeImpl(other, typeSolver));
	}

	@Override
	public boolean hasDirectlyAnnotation(String qualifiedName) {
		return Stream.of(referenceBinding.getAnnotations())
				.anyMatch(binding -> EcjUtils.getFQN(binding).equals(qualifiedName));
	}

	@Override
	public Optional<ResolvedReferenceTypeDeclaration> containerType() {
		return Optional.ofNullable(referenceBinding.enclosingType())
				.map(type -> EcjFactory.toTypeDeclaration(type, typeSolver));
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
	public boolean isField() {
		return false;
	}

	@Override
	public boolean isParameter() {
		return false;
	}

	@Override
	public boolean isType() {
		return true;
	}

	@Override
	public boolean isClass() {
		return referenceBinding.isClass();
	}

	@Override
	public boolean isEnum() {
		return referenceBinding.isEnum();
	}

	@Override
	public boolean isInterface() {
		return referenceBinding.isInterface();
	}

	@Override
	public AccessSpecifier accessSpecifier() {
		return EcjFactory.modifiersToAccessLevel(referenceBinding.getAccessFlags());
	}

	@Override
	public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes, boolean staticOnly) {
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

			// no need to search for overloaded/inherited methods if the method
			// has no parameters
			if(argumentsTypes.isEmpty() && candidate.getNumberOfParams() == 0) {
				return SymbolReference.solved(candidate);
			}
		}
		return MethodResolutionLogic.findMostApplicable(candidates, name, argumentsTypes, typeSolver);
	}

	@Override
	public Optional<MethodUsage> solveMethodAsUsage(String name, List<ResolvedType> argumentTypes, Context invocationContext, List<ResolvedType> typeParameters) {
		return EcjUtils.getMethodUsage(referenceBinding, name, argumentTypes, typeSolver, getTypeParameters(), typeParameters);
	}

	@Override
	protected ResolvedReferenceType object() {
		return new ReferenceTypeImpl(typeSolver.solveType(Object.class.getCanonicalName()), typeSolver);
	}

	@Override
	public String toString() {
		return "EcjClassDeclaration {" + new String(referenceBinding.readableName()) + "}";
	}

	@Override
	public Optional<Node> toAst() {
		return Optional.empty();
	}
}
