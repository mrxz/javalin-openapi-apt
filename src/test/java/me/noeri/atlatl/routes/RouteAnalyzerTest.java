package me.noeri.atlatl.routes;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import java.util.List;
import me.noeri.atlatl.TestBase;
import me.noeri.atlatl.route.Route;
import me.noeri.atlatl.route.RouteAnalyzer;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static me.noeri.atlatl.matchers.Matchers.isRoute;

public class RouteAnalyzerTest extends TestBase {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldHandleSimpleRoutes() {
		ParseResult<CompilationUnit> cu = parser.parse(getResource("SimpleRoutes.java.txt"));

		RouteAnalyzer routeAnalyzer = new RouteAnalyzer(typeSolver);
		List<Route> actual = routeAnalyzer.analyze(cu.getResult().get());

		assertThat(actual, containsInAnyOrder(
				isRoute("GET", "/first/second/third"),
				isRoute("DELETE", "/first/fourth/fifth"),
				isRoute("POST", "/first/fourth/sixth"),
				isRoute("PUT", "/first/fourth/seventh"),
				isRoute("GET", "/first/fourth")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldHandlePathParams() {
		ParseResult<CompilationUnit> cu = parser.parse(getResource("PathParams.java.txt"));

		RouteAnalyzer routeAnalyzer = new RouteAnalyzer(typeSolver);
		List<Route> actual = routeAnalyzer.analyze(cu.getResult().get());

		assertThat(actual, hasSize(4));
		assertThat(actual, containsInAnyOrder(
				isRoute("GET", "/{customerId}"),
				isRoute("POST", "/{customerId}/items"),
				isRoute("DELETE", "/{customerId}/items/{itemId}"),
				isRoute("PUT", "/{customerId}/items/{itemId}")));
	}
}
