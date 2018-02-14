package net.tonbot.core.request;

@SuppressWarnings("serial")
public class RequestMappingException extends RuntimeException {

	public RequestMappingException(String message) {
		super(message);
	}

	public RequestMappingException(String message, Exception e) {
		super(message, e);
	}
}
