package com.tonberry.tonbot.modules.coinflip;

import com.tonberry.tonbot.BotUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

class CoinFlipper {

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContent().startsWith("t! flip a coin")) {
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
