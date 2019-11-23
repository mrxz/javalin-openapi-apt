package me.noeri.atlatl.matchers;

public final class Matchers {

	public static RouteMatches isRoute(String method, String path) {
		return new RouteMatches(method, path);
	}

}
