package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TMDbClient {

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w500";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    @Inject
    public TMDbClient(HttpClient httpClient, ObjectMapper objectMapper, @TMDbApiKey String apiKey) {
        this.httpClient = Preconditions.checkNotNull(httpClient, "httpClient must be non-null.");
        this.objectMapper = Preconditions.checkNotNull(objectMapper, "objectMapper must be non-null.");
        this.apiKey = Preconditions.checkNotNull(apiKey, "apiKey must be non-null.");
    }

    public MovieSearchResult searchMovies(String query) {
        Preconditions.checkNotNull(query, "query must be non-null.");

        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("api.themoviedb.org")
                    .setPath("/3/search/movie")
                    .setParameter("api_key", apiKey)
                    .setParameter("query", query)
                    .build();

            return get(uri, MovieSearchResult.class);

        } catch (URISyntaxException e) {
            throw new TMDbClientException("Couldn't search for movies.", e);
        }
    }

    public Movie getMovie(int movieId) {

        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("api.themoviedb.org")
                    .setPath("/3/movie/" + movieId)
                    .setParameter("api_key", apiKey)
                    .build();

            return get(uri, Movie.class);

        } catch (URISyntaxException e) {
            throw new TMDbClientException("Couldn't get the movie.", e);
        }
    }

    public TvShowSearchResult searchTvShows(String query) {
        Preconditions.checkNotNull(query, "query must be non-null.");

        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("api.themoviedb.org")
                    .setPath("/3/search/tv")
                    .setParameter("api_key", apiKey)
                    .setParameter("query", query)
                    .build();

            return get(uri, TvShowSearchResult.class);

        } catch (URISyntaxException e) {
            throw new TMDbClientException("Couldn't search for TV shows.", e);
        }
    }

    public TvShow getTvShow(int tvShowId) {

        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("api.themoviedb.org")
                    .setPath("/3/tv/" + tvShowId)
                    .setParameter("api_key", apiKey)
                    .build();

            return get(uri, TvShow.class);

        } catch (URISyntaxException e) {
            throw new TMDbClientException("Couldn't search for TV shows.", e);
        }
    }

    private <T> T get(URI uri, Class<T> clazz) {
        try {
            HttpGet httpGet = new HttpGet(uri);

            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new TMDbClientException("TMDb service returned a " + response.getStatusLine().getStatusCode() + " status code.");
            }

            T searchResult = objectMapper.readValue(response.getEntity().getContent(), clazz);

            return searchResult;
        } catch (IOException e) {
            throw new TMDbClientException("Failed to make TMDb call.", e);
        }
    }

    public String getImageUrl(String relativePath) {
        return IMAGE_BASE_URL + relativePath;
    }
}
