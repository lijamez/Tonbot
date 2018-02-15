package net.tonbot.core.request.parsing;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import net.tonbot.core.request.Context;

class StringParser implements Parser {

	private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^([\"'])(?<content>(?:\\\\\\1|.)*?)\\1");
	private static final Pattern SINGLE_WORD_PATTERN = Pattern.compile("^(?<content>\\S+)");

	@Override
	public boolean supports(Class<?> type) {
		Preconditions.checkNotNull(type, "type must be non-null.");
		return CharSequence.class.isAssignableFrom(type);
	}

	@Override
	public ParseResult parse(String content, Class<?> targetType, Context context) {
		Preconditions.checkNotNull(content, "content must be non-null.");

		List<Matcher> matchers = ImmutableList.of(
				QUOTED_STRING_PATTERN.matcher(content),
				SINGLE_WORD_PATTERN.matcher(content));

		Matcher matchedMatcher = matchers.stream()
				.filter(matcher -> matcher.find())
				.findFirst()
				.orElseThrow(() -> new ParseException(
						"Line parser encountered unexpected input which could not be parsed as a string."));

		String remainingArgs = content.substring(matchedMatcher.end(), content.length());
		String matchedValue = matchedMatcher.group("content");

		return new ParseResult(matchedValue, remainingArgs);
	}

}
