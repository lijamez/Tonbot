package com.tonberry.tonbot.common;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface MessageReceivedAction {

    /**
     * Gets the route. Must not be empty.
     * @return The route.
     */
    List<String> getRoute();

    void enact(MessageReceivedEvent event, String args);
}
