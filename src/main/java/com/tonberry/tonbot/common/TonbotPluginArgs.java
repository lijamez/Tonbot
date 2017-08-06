package com.tonberry.tonbot.common;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import sx.blah.discord.api.IDiscordClient;

/**
 * A standard set of data passed to every Tonbot Plugin.
 */
@Data
@Builder
public class TonbotPluginArgs {

    private final String prefix;
    private final IDiscordClient discordClient;

    private TonbotPluginArgs(String prefix, IDiscordClient discordClient) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
    }
}
