package net.tonbot.core.permission;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
class RoleRule extends Rule {

	private final PathExpression pathExp;
	private final long roleId;

	public RoleRule(List<String> pathExpComponents, long guildId, long roleId, boolean allow) {
		this(new PathExpression(pathExpComponents), guildId, roleId, allow);
	}

	@JsonCreator
	public RoleRule(@JsonProperty("pathExp") PathExpression pathExp, @JsonProperty("guildId") long guildId,
			@JsonProperty("roleId") long roleId, @JsonProperty("allow") boolean allow) {
		super(guildId, allow);

		this.pathExp = Preconditions.checkNotNull(pathExp, "pathExp must be non-null.");
		this.roleId = Preconditions.checkNotNull(roleId, "roleId must be non-null.");
	}

	@Override
	public boolean appliesTo(List<String> route, IUser user) {
		Preconditions.checkNotNull(route, "route must be non-null.");
		Preconditions.checkNotNull(user, "user must be non-null.");

		IDiscordClient client = user.getClient();

		if (!pathExp.matches(route)) {
			return false;
		}

		IGuild guild = client.getGuildByID(this.getGuildId());

		List<IRole> roles = user.getRolesForGuild(guild);

		return roles.stream().map(role -> role.getLongID()).filter(userRoleId -> userRoleId == roleId).findAny()
				.isPresent();
	}
}
