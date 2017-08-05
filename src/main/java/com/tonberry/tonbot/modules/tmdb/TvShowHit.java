package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class TvShowHit {

    private final String posterPath;
    private final int popularity;
    private final int id;
    private final String backdropPath;
    private final int voteAverage;
    private final String overview;
    private final String firstAirDate;
    private final List<String> originCountry;
    private final List<Integer> genreIds;
    private final String originalLanguage;
    private final int voteCount;
    private final String name;
    private final String originalName;

    @JsonCreator
    public TvShowHit(
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("popularity") int popularity,
            @JsonProperty("id") int id,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("vote_average") int voteAverage,
            @JsonProperty("overview") String overview,
            @JsonProperty("firstAirDate") String firstAirDate,
            @JsonProperty("origin_country") List<String> originCountry,
            @JsonProperty("genre_ids") List<Integer> genreIds,
            @JsonProperty("original_language") String originalLanguage,
            @JsonProperty("vote_count") int voteCount,
            @JsonProperty("name") String name,
            @JsonProperty("original_name") String originalName) {
        this.posterPath = posterPath;
        this.popularity = popularity;
        this.id = id;
        this.backdropPath = backdropPath;
        this.voteAverage = voteAverage;
        this.overview = overview;
        this.firstAirDate = firstAirDate;
        this.originCountry = originCountry;
        this.genreIds = genreIds;
        this.originalLanguage = originalLanguage;
        this.voteCount = voteCount;
        this.name = name;
        this.originalName = originalName;
    }

    public Optional<String> getPosterPath() {
        return Optional.ofNullable(posterPath);
    }

    public Optional<String> getBackdropPath() {
        return Optional.ofNullable(backdropPath);
    }
}
