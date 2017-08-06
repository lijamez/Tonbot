package com.tonberry.tonbot;

import com.google.inject.AbstractModule;
import com.tonberry.tonbot.common.Prefix;

public class IntegrationTestModule extends AbstractModule {

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance("t!");
    }
}
