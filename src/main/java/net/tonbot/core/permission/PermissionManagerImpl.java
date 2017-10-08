package net.tonbot.core.permission;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.Route;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

class PermissionManagerImpl implements PermissionManager {

	private Map<Long, GuildConfiguration> guildConfigs;

	private final List<Activity> publicActivities;
	private final List<Activity> restrictedActivities;
	private final ObjectMapper objectMapper;
	private final File permissionsFile;

	private final ReadWriteLock lock;

	@Inject
	public PermissionManagerImpl(
			@PublicActivities List<Activity> publicActivities,
			@RestrictedActivities List<Activity> restrictedActivities,
			File permissionsFile,
			ObjectMapper objectMapper) {
		this.guildConfigs = new HashMap<>();
		// Activities that should always be accessible to everyone.
		Preconditions.checkNotNull(publicActivities, "publicActivities must be non-null.");
		this.publicActivities = new ArrayList<>(publicActivities);

		// Activities that should should not be accessible to everyone by default.
		Preconditions.checkNotNull(restrictedActivities, "restrictedActivities must be non-null.");
		this.restrictedActivities = new ArrayList<>(restrictedActivities);

		this.permissionsFile = Preconditions.checkNotNull(permissionsFile, "permissionsFile must be non-null.");
		this.objectMapper = Preconditions.checkNotNull(objectMapper, "objectMapper must be non-null.");

		this.lock = new ReentrantReadWriteLock();

		if (permissionsFile.exists()) {
			load();
		} else {
			save();
		}
	}

	private void load() {
		lock.writeLock().lock();
		try {
			this.guildConfigs = objectMapper.readValue(
					permissionsFile, new TypeReference<Map<Long, GuildConfiguration>>() {
					});
		} catch (IOException e) {
			throw new UncheckedIOException("Unable to read permissions file.", e);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void save() {
		lock.readLock().lock();
		try {
			permissionsFile.createNewFile();
			objectMapper.writeValue(permissionsFile, guildConfigs);
		} catch (IOException e) {
			throw new UncheckedIOException("Unable to save permissions file.", e);
		} finally {
			lock.readLock().unlock();
		}
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

	@Override
	public void add(int index, Rule rule) {
		Preconditions.checkNotNull(rule, "rule must be non-null.");

		lock.writeLock().lock();
		try {
			long guildId = rule.getGuildId();
			GuildConfiguration guildConfig = guildConfigs.computeIfAbsent(
					guildId,
					k -> new GuildConfiguration(new ArrayList<>(), true));
			guildConfig.getRules().add(index, rule);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void addAllInternal(Collection<Rule> inputRules) {
		for (Rule rule : inputRules) {
			long guildId = rule.getGuildId();
			GuildConfiguration guildConfig = guildConfigs.computeIfAbsent(
					guildId,
					k -> new GuildConfiguration(new ArrayList<>(), true));
			guildConfig.getRules().add(rule);
		}
	}

	@Override
	public Rule remove(IGuild guild, int index) {
		lock.writeLock().lock();
		try {
			GuildConfiguration guildConfig = guildConfigs.get(guild.getLongID());
			if (guildConfig == null) {
				throw new IllegalStateException("No guild configuration found.");
			}
			return guildConfig.getRules().remove(index);
		} finally {
			lock.writeLock().unlock();
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
	public void setDefaultAllowForGuild(IGuild guild, boolean defaultAllow) {
		lock.writeLock().lock();
		try {
			GuildConfiguration guildConfig = guildConfigs.get(guild.getLongID());
			if (guildConfig == null) {
				throw new IllegalStateException("No guild configuration found.");
			}
			guildConfig.setDefaultAllow(defaultAllow);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void initializeForGuild(IGuild guild) {
		Preconditions.checkNotNull(guild, "guild must be non-null.");

		boolean isInitialized;
		lock.readLock().lock();
		try {
			isInitialized = guildConfigs.containsKey(guild.getLongID());
		} finally {
			lock.readLock().unlock();
		}

		if (!isInitialized) {
			List<Rule> rules = new ArrayList<>();

			for (Activity publicActivity : publicActivities) {
				Route route = publicActivity.getDescriptor().getRoute();
				Rule rule = new RoleRule(route.getPath(), guild.getLongID(), guild.getEveryoneRole().getLongID(), true);
				rules.add(rule);
			}

			for (Activity restrictedActivity : restrictedActivities) {
				Route route = restrictedActivity.getDescriptor().getRoute();
				Rule rule = new RoleRule(route.getPath(), guild.getLongID(), guild.getEveryoneRole().getLongID(),
						false);
				rules.add(rule);
			}

			lock.writeLock().lock();
			try {
				this.addAllInternal(rules);
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	@Override
	public boolean checkAccessibility(Activity activity, IUser user, IGuild guild) {
		Preconditions.checkNotNull(activity, "activity must be non-null.");
		Preconditions.checkNotNull(user, "user must be non-null.");
		Preconditions.checkNotNull(guild, "guild must be non-null.");

		// The guild owner and the administrators can always access an activity.
		boolean userIsAdmin = user.getLongID() == guild.getOwnerLongID()
				|| user.getRolesForGuild(guild).stream()
						.filter(role -> role.getPermissions().contains(Permissions.ADMINISTRATOR))
						.findAny()
						.isPresent();
		if (userIsAdmin) {
			return true;
		}

		Route route = activity.getDescriptor().getRoute();

		lock.readLock().lock();
		try {
			GuildConfiguration guildConfig = guildConfigs.get(guild.getLongID());
			if (guildConfig == null) {
				return false;
			}

			Rule bestRule = guildConfig.getRules().stream()
					.filter(rule -> rule.appliesTo(route.getPath(), user))
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

	@Override
	public void destroy() {
		save();
	}
}
