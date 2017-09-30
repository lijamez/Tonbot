package net.tonbot.core.permission;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Data;
import sx.blah.discord.handle.obj.IUser;

@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@Data
public abstract class Rule {

	private final long guildId;
	private final boolean allow;

	public Rule(long guildId, boolean allow) {
		this.guildId = guildId;
		this.allow = allow;
	}

	abstract boolean appliesTo(List<String> route, IUser user);
}
