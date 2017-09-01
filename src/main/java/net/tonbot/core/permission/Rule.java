package net.tonbot.core.permission;

import java.util.List;

import com.google.common.base.Preconditions;

import lombok.Data;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

@Data
public abstract class Rule {

	private final IGuild guild;
	private final boolean allow;

	public Rule(IGuild guild, boolean allow) {
		this.guild = Preconditions.checkNotNull(guild, "guild must be non-null.");
		this.allow = allow;
	}

	abstract boolean appliesTo(List<String> route, IUser user);
}
