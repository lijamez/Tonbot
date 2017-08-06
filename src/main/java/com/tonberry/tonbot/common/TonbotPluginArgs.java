package com.tonberry.tonbot.common;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import sx.blah.discord.api.IDiscordClient;

import java.net.URL;
import java.util.Optional;

/**
 * A standard set of data passed to every Tonbot PluginResources.
 */
@Data
@Builder
public class TonbotPluginArgs {

    private final String prefix;
    private final IDiscordClient discordClient;
    private final URL configFileUrl;

    private TonbotPluginArgs(String prefix, IDiscordClient discordClient, URL configFileUrl) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
        this.configFileUrl = configFileUrl;
    }

    public Optional<URL> getConfigFileUrl() {
        return Optional.ofNullable(configFileUrl);
    }
}
