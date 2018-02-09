package net.tonbot.core.permission;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.ActivityUsageException;
import net.tonbot.common.BotUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.MessageTokenizer.RoleMentionToken;
import sx.blah.discord.util.MessageTokenizer.Token;

class AddRuleActivity implements Activity {

	private static final Pattern EVERYONE_PATTERN = Pattern.compile("@everyone");
	
	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("permissions add")
			.parameters(ImmutableList.of("index", "role", "allow/deny", "route path expression"))
			.description("Add a rule for this server.")
			.build();

	private final PermissionManager permissionManager;
	private final RulesPrinter rulesPrinter;
	private final IDiscordClient discordClient;
	private final BotUtils botUtils;

	@Inject
	public AddRuleActivity(
			PermissionManager permissionManager,
			RulesPrinter rulesPrinter,
			IDiscordClient discordClient,
			BotUtils botUtils) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.rulesPrinter = Preconditions.checkNotNull(rulesPrinter, "rulesPrinter must be non-null.");
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
			throw new ActivityUsageException("No arguments present.");
		}

		MessageTokenizer tokenizer = new MessageTokenizer(discordClient, args);

		int index;
		if (tokenizer.hasNextWord()) {
			Token indexToken = tokenizer.nextWord();
			try {
				index = Integer.parseInt(indexToken.getContent()) - 1;
				if (index < 0) {
					throw new ActivityUsageException("The index argument is not valid.");
				}
			} catch (IllegalArgumentException e) {
				throw new ActivityUsageException("The index argument isn't an integer.");
			}
		} else {
			throw new ActivityUsageException("Missing index argument.");
		}

		// FIXME: This doesn't work if the role is not mentionable.
		IRole role;
		if (tokenizer.hasNextMention()) {
			Token maybeRoleToken = tokenizer.nextMention();
			if (!(maybeRoleToken instanceof RoleMentionToken)) {
				throw new ActivityUsageException("Second argument must be a role mention.");
			}
			role = ((RoleMentionToken) maybeRoleToken).getMentionObject();
		} else if (tokenizer.hasNextWord() && tokenizer.hasNextRegex(EVERYONE_PATTERN)) {
			tokenizer.nextRegex(EVERYONE_PATTERN);
			role = guild.getEveryoneRole();
		} else {
			throw new ActivityUsageException("Missing role argument.");
		}

		boolean allow;
		if (tokenizer.hasNextWord()) {
			Token allowOrDenyToken = tokenizer.nextWord();

			if (StringUtils.equalsIgnoreCase(allowOrDenyToken.getContent(), "allow")) {
				allow = true;
			} else if (StringUtils.equalsIgnoreCase(allowOrDenyToken.getContent(), "deny")) {
				allow = false;
			} else {
				throw new ActivityUsageException("The allow/deny argument must be the values 'allow' or 'deny'.");
			}
		} else {
			throw new ActivityUsageException("Missing allow/deny argument.");
		}

		PathExpression routePathExp;
		if (tokenizer.hasNext()) {
			try {
				routePathExp = new PathExpression(tokenizer.getRemainingContent());
			} catch (MalformedPathExpressionException e) {
				throw new ActivityUsageException(e.getMessage(), e);
			}

		} else {
			throw new ActivityUsageException("Command must not be empty.");
		}

		// Finally, we can create the rule.
		Rule rule = new RoleRule(routePathExp, guild.getLongID(), role.getLongID(), allow);
		try {
			permissionManager.add(index, rule);
		} catch (IndexOutOfBoundsException e) {
			throw new ActivityUsageException("Index was not valid.");
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Rule was successfully added.\n\n");
		sb.append(rulesPrinter.getPrettyRulesOf(guild));
		
		botUtils.sendMessage(event.getChannel(), sb.toString());
	}

}
