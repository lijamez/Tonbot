package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Set;

class HelpHandler {

    private final String prefix;
    private final Set<Plugin> plugins;

    @Inject
    public HelpHandler(@Prefix String prefix, Set<Plugin> plugins) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.plugins = Preconditions.checkNotNull(plugins, "plugins must be non-null.");
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContent().startsWith(getTrigger())) {
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

    public String getTrigger() {
        return prefix + " help";
    }
}
