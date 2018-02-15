package net.tonbot.core.permission;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.ActivityUsageException;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Enactable;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

class DeleteRuleActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("permissions delete")
			.parameters(ImmutableList.of("index"))
			.description("Deletes a rule for this server.")
			.usageDescription(
					"Use the ``permissions list`` command to see all the rules and then pick a rule to delete with ``permissions delete``.")
			.build();

	private final PermissionManager permissionManager;
	private final RulesPrinter rulesPrinter;
	private final BotUtils botUtils;

	@Inject
	public DeleteRuleActivity(
			PermissionManager permissionManager,
			RulesPrinter rulesPrinter,
			BotUtils botUtils) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.rulesPrinter = Preconditions.checkNotNull(rulesPrinter, "rulesPrinter must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Enactable
	public void enact(MessageReceivedEvent event, DeleteRuleRequest request) {
		IGuild guild = event.getGuild();

		try {
			int index = request.getDisplayIndex() - 1;
			permissionManager.remove(guild, index);
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			throw new ActivityUsageException("The index is invalid.", e);
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Rule was successfully removed.\n\n");
		sb.append(rulesPrinter.getPrettyRulesOf(guild));

		botUtils.sendMessage(event.getChannel(), sb.toString());
	}

}
