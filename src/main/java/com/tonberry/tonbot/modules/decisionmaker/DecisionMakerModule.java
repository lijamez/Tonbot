package com.tonberry.tonbot.modules.decisionmaker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.Prefix;

class DecisionMakerModule extends AbstractModule {

    private final String prefix;

    public DecisionMakerModule(String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
    }

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
    }

    @Provides
    @Singleton
    PluginResources plugin(DecisionMakerEventListener eventListener) {
        return PluginResources.builder()
                .name("Decision Maker")
                .shortSummary("Make Important Decisions")
                .usageDescription("``" + prefix + " coinflip``    Flips a coin\n"
                    + "``" + prefix + " pickanumber``    Picks a number between two other numbers")
                .eventListeners(ImmutableSet.of(eventListener))
                .build();
    }
}
