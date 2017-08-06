package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.Data;

@Data
class Config {

    private final String wolframAlphaAppId;

    @JsonCreator
    public Config(@JsonProperty("wolframAlphaAppId") String wolframAlphaAppId) {
        this.wolframAlphaAppId = Preconditions.checkNotNull(wolframAlphaAppId, "wolframAlphaAppId must be non-null.");
    }

}
