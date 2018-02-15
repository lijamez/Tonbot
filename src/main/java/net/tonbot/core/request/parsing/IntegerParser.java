package net.tonbot.core.request.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import net.tonbot.core.request.Context;

class IntegerParser implements Parser {

	private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?[0-9]+");

	@Override
	public boolean supports(Class<?> type) {
		Preconditions.checkNotNull(type, "type must be non-null.");
		return type == Integer.class || type == int.class;
	}

	@Override
	public ParseResult parse(String content, Class<?> targetType, Context context) {
		Preconditions.checkNotNull(content, "content must be non-null.");

		Matcher matcher = INTEGER_PATTERN.matcher(content);
		if (matcher.find()) {
			// Since the INTEGER_PATTERN must match at the beginning of the string, we can
			// assume that matcher.start() is 0.
			String remainingArgs = content.substring(matcher.end(), content.length());
			String matchedValue = matcher.group(0);

			Integer parsedValue = Integer.parseInt(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException(
					"Line parser encountered unexpected input which could not be parsed as an integer.");
		}
	}

}
