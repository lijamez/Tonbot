package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Set;

class HelpHandler {

    private final Set<Plugin> plugins;

    public HelpHandler(Set<Plugin> plugins) {
        this.plugins = Preconditions.checkNotNull(plugins, "plugins must be non-null.");
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContent().startsWith("t! help")) {
            StringBuffer sb = new StringBuffer();

            plugins.stream()
                    .filter(plugin -> !plugin.isHidden())
                    .forEach(plugin -> {
                        sb.append("**" + plugin.getName() + "**\n");
                        sb.append(plugin.getUsageDescription());
                        sb.append("\n\n");
                    });

            BotUtils.sendMessage(event.getChannel(), sb.toString());
        }
    }
}
