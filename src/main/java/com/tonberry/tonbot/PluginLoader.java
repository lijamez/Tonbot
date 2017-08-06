package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

class PluginLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginLoader.class);

    private final String configDir;

    @Inject
    public PluginLoader(@ConfigDir String configDir) {
        this.configDir = Preconditions.checkNotNull(configDir, "configDir must be non-null.");
    }

    /**
     * Instantiates plugins based on the fully qualified names of their {@link TonbotPlugin}.
     * Names of factories that do not exist in the classpath will be ignored.
     * Any factories that return a null {@link PluginResources} will be ignored.
     *
     * @param pluginFqns List of fully qualified {@link TonbotPlugin} class names.
     * @param prefix The prefix. Non-null.
     * @param discordClient The discord client. Non-null.
     * @return A list of {@link PluginResources}s.
     */
    public List<PluginResources> instantiatePlugins(List<String> pluginFqns, String prefix, IDiscordClient discordClient) {
        Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
        Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");

        return pluginFqns.stream()
                .map(factoryClassName -> {
                    try {
                        Class<?> factoryClass = Class.forName(factoryClassName);
                        if (!TonbotPlugin.class.isAssignableFrom(factoryClass)) {
                            LOG.warn("PluginResources '{}' could not be loaded because it is not a TonbotPlugin.", factoryClassName);
                            return null;
                        }

                        URL configUrl = null;
                        try {
                            configUrl = Resources.getResource("plugin_config/" + factoryClass.getName() + ".config");
                        } catch (IllegalArgumentException e) {
                            // This is fine. There's no configuration file for this particular plugin.
                        }

                        TonbotPlugin plugin = (TonbotPlugin) factoryClass.newInstance();

                        TonbotPluginArgs pluginArgs = getPluginArgs(plugin, prefix, discordClient);
                        try {
                            plugin.initialize(pluginArgs);
                            PluginResources pluginResources = plugin.build();
                            return pluginResources;
                        } catch (Exception e) {
                            LOG.warn("PluginResources '{}' could not be loaded.", factoryClassName, e);
                        }

                    } catch (ClassNotFoundException e) {
                        LOG.warn("PluginResources '{}' could not be loaded because it was not found on the classpath.", factoryClassName);
                    } catch (IllegalAccessException | InstantiationException e) {
                        LOG.warn("PluginResources '{}' could not be loaded because the builder not be instantiated.", factoryClassName, e);
                    }

                    return null;
                })
                .filter(plugin -> plugin != null)
                .collect(Collectors.toList());
    }

    private TonbotPluginArgs getPluginArgs(TonbotPlugin plugin, String prefix, IDiscordClient discordClient) {
        URL configUrl = null;
        try {
            configUrl = Resources.getResource(configDir + "plugin_config/" + plugin.getClass().getName() + ".config");
        } catch (IllegalArgumentException e) {
            // This is fine. It just means there's no configuration file for this particular plugin.
        }

        TonbotPluginArgs pluginArgs = TonbotPluginArgs.builder()
                .discordClient(discordClient)
                .prefix(prefix)
                .configFileUrl(configUrl)
                .build();

        return pluginArgs;
    }
}
