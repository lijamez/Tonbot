package net.tonbot.core.request.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.util.Preconditions;

import net.tonbot.core.request.Context;

class BooleanParser implements Parser {

	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean supports(Class<?> type) {
		Preconditions.checkNotNull(type, "type must be non-null.");
		return type == Boolean.class || type == boolean.class;
	}

	@Override
	public ParseResult parse(String content, Class<?> targetType, Context context) {
		Preconditions.checkNotNull(content, "content must be non-null.");

		Matcher matcher = BOOLEAN_PATTERN.matcher(content);
		if (matcher.find()) {
			// Since the BOOLEAN_PATTERN must match at the beginning of the string, we can
			// assume that matcher.start() is 0.
			String remainingArgs = content.substring(matcher.end(), content.length());
			String matchedValue = matcher.group(0);

			boolean parsedValue = Boolean.parseBoolean(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException(
					"Line parser encountered unexpected input which could not be parsed as a boolean.");
		}
	}

}
