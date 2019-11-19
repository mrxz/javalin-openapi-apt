package me.noeri.atlatl.apt.eclipse;

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
