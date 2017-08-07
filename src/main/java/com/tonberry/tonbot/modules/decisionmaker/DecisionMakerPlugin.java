package com.tonberry.tonbot.modules.decisionmaker;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class DecisionMakerPlugin implements TonbotPlugin {

    private DecisionMakerModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new DecisionMakerModule(args.getPrefix());
    }

    public PluginResources build() {
        return Guice.createInjector(module)
                .getInstance(PluginResources.class);
    }
}
