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
package me.noeri.atlatl.routes;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import java.util.List;
import me.noeri.atlatl.TestBase;
import me.noeri.atlatl.route.Route;
import me.noeri.atlatl.route.RouteAnalyzer;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static me.noeri.atlatl.matchers.Matchers.isRoute;

public class RouteAnalyzerTest extends TestBase {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldHandleSimpleRoutes() {
		ParseResult<CompilationUnit> cu = parser.parse(getResource("routes/SimpleRoutes.java.txt"));

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
		ParseResult<CompilationUnit> cu = parser.parse(getResource("routes/PathParams.java.txt"));

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
