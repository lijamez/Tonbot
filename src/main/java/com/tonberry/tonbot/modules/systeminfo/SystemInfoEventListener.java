package com.tonberry.tonbot.modules.systeminfo;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.EventDispatcher;
import com.tonberry.tonbot.common.Prefix;

class SystemInfoEventListener extends EventDispatcher {

    @Inject
    public SystemInfoEventListener(@Prefix String prefix, SystemInfoAction systemInfoAction) {
        super(prefix, ImmutableSet.of(systemInfoAction));
    }
}
