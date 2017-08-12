package com.tonberry.tonbot.modules.decisionmaker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.PeriodicTask;
import com.tonberry.tonbot.common.Prefix;

import java.time.Period;
import java.util.Set;

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
    Set<Activity> activities(CoinFlipActivity coinFlip, NumberPickerActivity numberPicker) {
        return ImmutableSet.of(coinFlip, numberPicker);
    }
}
