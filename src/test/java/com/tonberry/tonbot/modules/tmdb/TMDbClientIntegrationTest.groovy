package com.tonberry.tonbot.modules.tmdb

import com.google.inject.Guice
import com.tonberry.tonbot.IntegrationTestModule
import spock.lang.Specification

class TMDbClientIntegrationTest extends Specification {

    TMDbClient client

    def setup() {
        String tmdbApiKey = System.getProperty("tmdbApiKey");
        this.client = Guice.createInjector(new IntegrationTestModule(), new TMDbModule(tmdbApiKey))
            .getInstance(TMDbClient.class);
    }

    def "search for movies"() {
        when:
        MovieSearchResult searchResult = this.client.searchMovies("kubo")

        then:
        searchResult != null
        !searchResult.getResults().isEmpty()
    }

    def "get a particular movie"() {
        when:
        Movie movie = this.client.getMovie(24428)

        then:
        movie != null
        movie.getTitle() == "The Avengers"
    }
}
