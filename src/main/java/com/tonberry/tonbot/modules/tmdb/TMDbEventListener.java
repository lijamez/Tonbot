package com.tonberry.tonbot.modules.tmdb;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.EventDispatcher;
import com.tonberry.tonbot.common.Prefix;

class TMDbEventListener extends EventDispatcher {

    @Inject
    public TMDbEventListener(
            @Prefix String prefix,
            MovieAction movieAction,
            TvShowAction tvShowAction) {
        super(prefix, ImmutableSet.of(movieAction, tvShowAction));
    }
}
