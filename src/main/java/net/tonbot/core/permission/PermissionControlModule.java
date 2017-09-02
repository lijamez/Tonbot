package net.tonbot.core.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import net.tonbot.common.Activity;
import net.tonbot.common.BotUtils;
import sx.blah.discord.api.IDiscordClient;

class PermissionControlModule extends AbstractModule {

	private final BotUtils botUtils;
	private final IDiscordClient discordClient;
	private final List<Activity> publicActivities;
	private final List<Activity> restrictedActivities;

	public PermissionControlModule(
			BotUtils botUtils,
			IDiscordClient discordClient) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.publicActivities = new ArrayList<>();
		this.restrictedActivities = new ArrayList<>();
	}

	@Override
	protected void configure() {
		bind(BotUtils.class).toInstance(botUtils);
		bind(IDiscordClient.class).toInstance(discordClient);
		bind(new TypeLiteral<List<Activity>>() {
		}).annotatedWith(PublicActivities.class).toInstance(publicActivities);
		bind(PermissionManager.class).to(PermissionManagerImpl.class).in(Scopes.SINGLETON);
	}

	@Provides
	@Singleton
	@RestrictedActivities
	List<Activity> restrictedActivities(PermissionsListActivity permissionsListActivity) {
		List<Activity> activities = new ArrayList<>();
		activities.addAll(restrictedActivities);
		activities.add(permissionsListActivity);

		return activities;
	}

	@Provides
	@Singleton
	Set<Activity> activities(
			PermissionsListActivity permissionsListActivity,
			AddRuleActivity addRuleActivity) {
		return ImmutableSet.of(permissionsListActivity, addRuleActivity);
	}

	@Provides
	@Singleton
	Set<Object> eventListeners(PermissionsManagerListener listener) {
		return ImmutableSet.of(listener);
	}
}
