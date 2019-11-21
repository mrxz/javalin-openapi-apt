package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedEnumDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
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
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class EcjEnumDeclaration extends AbstractTypeDeclaration
		implements ResolvedEnumDeclaration, MethodResolutionCapability, MethodUsageResolutionCapability {

	private final ReferenceBinding referenceBinding;
	private final TypeSolver typeSolver;

	public EcjEnumDeclaration(ReferenceBinding type, TypeSolver typeSolver) {
		this.referenceBinding = type;
		this.typeSolver = typeSolver;
	}

	@Override
	public List<ResolvedConstructorDeclaration> getConstructors() {
		// TODO Implement constructors
		return new ArrayList<>();
	}

	@Override
	public List<ResolvedReferenceType> getAncestors(boolean acceptIncompleteList) {
		// Direct ancestors of an enum are java.lang.Enum and interfaces
		List<ResolvedReferenceType> ancestors = new ArrayList<>();

		String superClassName = EcjUtils.getFQN(referenceBinding.superclass());

		if(superClassName != null) {
			try {
				ancestors.add(new ReferenceTypeImpl(typeSolver.solveType(superClassName), typeSolver));
			} catch(UnsolvedSymbolException e) {
				if(!acceptIncompleteList) {
					// we only throw an exception if we require a complete list;
					// otherwise, we attempt to continue gracefully
					throw e;
				}
			}
		}

		for(String interfazeName : Arrays.stream(referenceBinding.superInterfaces()).map(EcjUtils::getFQN).collect(Collectors.toList())) {
			try {
				ancestors.add(new ReferenceTypeImpl(typeSolver.solveType(interfazeName), typeSolver));
			} catch(UnsolvedSymbolException e) {
				if(!acceptIncompleteList) {
					// we only throw an exception if we require a complete list;
					// otherwise, we attempt to continue gracefully
					throw e;
				}
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
		// TODO Implement solveMethodAsUsage for enum
		return Optional.empty();
	}

	@Override
	public List<ResolvedEnumConstantDeclaration> getEnumConstants() {
		return Arrays.stream(referenceBinding.fields())
				.filter(f -> (f.getAccessFlags() & AccessFlag.ENUM) != 0)
				.map(f -> new EcjEnumConstantDeclaration(f, typeSolver))
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return "EcjEnumDeclaration {" + new String(referenceBinding.readableName()) + "}";
	}

}
