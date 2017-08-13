package com.tonberry.tonbot.modules.tmdb;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TvShow {

    private final int id;
    private final String backdropPath;
    private final String name;
    private final int numberOfEpisodes;
    private final int numberOfSeasons;
    private final String overview;
    private final String posterPath;
    private final int voteAverage;
    private final int voteCount;
    private final String firstAirDate;
    private final String lastAirDate;
    private final List<Genre> genres;

    @JsonCreator()
    public TvShow(
            @JsonProperty("id") int id,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("name") String name,
            @JsonProperty("number_of_episodes") int numberOfEpisodes,
            @JsonProperty("number_of_seasons") int numberOfSeasons,
            @JsonProperty("overview") String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("vote_average") int voteAverage,
            @JsonProperty("vote_count") int voteCount,
            @JsonProperty("first_air_date") String firstAirDate,
            @JsonProperty("last_air_date") String lastAirDate,
            @JsonProperty("genres") List<Genre> genres) {
        this.id = id;
        this.backdropPath = backdropPath;
        this.name = name;
        this.numberOfEpisodes = numberOfEpisodes;
        this.numberOfSeasons = numberOfSeasons;
        this.overview = overview;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.firstAirDate = firstAirDate;
        this.lastAirDate = lastAirDate;
        this.genres = genres;
    }

    public Optional<String> getBackdropPath() {
        return Optional.ofNullable(backdropPath);
    }

    public Optional<String> getPosterPath() {
        return Optional.ofNullable(posterPath);
    }

}
