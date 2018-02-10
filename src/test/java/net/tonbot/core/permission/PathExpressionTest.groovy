package net.tonbot.core.permission

import spock.lang.Specification

class PathExpressionTest extends Specification {

	def "path expression validation - invalid cases"(def pathExpression) {
		when:
		new PathExpression(pathExpression)
		
		then:
		thrown MalformedPathExpressionException
		
		where:
		pathExpression | _
		"a ** b"       | _  
		"** **"        | _
		["a", null]    | _
		["a", ""]      | _
		["a", "  "]    | _
	}
	
	def "path expression matching"(String pathExpression, String testPath, boolean expectedMatch) {
		given:
		PathExpression pathExp = new PathExpression(pathExpression);
		
		when:
		boolean matches = pathExp.matches(testPath);
		
		then:
		matches == expectedMatch
		
		where:
		pathExpression | testPath       || expectedMatch
		"foo bar"      | "foo bar"      || true
		"foo bar"      | "foo bar baz"  || false
		"foo bar"      | "foo aa"       || false
		"foo bar"      | "foo"          || false
		"foo bar"      | "bar"          || false
		"foo bar"      | ""             || false
		// Single wildcards
		"foo *"        | "foo bar"      || true
		"* bar"        | "foo bar"      || true
		"* bar"        | "foo aa"       || false
		"* bar"        | "foo aa bar"   || false
		"*"            | "foo"          || true
		"*"            | "foo bar"      || false
		"*"            | ""             || false
		// Double wildcards
		"foo bar **"   | "foo bar"      || true
		"foo bar **"   | "foo bar baz"  || true
		"foo bar **"   | "foo x baz"    || false
		"**"           | ""             || true
		"**"           | "foo"          || true
		"**"           | "foo bar"      || true
		// Single and double wildcards
		"foo * **"   | "foo bar"        || true
		"foo * **"   | "foo bar baz"    || true
		"foo * **"   | "x bar"          || false
		"foo * **"   | "foo"            || false
		"* bar **"   | "x bar"          || true
		"* bar **"   | "x bar baz z"    || true
		"* * **"     | "foo bar baz"    || true
		// The empty path expression
		""           | ""               || true
		""           | "foo"            || false
		""           | "foo bar"        || false
	}
	
}
