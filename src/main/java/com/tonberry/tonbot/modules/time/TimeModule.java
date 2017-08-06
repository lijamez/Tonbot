package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Exposed;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginModule;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import sx.blah.discord.api.IDiscordClient;

public class TimeModule extends TonbotPluginModule {

    private final String wolframAlphaAppId;

    public TimeModule(String prefix, IDiscordClient discordClient) {
        super(prefix, discordClient);

        String wolframAlphaAppId = System.getProperty("wolframAlphaAppId");
        Preconditions.checkNotNull(wolframAlphaAppId, "wolframAlphaAppId system property must be set.");

        this.wolframAlphaAppId = Preconditions.checkNotNull(wolframAlphaAppId, "wolframAlphaAppId must be non-null.");
    }

    public void configure() {
        super.configure();

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
    @Exposed
    Plugin plugin(TimeEventListener timeEventListener) {
        return Plugin.builder()
                .name("Time")
                .usageDescription("``" + this.getPrefix() + " time <query>``  Anything about time. Conversions, current time, etc.")
                .eventListeners(ImmutableSet.of(timeEventListener))
                .build();
    }
}
