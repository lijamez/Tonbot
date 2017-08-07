package com.tonberry.tonbot.modules.decisionmaker;

import com.google.common.collect.ImmutableList;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.MessageReceivedAction;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

class CoinFlipAction implements MessageReceivedAction {

    private static final List<String> ROUTE = ImmutableList.of("coinflip");

    @Override
    public List<String> getRoute() {
        return ROUTE;
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
