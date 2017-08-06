package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPlugin;

import java.io.IOException;
import java.net.URL;

public class TMDbPlugin implements TonbotPlugin {

    private TMDbModule module;

    public void initialize(TonbotPluginArgs args) {
        URL configFileUrl = args.getConfigFileUrl().orElse(null);
        Preconditions.checkNotNull(configFileUrl, "configFileUrl must be non-null.");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Config config = objectMapper.readValue(configFileUrl, Config.class);
            this.module = new TMDbModule(args.getPrefix(), config.getTmdbApiKey());
        } catch (IOException e) {
            throw new RuntimeException("Could not read configuration file.", e);
        }

    }

    public PluginResources build() {
        return Guice.createInjector(module)
                .getInstance(PluginResources.class);
    }
}
