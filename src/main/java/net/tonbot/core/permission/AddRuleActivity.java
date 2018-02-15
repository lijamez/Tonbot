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

class AddRuleActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("permissions add")
			.parameters(ImmutableList.of("<index>", "<role>", "<allow/deny>", "<route path expression>"))
			.description("Add a rule for this server.")
			.usageDescription(
					"Adds a rule at the given ``index`` which allows or denies the ``role`` to use command(s) "
							+ "that satisfy a ``route path expression``.\n\n"
							+ "**Arguments**:\n\n"
							+ "``index`` - The index to insert the rule at as shown by the ``permissions list`` command.\n"
							+ "``role`` - A role mention.\n"
							+ "``allow/deny`` - Must be either 'allow' or 'deny'\n"
							+ "``route path expression`` - The command name or some expression to allow or deny for the given role. This route "
							+ "must be the natural route, not an alias. A path expression may contain wildcards ``*`` or ``**``.\n\n"
							+ "**Examples:**\n\n"
							+ "```${absoluteReferencedRoute} 1 @somerole deny permissions **```\n"
							+ "This would deny all users in ``@somerole`` from being able to use commands that *start with* ``permissions``\n\n"
							+ "```${absoluteReferencedRoute} 1 @somerole allow help```\n"
							+ "This would allow all users in ``@somerole`` to use the ``help`` command. "
							+ "Note that this rule says nothing about whether if the role can access other commands that start with ``help``, "
							+ "such as ``help foo bar``.\n")
			.build();

	private final PermissionManager permissionManager;
	private final RulesPrinter rulesPrinter;
	private final BotUtils botUtils;

	@Inject
	public AddRuleActivity(
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
	public void enact(MessageReceivedEvent event, AddRuleRequest request) {
		IGuild guild = event.getGuild();

		PathExpression routePathExp = new PathExpression(request.getPath());

		int actualIndex = request.getDisplayIndex() - 1;
		if (actualIndex < 0) {
			throw new ActivityUsageException("The index argument must be positive.");
		}

		// Finally, we can create the rule.
		Rule rule = new RoleRule(
				routePathExp,
				guild.getLongID(),
				request.getRole().getLongID(),
				request.getAllowability() == Allowability.ALLOW);
		try {
			permissionManager.add(actualIndex, rule);
		} catch (IndexOutOfBoundsException e) {
			throw new ActivityUsageException("Index was not valid.");
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Rule was successfully added.\n\n");
		sb.append(rulesPrinter.getPrettyRulesOf(guild));

		botUtils.sendMessage(event.getChannel(), sb.toString());
	}
}
