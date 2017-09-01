package net.tonbot.core.permission;

import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import net.tonbot.common.Activity;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.common.TonbotPluginArgs;

public class PermissionPlugin extends TonbotPlugin {

	private final Injector injector;

	public PermissionPlugin(
			TonbotPluginArgs pluginArgs) {
		super(pluginArgs);
		this.injector = Guice.createInjector(
				new PermissionControlModule(
						pluginArgs.getBotUtils(),
						pluginArgs.getDiscordClient()));
	}

	@Override
	public String getActionDescription() {
		return "Manage Permissions";
	}

	@Override
	public String getFriendlyName() {
		return "Permission Manager";
	}

	@Override
	public Set<Activity> getActivities() {
		return injector.getInstance(Key.get(new TypeLiteral<Set<Activity>>() {
		}));
	}

	@Override
	public Set<Object> getRawEventListeners() {
		return injector.getInstance(Key.get(new TypeLiteral<Set<Object>>() {
		}));
	}

	public PermissionManager getPermissionManager() {
		return injector.getInstance(PermissionManager.class);
	}
}
