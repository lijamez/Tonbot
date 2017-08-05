package com.tonberry.tonbot;

import com.google.inject.AbstractModule;

public class IntegrationTestModule extends AbstractModule {

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance("t!");
    }
}
