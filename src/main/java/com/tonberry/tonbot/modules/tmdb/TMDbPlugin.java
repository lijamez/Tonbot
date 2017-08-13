package com.tonberry.tonbot.modules.tmdb;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class TMDbPlugin extends TonbotPlugin {

    private Injector injector;

    public TMDbPlugin(TonbotPluginArgs args) {
        super(args);

        File configFile = args.getConfigFile().orElse(null);
        Preconditions.checkNotNull(configFile, "configFile must be non-null.");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Config config = objectMapper.readValue(configFile, Config.class);
            this.injector = Guice.createInjector(new TMDbModule(args.getPrefix(), config.getTmdbApiKey()));
        } catch (IOException e) {
            throw new RuntimeException("Could not read configuration file.", e);
        }

    }

    @Override
    public Set<Activity> getActivities() {
        return injector.getInstance(Key.get(new TypeLiteral<Set<Activity>>() {}));
    }

    @Override
    public String getFriendlyName() {
        return "Movie Info";
    }

    @Override
    public String getActionDescription() {
        return "Tell You About Movies and TV Shows";
    }
}
