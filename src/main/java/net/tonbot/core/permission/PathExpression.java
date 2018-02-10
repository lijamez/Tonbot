package net.tonbot.core.permission;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

class PathExpression {

	private static final String WILDCARD = "*";
	private static final String DOUBLE_WILDCARD = "**";

	@JsonProperty("pathExp")
	private final List<String> pathExp;

	/**
	 * Creates a path expression. A path expression must not contain null
	 * components. If a component is an asterisk, then it is considered to be a
	 * wildcard. A component that has double asterisks is considered to be a
	 * wildcard path. Double asterisks are only permitted at the end of paths.
	 * 
	 * @param pathExp
	 *            The path expression. Non-null.
	 */
	public PathExpression(String pathExp) {
		this(Arrays.asList(StringUtils.split(pathExp, " ")));
	}

	/**
	 * Creates a path expression. A path expression must not contain null
	 * components. If a component is an asterisk, then it is considered to be a
	 * wildcard. A component that has double asterisks is considered to be a
	 * wildcard path. Double asterisks are only permitted at the end of paths.
	 * 
	 * @param pathExp
	 *            The path expression. Non-null.
	 */
	@JsonCreator
	public PathExpression(@JsonProperty("pathExp") List<String> pathExp) {
		Preconditions.checkNotNull(pathExp, "pathExp must be non-null.");

		validate(pathExp);

		this.pathExp = ImmutableList.copyOf(pathExp);
	}

	private void validate(List<String> pathExp) {

		// All path components other than the last component must NOT be the double
		// wildcard.
		for (int i = 0; i < pathExp.size(); i++) {
			String component = pathExp.get(i);

			if (component == null) {
				throw new MalformedPathExpressionException("Path expressions must not contain null components.");
			}

			if (StringUtils.isBlank(component)) {
				throw new MalformedPathExpressionException("Path expressions must not contain blank components.");
			}

			if (i != pathExp.size() - 1 && StringUtils.equals(component, DOUBLE_WILDCARD)) {
				throw new MalformedPathExpressionException("Double wildcard is only permitted at the end of the path.");
			}
		}
	}

	/**
	 * Checks whether if this path expression matches a path.
	 * 
	 * @param path
	 *            The path to check against. Non-null.
	 * @return True if this path expression matches the given path. False otherwise.
	 */
	public boolean matches(String path) {
		return matches(Arrays.asList(StringUtils.split(path, " ")));
	}

	/**
	 * Checks whether if this path expression matches a path.
	 * 
	 * @param path
	 *            The path to check against. Non-null.
	 * @return True if this path expression matches the given path. False otherwise.
	 */
	public boolean matches(List<String> path) {
		Preconditions.checkNotNull(path, "path must be non-null.");

		if (this.pathExp.isEmpty()) {
			return path.isEmpty();
		}

		boolean doubleWildcarded = false;
		List<String> effectivePathExp;
		if (StringUtils.equals(this.pathExp.get(this.pathExp.size() - 1), DOUBLE_WILDCARD)) {
			doubleWildcarded = true;
			effectivePathExp = this.pathExp.subList(0, this.pathExp.size() - 1);
		} else {
			effectivePathExp = this.pathExp;
		}

		if (path.size() < effectivePathExp.size()) {
			return false;
		}

		if (path.size() > effectivePathExp.size() && !doubleWildcarded) {
			return false;
		}

		for (int i = 0; i < effectivePathExp.size(); i++) {
			String ourComponent = effectivePathExp.get(i);
			String theirComponent = path.get(i);

			if (!StringUtils.equals(ourComponent, WILDCARD) && !StringUtils.equals(ourComponent, theirComponent)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return StringUtils.join(this.pathExp, " ");
	}
}
