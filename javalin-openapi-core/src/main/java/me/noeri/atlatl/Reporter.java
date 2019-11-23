package me.noeri.atlatl;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public interface Reporter {

	public void report(MessageKind kind, String message, String qualifiedName);

	public default void warning(String message, String qualifiedName) {
		this.report(MessageKind.WARNING, message, qualifiedName);
	}

	public default void error(String message, String qualifiedName) {
		this.report(MessageKind.ERROR, message, qualifiedName);
	}

	public default void warning(String message, Node node) {
		this.report(MessageKind.WARNING, message, node);
	}

	public default void error(String message, Node node) {
		this.report(MessageKind.ERROR, message, node);
	}

	public default void report(MessageKind kind, String message, Node node) {
		try {
			do {
				if(node instanceof ClassOrInterfaceDeclaration) {
					this.report(kind, message, ((ClassOrInterfaceDeclaration) node).resolve().getQualifiedName());
					return;
				} else if(node instanceof FieldDeclaration) {
					ResolvedFieldDeclaration resolvedField = ((FieldDeclaration) node).resolve();
					String declaringType = resolvedField.declaringType().getId();
					String fieldName = resolvedField.getName();
					this.report(kind, message, declaringType + "#" + fieldName);
					return;
				} else if(node instanceof MethodDeclaration) {
					ResolvedMethodDeclaration resolvedMethod = ((MethodDeclaration) node).resolve();
					String declaringType = resolvedMethod.declaringType().getId();
					String methodName = resolvedMethod.getName();
					this.report(kind, message, declaringType + "#" + methodName);
					return;
				}
			} while((node = node.getParentNode().orElse(null)) != null);
		} catch(Exception e) {
			// Silence the error :-O
		}
		this.report(kind, message, "");
	}

	public enum MessageKind {
		ERROR,
		WARNING,
	}

}
