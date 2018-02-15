package net.tonbot.core.request.parsing;

@SuppressWarnings("serial")
public class ParseException extends RuntimeException {

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Exception e) {
		super(message, e);
	}
}
