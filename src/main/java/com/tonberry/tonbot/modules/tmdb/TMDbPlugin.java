package com.tonberry.tonbot.modules.tmdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

import java.io.File;
import java.io.IOException;

public class TMDbPlugin implements TonbotPlugin {

    private TMDbModule module;

    public void initialize(TonbotPluginArgs args) {
        File configFile = args.getConfigFile().orElse(null);
        Preconditions.checkNotNull(configFile, "configFile must be non-null.");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Config config = objectMapper.readValue(configFile, Config.class);
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
