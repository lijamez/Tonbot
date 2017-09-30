package net.tonbot.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import lombok.Data;
import lombok.NonNull;
import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.core.permission.PermissionManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Responsible for dispatching actions based on a message event and contents. A
 * set of activities is registered with this dispatcher. Each activity has a
 * route. This class will check the message event's contents, tokenize the
 * words, and attempt to find the best activity to run. The best activity is the
 * one that has the longest token prefix.
 */
class EventDispatcher {

	private static final Logger LOG = LoggerFactory.getLogger(EventDispatcher.class);

	private static final String TOKENIZATION_DELIMITER = " ";

	private final BotUtils botUtils;
	private final String prefix;
	private final Set<Activity> activities;
	private final PermissionManager permissionManager;

	public EventDispatcher(
			BotUtils botUtils,
			String prefix,
			Set<Activity> activities,
			PermissionManager permissionManager) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");

		Preconditions.checkNotNull(activities, "activities must be non-null.");
		activities.forEach(activity -> {
			Preconditions.checkArgument(activity.getDescriptor().getRoute().size() > 0,
					"Activity " + activity + " must have a non-empty route.");
		});

		this.activities = ImmutableSet.copyOf(activities);
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
			// Only the prefix. Maybe display help or something?
			// TODO: But for now, do nothing.

			return;
		}

		ActivityMatch matchedActivity = matchActivity(remainingTokens);

		if (matchedActivity == null) {
			return;
		}

		if (!permissionManager.checkAccessibility(matchedActivity.getMatchedActivity(), event.getAuthor(),
				event.getGuild())) {
			LOG.debug("Activity '{}' was denied to user '{}' in guild '{}'",
					matchedActivity.getMatchedActivity().getClass(),
					event.getAuthor(),
					event.getGuild().getName());
			return;
		}

		List<String> prefixAndRoute = tokens.subList(0, matchedActivity.getMatchedRoute().size() + 1);
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
			matchedActivity.getMatchedActivity().enact(event, remainingMessage);
		} catch (TonbotBusinessException e) {
			botUtils.sendMessage(event.getChannel(), e.getMessage());
		} catch (Exception e) {
			botUtils.sendMessage(event.getChannel(), "Something bad happened. :confounded:");
			LOG.error("Uncaught exception received from activity.", e);
		}
	}

	private ActivityMatch matchActivity(List<String> remainingTokens) {
		// Find the activity to run. We will first match by the main route.
		ActivityMatch matchedActivity = null;
		for (Activity activity : activities) {
			ActivityDescriptor descriptor = activity.getDescriptor();

			List<String> route = descriptor.getRoute();
			if (isPrefix(route, remainingTokens)
					&& (matchedActivity == null || matchedActivity.getMatchedRoute().size() < route.size())) {
				matchedActivity = new ActivityMatch(activity, route);
			}
		}

		if (matchedActivity == null) {
			// Since no activity was matched, we'll fall back matching via the route
			// aliases.
			matchedActivity = findBestActivityByRouteAlias(remainingTokens);
		}

		return matchedActivity;
	}

	private ActivityMatch findBestActivityByRouteAlias(List<String> remainingTokens) {
		ActivityMatch match = null;

		for (Activity activity : activities) {
			ActivityDescriptor descriptor = activity.getDescriptor();

			List<List<String>> routeAliases = descriptor.getRouteAliases();
			for (List<String> routeAlias : routeAliases) {
				if (isPrefix(routeAlias, remainingTokens)
						&& (match == null || match.getMatchedRoute().size() < routeAlias.size())) {
					match = new ActivityMatch(activity, routeAlias);
				}
			}
		}

		return match;
	}

	/**
	 * Checks wither if list2 is prefixed by list1.
	 * 
	 * @param list1
	 *            List 1
	 * @param list2
	 *            List 2
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
			}
			;
		}

		return true;
	}

	/**
	 * Object which represents a matched activity. The matchedRoute may be the
	 * canonical route or an alias.
	 */
	@Data
	private static class ActivityMatch {
		@NonNull
		private final Activity matchedActivity;

		@NonNull
		private final List<String> matchedRoute;
	}
}
