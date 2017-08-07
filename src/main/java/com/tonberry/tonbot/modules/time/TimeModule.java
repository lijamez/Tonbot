package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.Prefix;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

class TimeModule extends AbstractModule {

    private final String prefix;
    private final String wolframAlphaAppId;

    public TimeModule(String prefix, String wolframAlphaAppId) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.wolframAlphaAppId = Preconditions.checkNotNull(wolframAlphaAppId, "wolframAlphaAppId must be non-null.");
    }

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
        bind(String.class).annotatedWith(WolframAlphaAppId.class).toInstance(wolframAlphaAppId);
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
    PluginResources plugin(TimeEventListener timeEventListener) {
        return PluginResources.builder()
                .name("Time")
                .shortSummary("Tell Time")
                .usageDescription("``" + prefix + " time <query>``    Anything about time. Conversions, current time, etc.")
                .eventListeners(ImmutableSet.of(timeEventListener))
                .build();
    }
}
