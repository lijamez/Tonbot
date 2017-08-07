package com.tonberry.tonbot.modules.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

import java.io.File;
import java.io.IOException;

public class TimePlugin implements TonbotPlugin {

    private TimeModule module;

    public void initialize(TonbotPluginArgs args) {
        ObjectMapper objectMapper = new ObjectMapper();

        File configFile = args.getConfigFile().orElse(null);
        Preconditions.checkNotNull(configFile, "configFile must be non-null.");

        try {
            Config config = objectMapper.readValue(configFile, Config.class);
            this.module = new TimeModule(args.getPrefix(), config.getWolframAlphaAppId());
        } catch (IOException e) {
            throw new RuntimeException("Could not read configuration file.", e);
        }
    }

    public PluginResources build() {
        return Guice.createInjector(module)
                .getInstance(PluginResources.class);
    }
}
