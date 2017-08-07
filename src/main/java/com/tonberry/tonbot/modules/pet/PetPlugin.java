package com.tonberry.tonbot.modules.pet;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class PetPlugin implements TonbotPlugin {

    private PetModule module;

    @Override
    public void initialize(TonbotPluginArgs args) {
        this.module = new PetModule(args.getPrefix());
    }

    @Override
    public PluginResources build() {
        return Guice.createInjector(module)
                .getInstance(PluginResources.class);
    }
}
