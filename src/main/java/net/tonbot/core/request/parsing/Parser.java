package net.tonbot.core.request.parsing;

import net.tonbot.core.request.Context;

interface Parser {

	/**
	 * Checks whether if this parser supports parsing objects to the given class.
	 * 
	 * @param type
	 *            The type to output.
	 * @return True if this parser supports outputting this kind of object.
	 */
	boolean supports(Class<?> type);

	/**
	 * Parses the content from the beginning.
	 * 
	 * @param content
	 *            The content to parse. Non-null.
	 * @param targetType
	 *            The class that is being asked for. Non-null.
	 * @param context
	 *            {@link Context}. Non-null.
	 * @return A {@link ParseResult} containing an object that was parsed, and
	 *         whatever is remaining of the {@code content}.
	 * @throws ParseException
	 *             if the content couldn't be parsed.
	 */
	ParseResult parse(String content, Class<?> targetType, Context context);
}
