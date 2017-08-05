package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * https://developers.themoviedb.org/3/search/search-tv-shows
 */
@Data
class TvShowSearchResult {

    private final int page;
    private final List<TvShowHit> results;
    private final int totalResults;
    private final int totalPages;

    @JsonCreator
    public TvShowSearchResult(
            @JsonProperty("page") int page,
            @JsonProperty("results") List<TvShowHit> results,
            @JsonProperty("total_results") int totalResults,
            @JsonProperty("total_pages") int totalPages) {
        this.page = page;
        this.results = results;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
    }
}
