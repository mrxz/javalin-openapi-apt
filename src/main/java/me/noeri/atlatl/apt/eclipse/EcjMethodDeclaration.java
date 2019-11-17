package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.core.resolution.TypeVariableResolutionCapability;
import com.github.javaparser.symbolsolver.declarations.common.MethodDeclarationCommonLogic;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;

public class EcjMethodDeclaration implements ResolvedMethodDeclaration, TypeVariableResolutionCapability {

	private final MethodBinding methodBinding;
	private final TypeSolver typeSolver;

	public EcjMethodDeclaration(MethodBinding methodBinding, TypeSolver typeSolver) {
		this.methodBinding = methodBinding;
		this.typeSolver = typeSolver;
	}

	@Override
	public ResolvedReferenceTypeDeclaration declaringType() {
		// TODO: Perhaps not go external?
		return EcjFactory.toTypeDeclaration(methodBinding.declaringClass, typeSolver);
	}

	@Override
	public int getNumberOfParams() {
		return methodBinding.parameters.length;
	}

	@Override
	public ResolvedParameterDeclaration getParam(int i) {
		// TODO
		boolean variadic = methodBinding.isVarargs() && i == (methodBinding.parameters.length - 1);

		String paramName = new String(methodBinding.parameterNames[i]);
		System.out.println("Requesting parameter " + i + ": " + paramName + " (" + (variadic ? "variadic" : "normal") + ")");
		// FIXME: Add support for generics, etc...
		// For now we assume that all parameters are class types
		return new EcjParameterDeclaration(methodBinding.parameters[i], typeSolver, variadic, paramName);
	}

	@Override
	public int getNumberOfSpecifiedExceptions() {
		return methodBinding.thrownExceptions.length;
	}

	@Override
	public ResolvedType getSpecifiedException(int index) {
		if(index < 0 || index >= getNumberOfSpecifiedExceptions()) {
			throw new IllegalArgumentException(String.format("No exception with index %d. Number of exceptions: %d",
					index, getNumberOfSpecifiedExceptions()));
		}
		return EcjFactory.typeUsageFor(methodBinding.thrownExceptions[index], typeSolver);
	}

	@Override
	public String getName() {
		return new String(methodBinding.constantPoolName());
	}

	@Override
	public List<ResolvedTypeParameterDeclaration> getTypeParameters() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public AccessSpecifier accessSpecifier() {
		return EcjFactory.modifiersToAccessLevel(methodBinding.getAccessFlags());
	}

	@Override
	public MethodUsage resolveTypeVariables(Context context, List<ResolvedType> parameterTypes) {
		return new MethodDeclarationCommonLogic(this, typeSolver).resolveTypeVariables(context, parameterTypes);
	}

	@Override
	public ResolvedType getReturnType() {
		// TODO Auto-generated method stub
		return EcjFactory.typeUsageFor(methodBinding.returnType, typeSolver);
	}

	@Override
	public boolean isAbstract() {
		return methodBinding.isAbstract();
	}

	@Override
	public boolean isDefaultMethod() {
		return methodBinding.isDefaultMethod();
	}

	@Override
	public boolean isStatic() {
		return methodBinding.isStatic();
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
		return false;
	}

	@Override
	public Optional<MethodDeclaration> toAst() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		return "EcjMethodDeclaration{" + "methodBinding=" + methodBinding + '}';
	}
}
