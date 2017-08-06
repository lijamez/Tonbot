package com.tonberry.tonbot.modules.time;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.EventDispatcher;
import com.tonberry.tonbot.common.Prefix;

class TimeEventListener extends EventDispatcher {

    @Inject
    public TimeEventListener(@Prefix String prefix, TimeAction timeAction) {
        super(prefix, ImmutableSet.of(timeAction));
    }
}
