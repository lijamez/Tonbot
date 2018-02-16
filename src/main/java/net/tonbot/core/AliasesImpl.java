package net.tonbot.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import lombok.Getter;
import net.tonbot.common.Activity;
import net.tonbot.common.Route;

class AliasesImpl implements Aliases {

	private final Map<String, String> aliasToCanonicalRouteMap;

	private Map<Route, Activity> aliasToActivityMap;
	private Map<Activity, List<Route>> activityAliases;

	@Getter
	private Set<Activity> knownActivities;

	@Inject
	public AliasesImpl(Map<String, String> aliasToCanonicalRouteMap, Set<Activity> activities) {
		this.aliasToCanonicalRouteMap = Preconditions.checkNotNull(aliasToCanonicalRouteMap,
				"aliasToCanonicalRouteMap must be non-null.");
		Preconditions.checkNotNull(activities, "activities must be non-null.");

		this.knownActivities = activities;
		reindex();
	}

	/**
	 * Updates the alias -> activity and activity -> aliases mappings with the new
	 * collection of activities.
	 */
	private void reindex() {
		this.aliasToActivityMap = createAliasToActivityMap(aliasToCanonicalRouteMap, knownActivities);
		this.activityAliases = reverse(this.aliasToActivityMap);
	}

	@Override
	public List<Route> getAliasesOf(Activity activity) {
		Preconditions.checkNotNull(activity, "activity must be non-nuil");

		List<Route> aliases = activityAliases.get(activity);
		if (aliases == null) {
			return ImmutableList.of();
		} else {
			return ImmutableList.copyOf(aliases);
		}
	}

	@Override
	public Optional<Activity> getActivityAliasedBy(Route alias) {
		Preconditions.checkNotNull(alias, "alias must be non-null.");

		Activity activity = aliasToActivityMap.get(alias);
		return Optional.ofNullable(activity);
	}

	private Map<Route, Activity> createAliasToActivityMap(Map<String, String> aliasRouteToCanonicalRouteMap,
			Collection<Activity> activities) {
		Map<Route, Activity> aliasToActivityMap = new HashMap<>();
		for (Entry<String, String> aliasMapping : aliasRouteToCanonicalRouteMap.entrySet()) {
			Route from = Route.from(aliasMapping.getKey());
			Route to = Route.from(aliasMapping.getValue());

			Activity targetActivity = activities.stream()
					.filter(activity -> activity.getDescriptor().getRoute().equals(to)).findFirst().orElse(null);

			if (targetActivity != null) {
				aliasToActivityMap.put(from, targetActivity);
			}
		}

		return aliasToActivityMap;
	}

	private Map<Activity, List<Route>> reverse(Map<Route, Activity> input) {
		Map<Activity, List<Route>> activityAliases = new HashMap<>();

		for (Entry<Route, Activity> entry : input.entrySet()) {
			List<Route> routes = activityAliases.get(entry.getValue());

			if (routes == null) {
				routes = new ArrayList<>();
				activityAliases.put(entry.getValue(), routes);
			}

			routes.add(entry.getKey());
		}

		return activityAliases;
	}
}
