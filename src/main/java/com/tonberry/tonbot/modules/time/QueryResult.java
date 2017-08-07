package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
class QueryResult {

    private final boolean success;
    private final boolean error;
    private final List<Pod> pods;

    @JsonCreator
    public QueryResult(
            @JsonProperty("success") boolean success,
            @JsonProperty("error") boolean error,
            @JsonProperty("pods") List<Pod> pods) {

        this.success = success;
        this.error = error;
        this.pods = pods;
    }
}