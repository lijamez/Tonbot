package net.tonbot.core.permission;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOG = LoggerFactory.getLogger(PermissionsListActivity.class);

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

		sb.append("Permissions for **").append(guild.getName()).append("**:\n\n");

		List<Rule> rules = permissionManager.getRulesForGuild(guild);
		for (int i = 0; i < rules.size(); i++) {
			sb.append("[``").append(i + 1).append("``] ");
			Rule rule = rules.get(i);
			if (rule instanceof RoleRule) {
				sb.append(renderRoleRule((RoleRule) rule));
			} else {
				LOG.warn("Unknown rule type " + rule.getClass() + " found.");
				sb.append("Unknown rule.");
			}
			sb.append("\n");
		}

		sb.append("\nIf none of the above rules match, then the user **");
		if (permissionManager.getDefaultAllowForGuild(guild)) {
			sb.append("will");
		} else {
			sb.append("will not");
		}
		sb.append("** be able to use the command.");

		IPrivateChannel pmChannel = discordClient.getOrCreatePMChannel(event.getAuthor());

		String result = sb.toString();
		System.out.println(result);
		botUtils.sendMessage(pmChannel, result);

	}

	private String renderRoleRule(RoleRule roleRule) {
		StringBuffer sb = new StringBuffer();

		String routeStr = StringUtils.join(roleRule.getAppliesToRoute(), " ");
		sb.append("``").append(routeStr).append("`` **");
		if (roleRule.isAllow()) {
			sb.append("can");
		} else {
			sb.append("can not");
		}
		sb.append("** be used by ").append(roleRule.getRole().getName());

		return sb.toString();
	}

}
