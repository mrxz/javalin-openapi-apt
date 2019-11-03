package me.noeri.atlatl.utils;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import com.github.javaparser.ast.visitor.GenericVisitor;
import java.util.List;
import java.util.function.Function;

public final class Visitors {

	public static <R, A> GenericVisitor<List<R>, A> methodCallVisitor(MethodCallVisitor<List<R>, A> methodCallVisitor) {
		return new GenericListVisitorAdapter<R, A>() {
			@Override
			public List<R> visit(MethodCallExpr expression, A arg) {
				return methodCallVisitor.visit((updatedArg) -> super.visit(expression, updatedArg), expression, arg, this);
			}
		};
	}

	@FunctionalInterface
	public interface MethodCallVisitor<R, A> {
		public R visit(Function<A, R> recurse, MethodCallExpr expression, A arg, GenericVisitor<R, A> visitor);
	}
}
