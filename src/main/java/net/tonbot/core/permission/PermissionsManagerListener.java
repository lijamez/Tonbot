package net.tonbot.core.permission;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;

public class PermissionsManagerListener {

	private final PermissionManager permissionManager;

	@Inject
	public PermissionsManagerListener(PermissionManager permissionManager) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
	}

	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent event) {
		System.out.println("onGuildCreateEvent fired!");

		permissionManager.initializeForGuild(event.getGuild());
	}
}
