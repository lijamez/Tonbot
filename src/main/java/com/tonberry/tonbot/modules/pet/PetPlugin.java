package com.tonberry.tonbot.modules.pet;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

import java.util.Set;

public class PetPlugin extends TonbotPlugin {

    private Injector injector;

    public PetPlugin(TonbotPluginArgs args) {
        super(args);
        this.injector = Guice.createInjector(new PetModule(args.getPrefix()));
    }

    @Override
    public String getFriendlyName() {
        return "Pet";
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public String getActionDescription() {
        return "Emote when Praised";
    }

    @Override
    public Set<Object> getRawEventListeners() {
        PetEventListener listener = this.injector.getInstance(PetEventListener.class);
        return ImmutableSet.of(listener);
    }
}
