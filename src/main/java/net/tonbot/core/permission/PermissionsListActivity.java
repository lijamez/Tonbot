package net.tonbot.core.permission;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IPrivateChannel;

class PermissionsListActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route(ImmutableList.of("permissions", "list"))
			.description("Shows permissions for this server.")
			.build();

	private final PermissionManager permissionManager;
	private final IDiscordClient discordClient;
	private final BotUtils botUtils;

	@Inject
	public PermissionsListActivity(
			PermissionManager permissionManager,
			IDiscordClient discordClient,
			BotUtils botUtils) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		IGuild guild = event.getGuild();

		StringBuilder sb = new StringBuilder();

		permissionManager.getRulesForGuild(guild).stream()
				.forEach(rule -> {
					sb.append(rule).append("\n");
				});

		IPrivateChannel pmChannel = discordClient.getOrCreatePMChannel(event.getAuthor());

		String result = sb.toString();
		System.out.println(result);
		botUtils.sendMessage(pmChannel, result);

	}

}
