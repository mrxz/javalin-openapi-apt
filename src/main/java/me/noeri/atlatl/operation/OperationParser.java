package me.noeri.atlatl.operation;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Optional;
import me.noeri.atlatl.Reporter;
import me.noeri.atlatl.schema.BoxedPrimitivesUtils;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.utils.TypeUtils;
import me.noeri.atlatl.utils.Visitors;

public class OperationParser {
	private final ResolvedReferenceTypeDeclaration contextType;
	private final SchemaRegistry schemaRegistry;
	private final Reporter reporter;

	public OperationParser(TypeSolver typeSolver, SchemaRegistry schemaRegistry, Reporter reporter) {
		contextType = typeSolver.solveType("io.javalin.http.Context").asReferenceType();
		this.schemaRegistry = schemaRegistry;
		this.reporter = reporter;
	}

	public Operation analyzeOperation(Node routeBuilderNode, String operationId) {
		Operation result = new Operation()
				.operationId(operationId);
		ApiResponses responses = new ApiResponses()
				.addApiResponse("200", new ApiResponse()
						.description("Successful operation"));
		result.responses(responses);

		routeBuilderNode.accept(Visitors.methodCallVisitor((recurse, expression, operation, visitor) -> {
			ResolvedType resolvedType = null;
			try {
				resolvedType = expression.getScope().map(Expression::calculateResolvedType).orElse(null);
			} catch(Exception e) {}
			if(resolvedType != null) {
				// Check for invocations on the context.
				if(TypeUtils.isAssignable(resolvedType, contextType)) {
					String javalinMethod = expression.getNameAsString();
					switch(javalinMethod) {
					case "pathParam":
						result.addParametersItem(new Parameter()
								.in("path")
								.name(expression.getArgument(0).asStringLiteralExpr().getValue())
								.schema(determineParameterSchema(expression))
								.required(true));
						break;
					case "queryParam":
						result.addParametersItem(new Parameter()
								.in("query")
								.name(expression.getArgument(0).asStringLiteralExpr().getValue())
								.schema(determineParameterSchema(expression)));
						break;
					case "bodyAsClass":
					case "bodyValidator":
						ResolvedType bodyType = expression.getArgument(0).asClassExpr().getType().resolve();
						result.requestBody(new RequestBody()
								.required(true)
								.content(new Content()
										.addMediaType("application/json", new MediaType()
												.schema(schemaRegistry.getSchemaOrReferenceFor(bodyType)))));
						break;
					case "json":
						ResolvedType responseType = expression.getArgument(0).calculateResolvedType();
						responses.addApiResponse("200", new ApiResponse()
										.description("Successful operation")
										.content(new Content()
												.addMediaType("application/json", new MediaType()
														.schema(schemaRegistry.getSchemaOrReferenceFor(responseType)))));
						break;
					case "status":
						// FIXME
						// responses.addApiResponse("" + call.getArgument(0).asIntegerLiteralExpr(), new ApiResponse());
						break;
					default:
						reporter.warning("Unsupported javalin method: " + javalinMethod, expression);
						break;
					}
				}
			}

			// Check for invocations passing the context along.
			for(Expression argument : expression.getArguments()) {
				if(!argument.isNameExpr()) {
					continue;
				}
				ResolvedValueDeclaration valueDeclaration = argument.asNameExpr().resolve();
				try {
					if(!valueDeclaration.isParameter() || !TypeUtils.isAssignable(valueDeclaration.asParameter().getType(), contextType)) {
						continue;
					}
				} catch(Exception e) {
					reporter.warning("Unable to check argument '" + argument + "' of method invocation: " + expression, expression);
					continue;
				}

				Optional<MethodDeclaration> methodCode = expression.resolve().toAst();
				if(methodCode.isPresent()) {
					visitor.visit(methodCode.get(), operation);
				} else {
					reporter.warning("Unable to resolve AST for method: " + expression, expression);
				}
			}
			return recurse.apply(operation);
		}), result);
		return result;
	}

	private Schema<?> determineParameterSchema(MethodCallExpr expression) {
		if(expression.getArguments().size() != 2) {
			// No type information
			return new StringSchema();
		}

		Expression typeArgumentExpression = expression.getArgument(1);
		if(typeArgumentExpression.isClassExpr()) {
			ResolvedType resolvedType = typeArgumentExpression.asClassExpr().getType().resolve();
			Schema<?> potentialSchema = BoxedPrimitivesUtils.getSchemaFor(resolvedType);
			if(potentialSchema != null) {
				return potentialSchema;
			}
		}
		return new StringSchema();
	}
}
