package net.tonbot.core.request.parsing;

import lombok.Data;

@Data
class ParseResult {
	private final Object parsedValue;
	private final String remainingArgs;
}
