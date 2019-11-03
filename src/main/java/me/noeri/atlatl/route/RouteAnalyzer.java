package me.noeri.atlatl.route;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.util.Arrays;
import java.util.List;
import me.noeri.atlatl.utils.TypeUtils;
import me.noeri.atlatl.utils.Visitors;

public class RouteAnalyzer {

	private final ResolvedReferenceTypeDeclaration apiBuilderType;

	public RouteAnalyzer(TypeSolver typeSolver) {
		apiBuilderType = typeSolver.solveType("io.javalin.apibuilder.ApiBuilder").asReferenceType();
	}

	public List<Route> analyze(Node routeBuilderNode) {
		return routeBuilderNode.accept(Visitors.methodCallVisitor((recurse, expression, stack, visitor) -> {
			ResolvedMethodDeclaration method = expression.resolve();
			if(method.declaringType().canBeAssignedTo(apiBuilderType)) {
				boolean hasPathPart = expression.getArgument(0).isStringLiteralExpr();
				if(hasPathPart) {
					String pathPart = expression.getArgument(0).asStringLiteralExpr().getValue();
					stack = stack.clone();
					stack.add(pathPart);
				}

				switch(method.getName()) {
				case "get":
				case "post":
				case "put":
				case "delete":
					Expression action = expression.getArgument(hasPathPart ? 1 : 0);
					// Note: it is safe to return as nested inside a path can't be another path.
					return Arrays.asList(new Route(method.getName().toUpperCase(), stack.fullPath(), action));
				}
			}

			return recurse.apply(stack);
		}), new PathStack());
	}

}
