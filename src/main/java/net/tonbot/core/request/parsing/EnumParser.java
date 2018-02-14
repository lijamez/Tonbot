package net.tonbot.core.request.parsing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.util.Preconditions;

import net.tonbot.core.request.Context;

class EnumParser implements Parser {

	private static final Pattern SINGLE_WORD_PATTERN = Pattern.compile("^(?<content>\\S+)");

	@Override
	public boolean supports(Class<?> type) {
		Preconditions.checkNotNull(type, "type must be non-null.");
		return type.isEnum();
	}

	@Override
	public ParseResult parse(String content, Class<?> targetType, Context context) {
		Preconditions.checkNotNull(content, "content must be non-null.");
		Preconditions.checkNotNull(targetType, "targetType must be non-null.");
		Preconditions.checkArgument(targetType.isEnum(), "Supplied targetType class isn't really an enum class.");

		Matcher matcher = SINGLE_WORD_PATTERN.matcher(content);

		if (matcher.find()) {
			String remainingArgs = content.substring(matcher.end(), content.length());
			String matchedValue = matcher.group("content");

			// All enum values should have a name() method
			Method nameMethod;
			try {
				nameMethod = targetType.getMethod("name");
			} catch (NoSuchMethodException | SecurityException e1) {
				// Since parseEnum is always called with an enumeration class, this should never
				// happen.
				throw new IllegalStateException("Couldn't find the name() method of an enum value!");
			}

			List<Object> enumValues = Arrays.asList(targetType.getEnumConstants());
			Object enumValue = enumValues.stream()
					.filter(ev -> {
						try {
							return StringUtils.equalsIgnoreCase((CharSequence) nameMethod.invoke(ev), matchedValue);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new IllegalStateException("Couldn't call the enum value's name() method.", e);
						}
					})
					.findFirst()
					.orElseThrow(() -> new ParseException(
							"Line parser encountered unexpected input which could not be parsed as an enum "
									+ targetType + "."));

			return new ParseResult(enumValue, remainingArgs);
		} else {
			throw new ParseException("Line parser encountered unexpected input which could not be parsed as an enum "
					+ targetType + ".");
		}
	}
}
