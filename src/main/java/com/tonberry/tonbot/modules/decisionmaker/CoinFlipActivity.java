package com.tonberry.tonbot.modules.decisionmaker;

import com.google.common.collect.ImmutableList;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.ActivityDescriptor;
import com.tonberry.tonbot.common.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

class CoinFlipActivity implements Activity {

    private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
            .route(ImmutableList.of("coinflip"))
            .description("Flips a coin.")
            .build();

    @Override
    public ActivityDescriptor getDescriptor() {
        return ACTIVITY_DESCRIPTOR;
    }

    @Override
    public void enact(MessageReceivedEvent event, String args) {
        String result;
        if (Math.random() >= 0.5) {
            result = "Heads";
        } else {
            result = "Tails";
        }

        BotUtils.sendMessage(event.getChannel(), result);
    }
}
