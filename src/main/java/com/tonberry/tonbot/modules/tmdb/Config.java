package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.Data;

@Data
class Config {

    private final String tmdbApiKey;

    @JsonCreator
    public Config(@JsonProperty("tmdbApiKey") String tmdbApiKey) {
        this.tmdbApiKey = Preconditions.checkNotNull(tmdbApiKey, "tmdbApiKey must be non-null.");
    }

}
