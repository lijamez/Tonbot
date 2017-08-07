package com.tonberry.tonbot.modules.decisionmaker;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.EventDispatcher;
import com.tonberry.tonbot.common.Prefix;
import com.tonberry.tonbot.modules.time.TimeAction;

class DecisionMakerEventListener extends EventDispatcher {

    @Inject
    public DecisionMakerEventListener(
            @Prefix String prefix,
            CoinFlipAction coinFlipAction,
            NumberPickerAction numberPickerAction) {
        super(prefix, ImmutableSet.of(coinFlipAction, numberPickerAction));
    }
}
