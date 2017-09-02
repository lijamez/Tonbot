package net.tonbot.core.permission;

import java.util.Collection;
import java.util.List;

import net.tonbot.common.Activity;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public interface PermissionManager {

	/**
	 * Adds rules.
	 * 
	 * @param inputRules
	 *            The rules to be added. No elements should be null.
	 */
	void add(Rule... inputRules);

	/**
	 * Adds rules.
	 * 
	 * @param inputRules
	 *            The rules to be added. Non-null.
	 */
	void addAll(Collection<Rule> inputRules);

	/**
	 * Gets the rules for the specified guild.
	 * 
	 * @param guild
	 *            {@link IGuild}. Non-null.
	 * @return The rules for the guild. Non-null.
	 */
	List<Rule> getRulesForGuild(IGuild guild);

	/**
	 * Determines whether if an activity should be allowed or not if none of the
	 * rules matched.
	 * 
	 * @param guild
	 *            {@link IGuild}. Non-null.
	 * @return The default allowability.
	 */
	boolean getDefaultAllowForGuild(IGuild guild);

	/**
	 * Initializes the rules for the guild.
	 * 
	 * @param guild
	 *            {@link IGuild}. Non-null.
	 */
	void initializeForGuild(IGuild guild);

	/**
	 * Checks whether if the user is able to access the activity. Guild owners will
	 * always be able to access any activity.
	 * 
	 * @param activity
	 *            {@link Activity} to access. Non-null.
	 * @param user
	 *            {@link IUser} who is trying to access the activity. Non-null.
	 * @param guild
	 *            The {@link IGuild} where the user is trying to access the
	 *            activity. Non-null.
	 * @return True if the user is able to access the activity. False otherwise.
	 */
	boolean checkAccessibility(Activity activity, IUser user, IGuild guild);

	/**
	 * Adds a public activity.
	 * 
	 * @param publicActivity
	 *            The public {@link Activity}. Non-null.
	 */
	void addPublicActivity(Activity publicActivity);
}
