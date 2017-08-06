package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;
import com.tonberry.tonbot.common.TonbotPluginModule;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import sx.blah.discord.api.IDiscordClient;

public class TMDbModule extends TonbotPluginModule {

    private final String apiKey;

    public TMDbModule(String prefix, IDiscordClient discordClient) {
        super(prefix, discordClient);

        String tmdbApiKey = System.getProperty("tmdbApiKey");
        Preconditions.checkNotNull(tmdbApiKey, "tmdbApiKey system property must be set.");

        this.apiKey = tmdbApiKey;
    }

    public void configure() {
        super.configure();

        bind(String.class).annotatedWith(TMDbApiKey.class).toInstance(apiKey);

        bind(Plugin.class).toProvider(TMDbModule.PluginProvider.class);
        expose(Plugin.class);
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
                    .usageDescription("``" + prefix + " movie <search term>``  Gets information about a movie.\n"
                        + "``" + prefix + " tv <search term>``  Gets information about a TV show.")
                    .eventListeners(ImmutableSet.of(tmdbEventListener))
                    .build();
        }
    }
}
