package com.tonberry.tonbot.modules.tmdb;

import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.Prefix;

class TMDbModule extends AbstractModule {

    private final String prefix;
    private final String tmdbApiKey;

    public TMDbModule(String prefix, String tmdbApiKey) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.tmdbApiKey = Preconditions.checkNotNull(tmdbApiKey, "tmdbApiKey must be non-null.");
    }

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
        bind(String.class).annotatedWith(TMDbApiKey.class).toInstance(tmdbApiKey);
    }

    @Provides
    @Singleton
    HttpClient httpClient() {
        return HttpClients.createDefault();
    }

    @Provides
    @Singleton
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
    }

    @Provides
    @Singleton
    Set<Activity> activities(TvShowActivity tvShowActivity, MovieActivity movieActivity) {
        return ImmutableSet.of(tvShowActivity, movieActivity);
    }
}
