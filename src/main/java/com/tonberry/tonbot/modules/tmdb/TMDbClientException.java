package com.tonberry.tonbot.modules.tmdb;

public class TMDbClientException extends RuntimeException {

    public TMDbClientException(String message, Exception causedBy) {
        super(message, causedBy);
    }

    public TMDbClientException(String message) {
        super(message);
    }
}
