package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.core.resolution.MethodUsageResolutionCapability;
import com.github.javaparser.symbolsolver.logic.AbstractClassDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.resolution.MethodResolutionLogic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
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
		if(referenceBinding.superclass() == null) {
			return new ReferenceTypeImpl(typeSolver.solveType(Object.class.getCanonicalName()), typeSolver);
		}
		if(!referenceBinding.isGenericType()) {
			return new ReferenceTypeImpl(typeSolver.solveType(new String(referenceBinding.superclass().qualifiedSourceName())), typeSolver);
		}
		System.err.println("Generic reference types aren't supported: " + getQualifiedName());
		throw new RuntimeException("Generic reference types aren't supported");
	}

	@Override
	public List<ResolvedReferenceType> getInterfaces() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public List<ResolvedConstructorDeclaration> getConstructors() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public List<ResolvedReferenceType> getAncestors(boolean acceptIncompleteList) {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public List<ResolvedFieldDeclaration> getAllFields() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public Set<ResolvedMethodDeclaration> getDeclaredMethods() {
		// TODO Auto-generated method stub
		return new HashSet<>();
	}

	@Override
	public boolean isAssignableBy(ResolvedType type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAssignableBy(ResolvedReferenceTypeDeclaration other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasDirectlyAnnotation(String qualifiedName) {
		return Stream.of(referenceBinding.getAnnotations())
				.anyMatch(binding -> EcjUtils.getFQN(binding).equals(qualifiedName));
	}

	@Override
	public Optional<ResolvedReferenceTypeDeclaration> containerType() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return new ArrayList<>();
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
		return !referenceBinding.isInterface();
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
		// TODO Auto-generated method stub
		System.out.println(" Tyring to solve method " + name + " on " + getClassName());

		List<ResolvedMethodDeclaration> candidates = new ArrayList<>();
		for(MethodBinding methodBinding : referenceBinding.methods()) {
			// Ensure name matches
			if(!new String(methodBinding.constantPoolName()).equals(name)) {
				continue;
			}

			// TODO: synthetic and bridge??
			if(methodBinding.isSynthetic() || methodBinding.isBridge()) {
				System.out.println("Synthetic or bridge");
				continue;
			}

			ResolvedMethodDeclaration candidate = new EcjMethodDeclaration(methodBinding, typeSolver);
			candidates.add(candidate);

			// no need to search for overloaded/inherited methods if the method
			// has no parameters
			if(argumentsTypes.isEmpty() && candidate.getNumberOfParams() == 0) {
				return SymbolReference.solved(candidate);
			}

			System.out.println(new String(methodBinding.constantPoolName()) + " -> " + methodBinding);
		}
		return MethodResolutionLogic.findMostApplicable(candidates, name, argumentsTypes, typeSolver);
	}

	@Override
	public Optional<MethodUsage> solveMethodAsUsage(String name, List<ResolvedType> argumentTypes, Context invocationContext, List<ResolvedType> typeParameters) {
		// TODO Auto-generated method stub
		return Optional.empty();
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
