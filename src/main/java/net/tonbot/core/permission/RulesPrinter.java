package net.tonbot.core.permission;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

class RulesPrinter {

	private static final Logger LOG = LoggerFactory.getLogger(RulesPrinter.class);

	private final PermissionManager permissionManager;
	private final IDiscordClient discordClient;

	@Inject
	public RulesPrinter(PermissionManager permissionManager, IDiscordClient discordClient) {
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
	}

	/**
	 * Prettily prints the rules of a given guild.
	 * 
	 * @param guild
	 *            {@link IGuild}. Non-null.
	 * @return A pretty string describing the guild's rules.
	 */
	public String getPrettyRulesOf(IGuild guild) {
		Preconditions.checkNotNull(guild, "guild must be non-null.");

		List<Rule> rules = permissionManager.getRulesForGuild(guild);

		StringBuilder sb = new StringBuilder();

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

		sb.append("\nRules are evaluated from top to bottom. If none of the above rules match, then the user **");
		if (permissionManager.getDefaultAllowForGuild(guild)) {
			sb.append("WILL");
		} else {
			sb.append("WILL NOT");
		}
		sb.append("** be able to use the command.\n");
		sb.append("The server owner and users in an Administrator role can always use any commmand.");

		String result = sb.toString();

		return result;
	}

	private String renderRoleRule(RoleRule roleRule) {
		StringBuffer sb = new StringBuffer();

		String routeStr = roleRule.getPathExp().toString();

		sb.append("``").append(StringUtils.isEmpty(routeStr) ? "<empty>" : routeStr).append("`` **");
		if (roleRule.isAllow()) {
			sb.append("CAN");
		} else {
			sb.append("CAN NOT");
		}

		sb.append("** be used by ``");
		IRole role = discordClient.getRoleByID(roleRule.getRoleId());
		sb.append(role != null ? role.getName() : "<deleted role>").append("``");

		return sb.toString();
	}
}
