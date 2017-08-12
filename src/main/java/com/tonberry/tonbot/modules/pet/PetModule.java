package com.tonberry.tonbot.modules.pet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.tonberry.tonbot.common.Prefix;

class PetModule extends AbstractModule {

    private final String prefix;

    public PetModule(String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
    }
}
