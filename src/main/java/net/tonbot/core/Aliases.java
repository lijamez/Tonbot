package net.tonbot.core;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.tonbot.common.Activity;
import net.tonbot.common.Route;

interface Aliases {

	Set<Activity> getKnownActivities();

	/**
	 * Gets an immutable list of aliases for the given activity.
	 * 
	 * @param activity
	 *            {@link Activity}. Non-null.
	 * @return A list of aliases for the given activity. If this class isn't aware
	 *         of this activity, an empty list will be returned.
	 */
	List<Route> getAliasesOf(Activity activity);

	/**
	 * Gets the {@link Activity} that the given alias links to.
	 * 
	 * @param alias
	 *            An alias. Non-null.
	 * @return The {@link Activity} which the given alias links to.
	 */
	Optional<Activity> getActivityAliasedBy(Route alias);
}
