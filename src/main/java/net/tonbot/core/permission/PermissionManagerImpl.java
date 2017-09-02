package net.tonbot.core.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

class PermissionManagerImpl implements PermissionManager {

	private final Map<Long, GuildConfiguration> guildConfigs;

	private final List<Activity> publicActivities;
	private final List<Activity> restrictedActivities;

	private final ReadWriteLock lock;

	@Inject
	public PermissionManagerImpl(
			@PublicActivities List<Activity> publicActivities,
			@RestrictedActivities List<Activity> restrictedActivities) {
		this.guildConfigs = new HashMap<>();
		// Activities that should always be accessible to everyone.
		Preconditions.checkNotNull(publicActivities, "publicActivities must be non-null.");
		this.publicActivities = new ArrayList<>(publicActivities);

		// Activities that should should not be accessible to everyone by default.
		Preconditions.checkNotNull(restrictedActivities, "restrictedActivities must be non-null.");
		this.restrictedActivities = new ArrayList<>(restrictedActivities);
		this.lock = new ReentrantReadWriteLock();
	}

	@Override
	public void add(Rule... inputRules) {
		Preconditions.checkNotNull(inputRules, "inputRules must be non-null.");
		if (inputRules.length == 0) {
			return;
		}

		lock.writeLock().lock();
		try {
			addAllInternal(Arrays.asList(inputRules));
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(Collection<Rule> inputRules) {
		Preconditions.checkNotNull(inputRules, "inputRules must be non-null.");
		if (inputRules.isEmpty()) {
			return;
		}

		lock.writeLock().lock();
		try {
			addAllInternal(inputRules);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void addAllInternal(Collection<Rule> inputRules) {
		for (Rule rule : inputRules) {
			long guildId = rule.getGuild().getLongID();
			GuildConfiguration guildConfig = guildConfigs.computeIfAbsent(
					guildId,
					k -> new GuildConfiguration(new ArrayList<>(), true));
			guildConfig.getRules().add(rule);
		}
	}

	@Override
	public List<Rule> getRulesForGuild(IGuild guild) {
		lock.readLock().lock();
		try {
			GuildConfiguration guildConfig = guildConfigs.get(guild.getLongID());
			if (guildConfig != null) {
				return ImmutableList.copyOf(guildConfig.getRules());
			} else {
				return ImmutableList.of();
			}

		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean getDefaultAllowForGuild(IGuild guild) {
		lock.readLock().lock();
		try {
			GuildConfiguration guildConfig = guildConfigs.get(guild.getLongID());
			return guildConfig.isDefaultAllow();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void initializeForGuild(IGuild guild) {
		Preconditions.checkNotNull(guild, "guild must be non-null.");

		List<Rule> rules = new ArrayList<>();

		for (Activity publicActivity : publicActivities) {
			List<String> route = publicActivity.getDescriptor().getRoute();
			Rule rule = new RoleRule(route, guild, guild.getEveryoneRole(), true);
			rules.add(rule);
		}

		for (Activity restrictedActivity : restrictedActivities) {
			List<String> route = restrictedActivity.getDescriptor().getRoute();
			Rule rule = new RoleRule(route, guild, guild.getEveryoneRole(), false);
			rules.add(rule);
		}

		this.addAllInternal(rules);
	}

	@Override
	public boolean checkAccessibility(Activity activity, IUser user, IGuild guild) {
		Preconditions.checkNotNull(activity, "activity must be non-null.");
		Preconditions.checkNotNull(user, "user must be non-null.");
		Preconditions.checkNotNull(guild, "guild must be non-null.");

		if (user.getLongID() == guild.getOwnerLongID()) {
			return true;
		}

		List<String> route = activity.getDescriptor().getRoute();

		lock.readLock().lock();
		try {
			GuildConfiguration guildConfig = guildConfigs.get(guild.getLongID());
			if (guildConfig == null) {
				return false;
			}

			Rule bestRule = guildConfig.getRules().stream()
					.filter(rule -> rule.appliesTo(route, user))
					.findFirst()
					.orElse(null);

			if (bestRule != null) {
				return bestRule.isAllow();
			} else {
				return guildConfig.isDefaultAllow();
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void addPublicActivity(Activity publicActivity) {
		Preconditions.checkNotNull(publicActivity, "publicActivity must be non-null.");

		lock.writeLock().lock();
		try {
			this.publicActivities.add(publicActivity);
		} finally {
			lock.writeLock().unlock();
		}

	}
}
