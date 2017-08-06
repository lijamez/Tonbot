package com.tonberry.tonbot.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Responsible for dispatching actions based on a message event and contents.
 * A set of actions is registered with this dispatcher. Each action has a route.
 * This class will check the message event's contents, tokenize the words, and attempt to find the best action to run.
 * The best action is the one that has the longest token prefix.
 */
public abstract class EventDispatcher {

    private static final String TOKENIZATION_DELIMITER = " ";

    private final String prefix;
    private final Set<MessageReceivedAction> actions;

    @Inject
    public EventDispatcher(@Prefix String prefix, Set<MessageReceivedAction> actions) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");

        Preconditions.checkNotNull(actions, "actions must be non-null.");
        actions.forEach(action -> {
            Preconditions.checkArgument(action.getRoute().size() > 0,
                    "Action " + action + " must have a non-empty route.");
        });

        this.actions = ImmutableSet.copyOf(actions);
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageString = event.getMessage().getContent();

        List<String> tokens = Arrays.asList(messageString.split(TOKENIZATION_DELIMITER));

        if (tokens.size() == 0) {
            return;
        }

        String firstToken = tokens.get(0);
        if (!StringUtils.equals(firstToken, prefix)) {
            return;
        }

        List<String> remainingTokens = tokens.subList(1, tokens.size());

        if (remainingTokens.isEmpty()) {
            // Only the prefix. Maybe display help or something.
            // TODO: But for now, do nothing.

            return;
        }

        MessageReceivedAction bestAction = null;
        for (MessageReceivedAction action : actions) {

            List<String> route = action.getRoute();
            if (isPrefix(route, remainingTokens) && (bestAction == null || bestAction.getRoute().size() < route.size())) {
                bestAction = action;
            }
        }

        if (bestAction == null) {
            return;
        }

        List<String> prefixAndRoute = tokens.subList(0, bestAction.getRoute().size() + 1);
        int prefixAndRouteChars = (prefixAndRoute.size() * TOKENIZATION_DELIMITER.length()) + prefixAndRoute.stream()
                .mapToInt(String::length)
                .sum();
        // The part of the message that doesn't contain the prefix or route.
        String remainingMessage;
        if (prefixAndRouteChars > messageString.length()) {
            remainingMessage = "";
        } else {
            remainingMessage = messageString.substring(prefixAndRouteChars, messageString.length());
        }

        try {
            bestAction.enact(event, remainingMessage);
        } catch (Exception e) {
            BotUtils.sendMessage(event.getChannel(), "Something bad happened. :confounded:");
            throw e;
        }

    }

    /**
     * Checks wither if list2 is prefixed by list1.
     * @param list1 List 1
     * @param list2 List 2
     * @param <T>
     * @return True if list1 is a prefix of list2. False otherwise.
     */
    private <T> boolean isPrefix(List<T> list1, List<T> list2) {
        if (list1.size() > list2.size()) {
            return false;
        }

        Iterator<T> list1It = list1.iterator();
        Iterator<T> list2It = list2.iterator();
        while (list1It.hasNext()) {
            if (!list1It.next().equals(list2It.next())) {
                return false;
            };
        }

        return true;
    }
}
