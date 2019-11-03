package me.noeri.atlatl.route;

import com.github.javaparser.ast.expr.Expression;

public class Route {

	private final String method;
	private final String path;
	private final Expression action;

	public Route(String method, String path, Expression action) {
		this.method = method;
		this.path = path;
		this.action = action;
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public Expression getAction() {
		return action;
	}

}
