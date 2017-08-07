package com.tonberry.tonbot.modules.decisionmaker;

import com.google.common.collect.ImmutableList;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.MessageReceivedAction;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

class NumberPickerAction implements MessageReceivedAction {

    private static final List<String> ROUTE = ImmutableList.of("pickanumber");

    @Override
    public List<String> getRoute() {
        return ROUTE;
    }

    @Override
    public void enact(MessageReceivedEvent event, String args) {
        // Let's see if we can parse the number range.
        List<String> tokens = Arrays.asList(args.split(" "));

        List<Integer> ints = tokens.stream()
                .map(token -> {
                    try {
                        return Integer.parseInt(token);
                    } catch (NumberFormatException e){
                        return null;
                    }
                })
                .filter(value -> value != null)
                .collect(Collectors.toList());

        if (ints.size() != 2) {
            BotUtils.sendMessage(event.getChannel(), "You need to provide exactly two integers.");
            return;
        }

        ints = ints.stream()
                .sorted()
                .collect(Collectors.toList());

        int decision = ThreadLocalRandom.current().nextInt(ints.get(0), ints.get(1) + 1);

        BotUtils.sendMessage(event.getChannel(), "I pick... **" + decision + "**");
    }
}
