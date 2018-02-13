package net.tonbot.core.request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.util.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Data;

class LineParser {

	private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?[0-9]+");
	private static final Pattern FLOATING_POINT_PATTERN = Pattern.compile("^-?([0-9]*\\.[0-9]+|[0-9]+\\.[0-9]*)");
	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)", Pattern.CASE_INSENSITIVE);
	private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^([\"'])(?<content>(?:\\\\\\1|.)*?)\\1");
	private static final Pattern SINGLE_WORD_PATTERN = Pattern.compile("^(?<content>\\S+)");

	/**
	 * Parses the given line.
	 * 
	 * @param args
	 *            The line to be parsed. Non-null.
	 * @param types
	 *            The expected types. Non-null.
	 * @return A list of parsed objects of the same size as {@code types}.
	 */
	public List<Object> parse(String args, List<Class<?>> types) {
		Preconditions.checkNotNull(args, "args must be non-null.");
		Preconditions.checkNotNull(types, "types must be non-null.");

		String remainingArgs = args.trim();

		List<Object> parsedValues = new ArrayList<>(Collections.nCopies(types.size(), null));

		int i = 0;
		while (remainingArgs.length() != 0 && i < types.size()) {

			Class<?> typeToParse = types.get(i);

			ParseResult pr;
			if (typeToParse == Integer.class || typeToParse == int.class) {
				pr = parseInteger(remainingArgs);
			} else if (typeToParse == Long.class || typeToParse == long.class) {
				pr = parseLong(remainingArgs);
			} else if (typeToParse == Short.class || typeToParse == short.class) {
				pr = parseShort(remainingArgs);
			} else if (typeToParse == Float.class || typeToParse == float.class) {
				pr = parseFloat(remainingArgs);
			} else if (typeToParse == Double.class || typeToParse == double.class) {
				pr = parseDouble(remainingArgs);
			} else if (typeToParse == Boolean.class || typeToParse == boolean.class) {
				pr = parseBoolean(remainingArgs);
			} else if (typeToParse == String.class) {
				pr = parseString(remainingArgs);
			} else if (typeToParse.isEnum()) {
				pr = parseEnum(remainingArgs, typeToParse);
			} else {
				throw new IllegalArgumentException("Unsupported type " + typeToParse + ".");
			}

			parsedValues.set(i, pr.getParsedValue());
			remainingArgs = pr.getRemainingArgs().trim();
			i++;
		}

		return parsedValues;
	}

	private ParseResult parseInteger(String args) {
		Matcher matcher = INTEGER_PATTERN.matcher(args);
		if (matcher.find()) {
			// Since the INTEGER_PATTERN must match at the beginning of the string, we can
			// assume that matcher.start() is 0.
			String remainingArgs = args.substring(matcher.end(), args.length());
			String matchedValue = matcher.group(0);

			Integer parsedValue = Integer.parseInt(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException(
					"Line parser encountered unexpected input which could not be parsed as an integer.");
		}
	}

	private ParseResult parseLong(String args) {
		Matcher matcher = INTEGER_PATTERN.matcher(args);
		if (matcher.find()) {
			// Since the INTEGER_PATTERN must match at the beginning of the string, we can
			// assume that matcher.start() is 0.
			String remainingArgs = args.substring(matcher.end(), args.length());
			String matchedValue = matcher.group(0);

			Long parsedValue = Long.parseLong(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException("Line parser encountered unexpected input which could not be parsed as a long.");
		}
	}

	private ParseResult parseShort(String args) {
		Matcher matcher = INTEGER_PATTERN.matcher(args);
		if (matcher.find()) {
			// Since the INTEGER_PATTERN must match at the beginning of the string, we can
			// assume that matcher.start() is 0.
			String remainingArgs = args.substring(matcher.end(), args.length());
			String matchedValue = matcher.group(0);

			Short parsedValue = Short.parseShort(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException("Line parser encountered unexpected input which could not be parsed as a short.");
		}
	}

	private ParseResult parseFloat(String args) {
		Matcher matcher = FLOATING_POINT_PATTERN.matcher(args);
		if (matcher.find()) {
			// Since the FLOATING_POINT_PATTERN must match at the beginning of the string,
			// we can
			// assume that matcher.start() is 0.
			String remainingArgs = args.substring(matcher.end(), args.length());
			String matchedValue = matcher.group(0);

			float parsedValue = Float.parseFloat(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException("Line parser encountered unexpected input which could not be parsed as a float.");
		}
	}

	private ParseResult parseDouble(String args) {
		Matcher matcher = FLOATING_POINT_PATTERN.matcher(args);
		if (matcher.find()) {
			// Since the FLOATING_POINT_PATTERN must match at the beginning of the string,
			// we can
			// assume that matcher.start() is 0.
			String remainingArgs = args.substring(matcher.end(), args.length());
			String matchedValue = matcher.group(0);

			double parsedValue = Double.parseDouble(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException("Line parser encountered unexpected input which could not be parsed as a double.");
		}
	}

	private ParseResult parseBoolean(String args) {
		Matcher matcher = BOOLEAN_PATTERN.matcher(args);
		if (matcher.find()) {
			// Since the BOOLEAN_PATTERN must match at the beginning of the string, we can
			// assume that matcher.start() is 0.
			String remainingArgs = args.substring(matcher.end(), args.length());
			String matchedValue = matcher.group(0);

			boolean parsedValue = Boolean.parseBoolean(matchedValue);

			return new ParseResult(parsedValue, remainingArgs);
		} else {
			throw new ParseException(
					"Line parser encountered unexpected input which could not be parsed as a boolean.");
		}
	}

	private ParseResult parseString(String args) {

		List<Matcher> matchers = ImmutableList.of(
				QUOTED_STRING_PATTERN.matcher(args),
				SINGLE_WORD_PATTERN.matcher(args));

		Matcher matchedMatcher = matchers.stream()
				.filter(matcher -> matcher.find())
				.findFirst()
				.orElseThrow(() -> new ParseException(
						"Line parser encountered unexpected input which could not be parsed as a string."));

		String remainingArgs = args.substring(matchedMatcher.end(), args.length());
		String matchedValue = matchedMatcher.group("content");

		return new ParseResult(matchedValue, remainingArgs);
	}

	private ParseResult parseEnum(String args, Class<?> enumeration) {
		Matcher matcher = SINGLE_WORD_PATTERN.matcher(args);

		if (matcher.find()) {
			String remainingArgs = args.substring(matcher.end(), args.length());
			String matchedValue = matcher.group("content");

			// All enum values should have a name() method
			Method nameMethod;
			try {
				nameMethod = enumeration.getMethod("name");
			} catch (NoSuchMethodException | SecurityException e1) {
				// Since parseEnum is always called with an enumeration class, this should never
				// happen.
				throw new IllegalStateException("Couldn't find the name() method of an enum value!");
			}

			List<Object> enumValues = Arrays.asList(enumeration.getEnumConstants());
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
									+ enumeration + "."));

			return new ParseResult(enumValue, remainingArgs);
		} else {
			throw new ParseException("Line parser encountered unexpected input which could not be parsed as an enum "
					+ enumeration + ".");
		}
	}

	@Data
	private class ParseResult {
		private final Object parsedValue;
		private final String remainingArgs;
	}
}
