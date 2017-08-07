package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.PluginResources;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;

class HelpHandler {

    private final String prefix;
    private final List<PluginResources> pluginResources;

    public HelpHandler(String prefix, List<PluginResources> pluginResources) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.pluginResources = Preconditions.checkNotNull(pluginResources, "pluginResources must be non-null.");
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContent().startsWith(getTrigger())) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.withDesc("Here's what I can do...");

            pluginResources.stream()
                    .filter(plugin -> !plugin.isHidden())
                    .forEach(plugin -> {
                        embedBuilder.appendField(plugin.getShortSummary(), plugin.getUsageDescription(), false);
                    });

            BotUtils.sendEmbeddedContent(event.getChannel(), embedBuilder.build());
        }
    }

    public String getTrigger() {
        return prefix + " help";
    }
}
