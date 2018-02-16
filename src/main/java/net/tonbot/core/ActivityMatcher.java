package net.tonbot.core;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.Route;

class ActivityMatcher {

	private final Set<Activity> activities;
	private final Aliases aliases;

	@Inject
	public ActivityMatcher(Set<Activity> activities, Aliases aliases) {
		Preconditions.checkNotNull(activities, "activities must be non-null.");
		this.activities = ImmutableSet.copyOf(activities);

		this.aliases = Preconditions.checkNotNull(aliases, "aliases must be non-null.");
	}

	/**
	 * Tries to find the most appropriate activity to run.
	 * 
	 * @param tokens
	 * @return
	 */
	public Optional<ActivityMatch> matchActivity(List<String> tokens) {
		Preconditions.checkNotNull(tokens, "tokens must be non-null.");

		// Find the activity to run. We will first match by the main route.
		Route preliminaryRoute = Route.from(tokens);

		// Route alias matching takes precedence of natural route matching.
		ActivityMatch matchedActivity = findBestActivityByRouteAlias(preliminaryRoute);

		if (matchedActivity == null) {
			// Since no activity was matched, we'll fall back matching via the natural
			// routes.
			matchedActivity = findBestActivityByNaturalRoute(preliminaryRoute);
		}

		return Optional.ofNullable(matchedActivity);
	}

	private ActivityMatch findBestActivityByRouteAlias(Route preliminaryRoute) {
		ActivityMatch match = null;

		for (Activity activity : aliases.getKnownActivities()) {
			List<Route> aliasesOfActivity = aliases.getAliasesOf(activity);
			for (Route alias : aliasesOfActivity) {
				if (preliminaryRoute.isPrefixedBy(alias)
						&& (match == null || match.getMatchedRoute().getPath().size() < alias.getPath().size())) {
					match = new ActivityMatch(activity, alias);
				}
			}
		}

		return match;
	}

	private ActivityMatch findBestActivityByNaturalRoute(Route preliminaryRoute) {
		ActivityMatch matchedActivity = null;

		for (Activity activity : activities) {
			ActivityDescriptor descriptor = activity.getDescriptor();

			Route route = descriptor.getRoute();
			if (preliminaryRoute.isPrefixedBy(route) && (matchedActivity == null
					|| matchedActivity.getMatchedRoute().getPath().size() < route.getPath().size())) {
				matchedActivity = new ActivityMatch(activity, route);
			}
		}

		return matchedActivity;
	}
}
