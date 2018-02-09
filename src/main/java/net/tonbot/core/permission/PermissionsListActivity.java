package net.tonbot.core.permission;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

class PermissionsListActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("permissions list")
			.description("Shows permissions for this server.")
			.build();

	private final RulesPrinter rulesPrinter;
	private final BotUtils botUtils;

	@Inject
	public PermissionsListActivity(
			RulesPrinter rulesPrinter,
			BotUtils botUtils) {
		this.rulesPrinter = Preconditions.checkNotNull(rulesPrinter, "rulesPrinter must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		IGuild guild = event.getGuild();

		String prettyRules = rulesPrinter.getPrettyRulesOf(guild);
		
		botUtils.sendMessage(event.getChannel(), prettyRules);
		
	}
}
