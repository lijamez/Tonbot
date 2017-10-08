package net.tonbot.core.permission;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotBusinessException;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

class DeleteRuleActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("permissions delete")
			.parameters(ImmutableList.of("index"))
			.description("Deletes a rule for this server.")
			.build();

	private final PermissionManager permissionManager;
	private final BotUtils botUtils;

	@Inject
	public DeleteRuleActivity(
			PermissionManager permissionManager,
			BotUtils botUtils) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		IGuild guild = event.getGuild();

		try {
			int index = Integer.parseInt(args) - 1;
			permissionManager.remove(guild, index);
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			throw new TonbotBusinessException("The index is invalid.", e);
		}

		botUtils.sendMessage(event.getChannel(), "Rule was successfully removed.");
	}

}
