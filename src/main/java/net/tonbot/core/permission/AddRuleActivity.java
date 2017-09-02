package net.tonbot.core.permission;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotBusinessException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.MessageTokenizer.RoleMentionToken;
import sx.blah.discord.util.MessageTokenizer.Token;

class AddRuleActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route(ImmutableList.of("permissions", "add"))
			.parameters(ImmutableList.of("index", "role", "allow/deny", "command"))
			.description("Add a rule for this server.")
			.build();

	private final PermissionManager permissionManager;
	private final IDiscordClient discordClient;
	private final BotUtils botUtils;

	@Inject
	public AddRuleActivity(
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

		if (StringUtils.isEmpty(args)) {
			throw new TonbotBusinessException("No arguments present.");
		}

		MessageTokenizer tokenizer = new MessageTokenizer(discordClient, args);

		int index;
		if (tokenizer.hasNextWord()) {
			Token indexToken = tokenizer.nextWord();
			try {
				index = Integer.parseInt(indexToken.getContent()) - 1;
				if (index < 0) {
					throw new TonbotBusinessException("The index argument is not valid.");
				}
			} catch (IllegalArgumentException e) {
				throw new TonbotBusinessException("The index argument isn't an integer.");
			}
		} else {
			throw new TonbotBusinessException("Missing index argument.");
		}

		// FIXME: This doesn't work if the role is not mentionable.
		IRole role;
		if (tokenizer.hasNextMention()) {
			Token maybeRoleToken = tokenizer.nextMention();
			if (!(maybeRoleToken instanceof RoleMentionToken)) {
				throw new TonbotBusinessException("Second argument must be a role mention.");
			}
			role = ((RoleMentionToken) maybeRoleToken).getMentionObject();
		} else {
			throw new TonbotBusinessException("Missing role argument.");
		}

		boolean allow;
		if (tokenizer.hasNextWord()) {
			Token allowOrDenyToken = tokenizer.nextWord();

			if (StringUtils.equalsIgnoreCase(allowOrDenyToken.getContent(), "allow")) {
				allow = true;
			} else if (StringUtils.equalsIgnoreCase(allowOrDenyToken.getContent(), "deny")) {
				allow = false;
			} else {
				throw new TonbotBusinessException("The allow/deny argument must be the values 'allow' or 'deny'.");
			}
		} else {
			throw new TonbotBusinessException("Missing allow/deny argument.");
		}

		List<String> route;
		if (tokenizer.hasNext()) {
			String command = tokenizer.getRemainingContent();
			route = Arrays.asList(StringUtils.split(command, " "));
		} else {
			throw new TonbotBusinessException("Command must not be empty.");
		}

		// Finally, we can create the rule.
		Rule rule = new RoleRule(route, guild, role, allow);
		try {
			permissionManager.add(index, rule);
		} catch (IndexOutOfBoundsException e) {
			throw new TonbotBusinessException("Index was not valid.");
		}

		botUtils.sendMessage(event.getChannel(), "Rule was added successfully.");
	}

}
