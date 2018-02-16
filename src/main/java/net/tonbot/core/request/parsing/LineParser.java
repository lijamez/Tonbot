package net.tonbot.core.request.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.core.request.Context;

public class LineParser {

	private final List<Parser> parsers;

	@Inject
	public LineParser(List<Parser> parsers) {
		Preconditions.checkNotNull(parsers, "parsers must be non-null.");
		this.parsers = ImmutableList.copyOf(parsers);
	}

	/**
	 * Parses the given line.
	 * 
	 * @param args
	 *            The line to be parsed. Non-null.
	 * @param types
	 *            The expected types. Non-null.
	 * @param readRemaining
	 *            For the last object (which must be a string), just read whatever
	 *            is left.
	 * @param context
	 *            {@link Context}. Non-null.
	 * @return A list of parsed objects of the same size as {@code types}.
	 * @throws ParseException
	 *             If there was an error parsing some of the content due to
	 *             incorrectly formatted content.
	 */
	public List<Object> parse(String args, List<Class<?>> types, boolean readRemaining, Context context) {
		Preconditions.checkNotNull(args, "args must be non-null.");
		Preconditions.checkNotNull(types, "types must be non-null.");

		if (readRemaining) {
			Class<?> lastClass = types.get(types.size() - 1);
			Preconditions.checkArgument(CharSequence.class.isAssignableFrom(lastClass),
					"When readRemaining is true, the last type must be assignable from a CharSequence.");
		}

		String remainingArgs = args.trim();

		List<Object> parsedValues = new ArrayList<>(Collections.nCopies(types.size(), null));

		int i = 0;
		while (!remainingArgs.isEmpty() && i < types.size()) {
			Class<?> typeToParse = types.get(i);

			ParseResult pr;
			if (i == types.size() - 1 && readRemaining && CharSequence.class.isAssignableFrom(typeToParse)) {
				pr = new ParseResult(remainingArgs, StringUtils.EMPTY);
			} else {
				Parser suitableParser = parsers.stream().filter(parser -> parser.supports(typeToParse)).findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Unsupported type " + typeToParse + "."));

				pr = suitableParser.parse(remainingArgs, typeToParse, context);
			}

			parsedValues.set(i, pr.getParsedValue());
			remainingArgs = pr.getRemainingArgs().trim();
			i++;
		}

		return parsedValues;
	}
}
