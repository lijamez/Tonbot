package com.tonberry.tonbot.common;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import sx.blah.discord.api.IDiscordClient;

import java.io.File;
import java.util.Optional;

/**
 * A standard set of data passed to every Tonbot PluginResources.
 */
@Data
@Builder
public class TonbotPluginArgs {

    private final String prefix;
    private final IDiscordClient discordClient;
    private final File configFile;

    private TonbotPluginArgs(String prefix, IDiscordClient discordClient, File configFile) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
        this.configFile = configFile;
    }

    public Optional<File> getConfigFile() {
        return Optional.ofNullable(configFile);
    }
}
