/*
 * Copyright (c) 2019 Noeri Huisman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.noeri.atlatl.operation;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
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
import me.noeri.atlatl.utils.NodeUtils;
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

	public Operation analyzeOperation(Node operationNode, String operationId) {
		Operation result = new Operation()
				.operationId(operationId);
		ApiResponses responses = new ApiResponses()
				.addApiResponse("200", new ApiResponse()
						.description("Successful operation"));
		result.responses(responses);

		// Parse JavaDoc for method summary and description.
		operationNode.findFirst(MethodDeclaration.class)
			.flatMap(MethodDeclaration::getJavadoc)
			.ifPresent(javaDoc -> {
				Optional<String> summaryAndDescription = javaDoc.getDescription().getElements().stream()
						.map(JavadocDescriptionElement::toText)
						.findFirst();
				if(summaryAndDescription.isPresent()) {
					String[] parts = summaryAndDescription.get().split("\n", 2);
					result.summary(parts[0]);
					if(parts.length == 2) {
						// FIXME: Actually convert JavaDoc to markdown
						result.description(parts[1]);
					}
				}
			});

		operationNode.accept(Visitors.methodCallVisitor((recurse, expression, operation, visitor) -> {
			ResolvedType resolvedType = null;
			try {
				resolvedType = expression.getScope().map(Expression::calculateResolvedType).orElse(null);
			} catch(Exception e) {}
			if(resolvedType != null) {
				// Check for invocations on the context.
				if(TypeUtils.isAssignable(resolvedType, contextType)) {
					// Check for any comments directly above the containing statement.
					Optional<String> comment = NodeUtils.findFirstAncestor(Statement.class, expression)
							.flatMap(Node::getComment)
							.filter(Comment::isLineComment)
							.map(Comment::getContent)
							.map(String::trim);
					String javalinMethod = expression.getNameAsString();
					switch(javalinMethod) {
					case "pathParam":
						result.addParametersItem(new Parameter()
								.in("path")
								.name(expression.getArgument(0).asStringLiteralExpr().getValue())
								.description(comment.orElse(null))
								.schema(determineParameterSchema(expression))
								.required(true));
						break;
					case "queryParam":
						result.addParametersItem(new Parameter()
								.in("query")
								.name(expression.getArgument(0).asStringLiteralExpr().getValue())
								.description(comment.orElse(null))
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
										.description(comment.orElse("Successful operation"))
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
