package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
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
		System.out.println("Trying to resolve: " + name);

		String[] parts = name.split("\\.");
		char[][] lookupArg = new char[parts.length][];
		ReferenceBinding referenceBinding = processingEnvironment.getLookupEnvironment()
				.getType(Stream.of(parts).map(String::toCharArray).collect(Collectors.toList()).toArray(lookupArg));

		if(referenceBinding != null) {
			System.out.println(" Found " + name);
			//CtClass ctClazz = ClassPool.getDefault().makeClass(classfile)
			//return SymbolReference.solved(JavassistFactory.toTypeDeclaration(ctClazz , this));
			return SymbolReference.solved(EcjFactory.toTypeDeclaration(referenceBinding, this));
		} else {
			return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
		}
	}

}
