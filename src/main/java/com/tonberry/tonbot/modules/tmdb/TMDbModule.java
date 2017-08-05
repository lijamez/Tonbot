package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.Plugin;
import com.tonberry.tonbot.Prefix;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class TMDbModule extends AbstractModule {

    private final String apiKey;

    public TMDbModule(String apiKey) {
        this.apiKey = Preconditions.checkNotNull(apiKey, "apiKey must be non-null.");
    }

    public void configure() {
        bind(String.class).annotatedWith(TMDbApiKey.class).toInstance(apiKey);

        Multibinder<Plugin> pluginBinder = Multibinder.newSetBinder(binder(), Plugin.class);
        pluginBinder.addBinding().toProvider(TMDbModule.PluginProvider.class);
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

    static class PluginProvider implements Provider<Plugin> {

        private final TMDbEventListener tmdbEventListener;
        private final String prefix;

        @Inject
        public PluginProvider(TMDbEventListener tmdbEventListener, @Prefix String prefix) {
            this.tmdbEventListener = Preconditions.checkNotNull(tmdbEventListener, "tmdbEventListener must be non-null.");
            this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        }

        public Plugin get() {
            return Plugin.builder()
                    .name("Movie Info (powered by The Movie Database)")
                    .usageDescription("``" + prefix + " movie <search term>``  Gets information about a movie.")
                    .eventListeners(ImmutableSet.of(tmdbEventListener))
                    .build();
        }
    }
}
