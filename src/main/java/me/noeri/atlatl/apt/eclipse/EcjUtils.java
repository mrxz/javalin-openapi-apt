package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.MethodResolutionLogic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public final class EcjUtils {

	public static Optional<MethodUsage> getMethodUsage(ReferenceBinding referenceBinding, String name, List<ResolvedType> argumentsTypes, TypeSolver typeSolver, List<ResolvedTypeParameterDeclaration> typeParameters, List<ResolvedType> typeParameterValues) {
		List<MethodUsage> methods = new ArrayList<>();
		for(MethodBinding method : referenceBinding.methods()) {
			if(new String(method.constantPoolName()).equals(name) && !method.isBridge() && !method.isSynthetic()) {
				MethodUsage methodUsage = new MethodUsage(new EcjMethodDeclaration(method, typeSolver));
				for(int i = 0; i < typeParameters.size() && i < typeParameterValues.size(); i++) {
					ResolvedTypeParameterDeclaration tpToReplace = typeParameters.get(i);
					ResolvedType newValue = typeParameterValues.get(i);
					methodUsage = methodUsage.replaceTypeParameter(tpToReplace, newValue);
				}
				methods.add(methodUsage);

				// no need to search for overloaded/inherited methods if the
				// method has no parameters
				if(argumentsTypes.isEmpty() && methodUsage.getNoParams() == 0) {
					return Optional.of(methodUsage);
				}
			}
		}

		ReferenceBinding superClass = referenceBinding.superclass();
		if(superClass != null) {
			Optional<MethodUsage> ref = EcjUtils.getMethodUsage(superClass, name, argumentsTypes, typeSolver, typeParameters, typeParameterValues);
			if(ref.isPresent()) {
				methods.add(ref.get());
			}
		}

		for(ReferenceBinding interfaze : referenceBinding.superInterfaces()) {
			Optional<MethodUsage> ref = EcjUtils.getMethodUsage(interfaze, name, argumentsTypes, typeSolver, typeParameters, typeParameterValues);
			if(ref.isPresent()) {
				methods.add(ref.get());
			}
		}

		return MethodResolutionLogic.findMostApplicableUsage(methods, name, argumentsTypes, typeSolver);
	}

	public static String getFQN(TypeBinding typeBinding) {
		StringBuilder builder = new StringBuilder();
		builder.append(typeBinding.qualifiedPackageName());
		if(builder.length() > 0) {
			builder.append(".");
		}
		builder.append(typeBinding.qualifiedSourceName());
		return builder.toString();
	}

	public static String getFQN(AnnotationBinding annotationBinding) {
		return getFQN(annotationBinding.getAnnotationType());
	}

	public static String getGenericSignature(ReferenceBinding referenceBinding) {
		if(referenceBinding.isParameterizedType()) {
			return getGenericSignature(referenceBinding.actualType());
		}
		String genericTypeSignature = Arrays.stream(referenceBinding.typeVariables())
			.map(TypeVariableBinding::genericSignature)
			.map(String::new)
			.collect(Collectors.joining("", "<", ">"));
		String superClassSignature = new String(referenceBinding.superclass().genericTypeSignature());
		String genericSignature = genericTypeSignature + superClassSignature;
		if(referenceBinding.superInterfaces().length > 0) {
			genericSignature += Arrays.stream(referenceBinding.superInterfaces())
					.map(ReferenceBinding::genericTypeSignature)
					.map(String::new)
					.collect(Collectors.joining(""));
		}
		return genericSignature;
	}
}
