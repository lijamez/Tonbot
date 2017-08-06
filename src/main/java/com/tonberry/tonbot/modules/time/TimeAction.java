package com.tonberry.tonbot.modules.time;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.MessageReceivedAction;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;

public class TimeAction implements MessageReceivedAction {

    private static final List<String> ROUTE = ImmutableList.of("time");

    private final WolframAlphaClient waClient;

    @Inject
    public TimeAction(WolframAlphaClient waClient) {
        this.waClient = Preconditions.checkNotNull(waClient, "waClient must be non-null.");
    }

    @Override
    public List<String> getRoute() {
        return ROUTE;
    }

    @Override
    public void enact(MessageReceivedEvent event, String args) {

        WolframAlphaQueryResponse response = waClient.query(args);

        QueryResult queryResult = response.getQueryResult();

        if (!queryResult.isSuccess()) {
            BotUtils.sendMessage(event.getChannel(), "I don't know what that means. :worried:");
            return;
        }

        Pod resultPod = queryResult.getPods().stream()
                .filter(pod -> StringUtils.equals(pod.getId(), "Result"))
                .findAny()
                .orElse(null);

        Pod clockImagesPod = queryResult.getPods().stream()
                .filter(pod -> StringUtils.equals(pod.getId(), "ClockImages") || StringUtils.equals(pod.getId(), "ClockImage"))
                .findAny()
                .orElse(null);

        // Use the presence of a clock image to determine if query was time-related.
        if (clockImagesPod == null) {
            BotUtils.sendMessage(event.getChannel(), "I don't think that was related to time. :thinking:");
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (!clockImagesPod.getSubpods().isEmpty()) {
            embedBuilder.withImage(clockImagesPod.getSubpods().get(0).getImage().getSrc());
        }

        embedBuilder.withDescription(resultPod.getSubpods().get(0).getPlaintext());

        embedBuilder.withFooterText("Powered by WolframAlpha");

        BotUtils.sendEmbeddedContent(event.getChannel(), embedBuilder.build());
    }
}
