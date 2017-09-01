package net.tonbot.core.permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.obj.IGuild;

public class PermissionsManagerListener {

	private static Logger LOG = LoggerFactory.getLogger(PermissionsManagerListener.class);

	private final PermissionManager permissionManager;

	@Inject
	public PermissionsManagerListener(PermissionManager permissionManager) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
	}

	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent event) {
		IGuild guild = event.getGuild();

		LOG.debug("Joined guild '{}'. Initializing permissions...", guild.getName());

		permissionManager.initializeForGuild(guild);
	}
}
