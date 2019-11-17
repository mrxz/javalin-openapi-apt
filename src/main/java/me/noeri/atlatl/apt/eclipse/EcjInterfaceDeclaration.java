package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.MethodUsage;
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
import com.github.javaparser.symbolsolver.logic.AbstractTypeDeclaration;
import com.github.javaparser.symbolsolver.logic.MethodResolutionCapability;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.MethodResolutionLogic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
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
		System.out.println("[Interface] getAncestors(" + acceptIncompleteList + ")");
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public List<ResolvedFieldDeclaration> getAllFields() {
		System.out.println("[Interface] getAllFields()");
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public Set<ResolvedMethodDeclaration> getDeclaredMethods() {
		System.out.println("[Interface] getDeclaredMethods()");
		// TODO Auto-generated method stub
		return new HashSet<>();
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
		System.out.println("[Interface] hasDirectlyAnnotation(" + qualifiedName + ")");
		return Stream.of(referenceBinding.getAnnotations())
				.anyMatch(binding -> EcjUtils.getFQN(binding).equals(qualifiedName));
	}

	@Override
	public List<ResolvedConstructorDeclaration> getConstructors() {
		return Collections.emptyList();
	}

	@Override
	public Optional<ResolvedReferenceTypeDeclaration> containerType() {
		System.out.println("[Interface] containerType()");
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
		if(referenceBinding.typeVariables().length > 0) {
			// TODO Auto-generated method stub
			System.out.println("[Interface] getTypeParameters()");
		}
		return new ArrayList<>();
	}

	@Override
	public AccessSpecifier accessSpecifier() {
		return EcjFactory.modifiersToAccessLevel(referenceBinding.getAccessFlags());
	}

	@Override
	public Optional<MethodUsage> solveMethodAsUsage(String name, List<ResolvedType> argumentTypes,
			Context invocationContext, List<ResolvedType> typeParameters) {
		System.out.println("[Interface] solveMethodAsUsage(" + name + ", List<ResolvedType> (" + argumentTypes.size() + "), invocationContext, typeParameters (" + typeParameters.size() + "))");
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes, boolean staticOnly) {
		// TODO: Copied from EcjClassDeclaration
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

			// no need to search for overloaded/inherited methods if the method has no parameters
            if(argumentsTypes.isEmpty() && candidate.getNumberOfParams() == 0) {
                return SymbolReference.solved(candidate);
            }

			System.out.println(new String(methodBinding.constantPoolName()) + " -> " + methodBinding);
			return MethodResolutionLogic.findMostApplicable(candidates, name, argumentsTypes, typeSolver);
		}
		return SymbolReference.unsolved(ResolvedMethodDeclaration.class);
	}

	@Override
	public List<ResolvedReferenceType> getInterfacesExtended() {
		System.out.println("[Interface] getInterfacesExtended()");
		// TODO Auto-generated method stub
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
