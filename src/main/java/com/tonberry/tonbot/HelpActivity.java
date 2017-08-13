package com.tonberry.tonbot;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.ActivityDescriptor;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.TonbotPlugin;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

class HelpActivity implements Activity {

    private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
            .route(ImmutableList.of("help"))
            .build();

    private final String prefix;
    private final List<TonbotPlugin> plugins;

    public HelpActivity(String prefix, List<TonbotPlugin> plugins) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.plugins = Preconditions.checkNotNull(plugins, "plugins must be non-null.");
    }

    @Override
    public ActivityDescriptor getDescriptor() {
        return ACTIVITY_DESCRIPTOR;
    }

    @Override
    public void enact(MessageReceivedEvent event, String args) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.withDesc("Here's what I can do...");

        plugins.stream()
                .filter(plugin -> !plugin.isHidden())
                .forEach(plugin -> {
                    StringBuffer sb = new StringBuffer();
                    plugin.getActivities().stream()
                            .map(Activity::getDescriptor)
                            .forEach(activity -> {
                                sb.append("``");
                                sb.append(prefix);
                                sb.append(" ");
                                sb.append(StringUtils.join(activity.getRoute(), " "));
                                sb.append(" ");

                                List<String> formattedParams = activity.getParameters().stream()
                                        .map(param -> "<" + param + ">")
                                        .collect(Collectors.toList());

                                sb.append(StringUtils.join(formattedParams, " "));

                                sb.append("``");
                                sb.append("    ");
                                sb.append(activity.getDescription());
                                sb.append("\n");
                            });
                    embedBuilder.appendField(plugin.getActionDescription(), sb.toString(), false);
                });

        BotUtils.sendEmbeddedContent(event.getChannel(), embedBuilder.build());
    }
}
