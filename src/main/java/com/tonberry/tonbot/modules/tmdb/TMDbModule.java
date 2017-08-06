package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

class TMDbModule extends AbstractModule {

    private final String prefix;
    private final String apiKey;

    public TMDbModule(String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");

        String tmdbApiKey = System.getProperty("tmdbApiKey");
        Preconditions.checkNotNull(tmdbApiKey, "tmdbApiKey system property must be set.");

        this.apiKey = tmdbApiKey;
    }

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
        bind(String.class).annotatedWith(TMDbApiKey.class).toInstance(apiKey);
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
    Plugin plugin(TMDbEventListener tmdbEventListener) {
        return Plugin.builder()
                .name("Movie Info")
                .usageDescription("``" + prefix + " movie <search term>``  Gets information about a movie.\n"
                        + "``" + prefix + " tv <search term>``  Gets information about a TV show.")
                .eventListeners(ImmutableSet.of(tmdbEventListener))
                .build();
    }
}
