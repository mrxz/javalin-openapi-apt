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
