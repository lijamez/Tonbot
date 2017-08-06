package com.tonberry.tonbot.modules.time;

class WolframAlphaException extends RuntimeException {

    public WolframAlphaException(String message, Exception causedBy) {
        super(message, causedBy);
    }

    public WolframAlphaException(String message) {
        super(message);
    }
}
