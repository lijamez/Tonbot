package com.tonberry.tonbot.modules.helloworld;

import com.tonberry.tonbot.BotUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

class HelloWorldEventListener {

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContent().startsWith("t! hello")) {
            String response = String.format("Sup, %s?", event.getAuthor().getDisplayName(event.getGuild()));
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
