package me.noeri.atlatl.apt.eclipse;

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
