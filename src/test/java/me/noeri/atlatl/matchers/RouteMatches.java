package me.noeri.atlatl.matchers;

import me.noeri.atlatl.route.Route;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class RouteMatches extends TypeSafeMatcher<Route> {

	private final String method;
	private final String path;

	public RouteMatches(String method, String path) {
		this.method = method;
		this.path = path;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("matches route " + method + " " + path);
	}

	@Override
	protected boolean matchesSafely(Route item) {
		return item.getMethod().equals(method)
				&& item.getPath().equals(path);
	}

}
