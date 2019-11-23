package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedVoidType;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistAnnotationDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistClassDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistEnumDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistInterfaceDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import java.lang.reflect.Modifier;
import javassist.CtClass;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public final class EcjFactory {

	public static ResolvedType typeUsageFor(TypeBinding typeBinding, TypeSolver typeSolver) {
		if(typeBinding.isArrayType()) {
			return new ResolvedArrayType(typeUsageFor(typeBinding.leafComponentType(), typeSolver));
		} else if(typeBinding.isPrimitiveType()) {
			if(new String(typeBinding.readableName()).equals("void")) { // FIXME: is this even possible?
				return ResolvedVoidType.INSTANCE;
			} else {
				return ResolvedPrimitiveType.byName(new String(typeBinding.readableName()));
			}
		} else {
			if(typeBinding.isInterface()) {
				return new ReferenceTypeImpl(new EcjInterfaceDeclaration((ReferenceBinding) typeBinding, typeSolver), typeSolver);
			} else if(typeBinding.isEnum()) {
				return new ReferenceTypeImpl(new EcjEnumDeclaration((ReferenceBinding) typeBinding, typeSolver), typeSolver);
			} else {
				return new ReferenceTypeImpl(new EcjClassDeclaration((ReferenceBinding) typeBinding, typeSolver), typeSolver);
			}
		}
	}

	public static ResolvedReferenceTypeDeclaration toTypeDeclaration(ReferenceBinding type, TypeSolver typeSolver) {
		if(type.isInterface()) {
			return new EcjInterfaceDeclaration(type, typeSolver);
		} else if(type.isEnum()) {
			return new EcjEnumDeclaration(type, typeSolver);
		} else if(type.isArrayType()) {
			throw new IllegalArgumentException("This method should not be called passing an array");
		} else {
			return new EcjClassDeclaration(type, typeSolver);
		}
	}

	public static ResolvedReferenceTypeDeclaration toTypeDeclaration(CtClass ctClazz, TypeSolver typeSolver) {
		if(ctClazz.isAnnotation()) {
			return new JavassistAnnotationDeclaration(ctClazz, typeSolver);
		} else if(ctClazz.isInterface()) {
			return new JavassistInterfaceDeclaration(ctClazz, typeSolver);
		} else if(ctClazz.isEnum()) {
			return new JavassistEnumDeclaration(ctClazz, typeSolver);
		} else if(ctClazz.isArray()) {
			throw new IllegalArgumentException("This method should not be called passing an array");
		} else {
			return new JavassistClassDeclaration(ctClazz, typeSolver);
		}
	}

	public static AccessSpecifier modifiersToAccessLevel(int accessFlags) {
		if(Modifier.isPublic(accessFlags)) {
			return AccessSpecifier.PUBLIC;
		} else if(Modifier.isProtected(accessFlags)) {
			return AccessSpecifier.PROTECTED;
		} else if(Modifier.isPrivate(accessFlags)) {
			return AccessSpecifier.PRIVATE;
		} else {
			return AccessSpecifier.PACKAGE_PRIVATE;
		}
	}
}
