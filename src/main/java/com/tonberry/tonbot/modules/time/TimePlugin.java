package com.tonberry.tonbot.modules.time;

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

public class TimePlugin extends TonbotPlugin {

    private Injector injector;

    public TimePlugin(TonbotPluginArgs args) {
        super(args);

        ObjectMapper objectMapper = new ObjectMapper();

        File configFile = args.getConfigFile().orElse(null);
        Preconditions.checkNotNull(configFile, "configFile must be non-null.");

        try {
            Config config = objectMapper.readValue(configFile, Config.class);
            this.injector = Guice.createInjector(new TimeModule(args.getPrefix(), config.getWolframAlphaAppId()));
        } catch (IOException e) {
            throw new RuntimeException("Could not read configuration file.", e);
        }
    }

    @Override
    public String getFriendlyName() {
        return "Time";
    }

    @Override
    public String getActionDescription() {
        return "Tell Time";
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public Set<Activity> getActivities() {
        return injector.getInstance(Key.get(new TypeLiteral<Set<Activity>>() {}));
    }
}
