package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
class WolframAlphaQueryResponse {

    private final QueryResult queryResult;

    @JsonCreator
    WolframAlphaQueryResponse(@JsonProperty("queryresult") QueryResult queryResult) {
        this.queryResult = queryResult;
    }
}
