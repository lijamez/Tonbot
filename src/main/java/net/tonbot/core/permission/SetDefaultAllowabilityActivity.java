package net.tonbot.core.permission;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Enactable;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

class SetDefaultAllowabilityActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("permissions setdefault").parameters(ImmutableList.of("allow/deny"))
			.description("Sets whether if commands should be allowed or denied when they don't match a rule.").build();

	private final PermissionManager permissionManager;
	private final BotUtils botUtils;

	@Inject
	public SetDefaultAllowabilityActivity(PermissionManager permissionManager, BotUtils botUtils) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Enactable
	public void enact(MessageReceivedEvent event, SetDefaultAllowabilityRequest request) {
		IGuild guild = event.getGuild();

		boolean defaultAllow = request.getAllowability() == Allowability.ALLOW;

		permissionManager.setDefaultAllowForGuild(guild, defaultAllow);

		botUtils.sendMessage(event.getChannel(),
				"The default allowability has been set to **" + request.getAllowability() + "**.");
	}

}
