package com.tonberry.tonbot.modules.coinflip;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.Prefix;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

class CoinFlipper {

    private final String prefix;

    @Inject
    public CoinFlipper(@Prefix String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContent().startsWith(prefix + " flip a coin")) {
            String result;
            if (Math.random() >= 0.5) {
                result = "Heads";
            } else {
                result = "Tails";
            }

            BotUtils.sendMessage(event.getChannel(), result);
        }
    }
}
