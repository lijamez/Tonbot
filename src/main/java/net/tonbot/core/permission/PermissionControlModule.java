package net.tonbot.core.permission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
	private final File permissionsFile;
	private final List<Activity> publicActivities;

	public PermissionControlModule(
			BotUtils botUtils,
			IDiscordClient discordClient,
			File permissionsFile) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.permissionsFile = Preconditions.checkNotNull(permissionsFile, "permissionsFile must be non-null.");
		this.publicActivities = new ArrayList<>();
	}

	@Override
	protected void configure() {
		bind(BotUtils.class).toInstance(botUtils);
		bind(IDiscordClient.class).toInstance(discordClient);
		bind(new TypeLiteral<List<Activity>>() {
		}).annotatedWith(PublicActivities.class).toInstance(publicActivities);
		bind(PermissionManager.class).to(PermissionManagerImpl.class).in(Scopes.SINGLETON);
		bind(File.class).toInstance(permissionsFile);
	}

	@Provides
	@Singleton
	@RestrictedActivities
	List<Activity> restrictedActivities(Set<Activity> permissionControlActivities) {
		List<Activity> activities = new ArrayList<>();
		activities.addAll(permissionControlActivities);

		return activities;
	}

	@Provides
	@Singleton
	Set<Activity> activities(
			PermissionsListActivity permissionsListActivity,
			AddRuleActivity addRuleActivity,
			DeleteRuleActivity deleteRuleActivity,
			SetDefaultAllowabilityActivity setDefaultAllowabilityActivity) {
		return ImmutableSet.of(permissionsListActivity, addRuleActivity, deleteRuleActivity,
				setDefaultAllowabilityActivity);
	}

	@Provides
	@Singleton
	Set<Object> eventListeners(PermissionsManagerListener listener) {
		return ImmutableSet.of(listener);
	}

	@Provides
	@Singleton
	ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		return objectMapper;
	}
}