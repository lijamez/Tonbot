package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class PluginLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginLoader.class);

    /**
     * Instantiates plugins based on the fully qualified names of their {@link TonbotPluginFactory}.
     * Names of factories that do not exist in the classpath will be ignored.
     * Any factories that return a null {@link Plugin} will be ignored.
     *
     * @param pluginFqns List of fully qualified {@link TonbotPluginFactory} class names.
     * @return A list of {@link Plugin}s.
     */
    public List<Plugin> instantiatePlugins(List<String> pluginFqns, TonbotPluginArgs pluginArgs) {
        Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
        Preconditions.checkNotNull(pluginArgs, "pluginArgs must be non-null.");

        return pluginFqns.stream()
                .map(factoryClassName -> {
                    try {
                        Class<?> factoryClass = Class.forName(factoryClassName);
                        if (!TonbotPluginFactory.class.isAssignableFrom(factoryClass)) {
                            LOG.warn("Plugin '{}' could not be loaded because it is not a TonbotPluginFactory.", factoryClassName);
                            return null;
                        }

                        TonbotPluginFactory pluginFactory = (TonbotPluginFactory) factoryClass.newInstance();
                        try {
                            pluginFactory.initialize(pluginArgs);
                            Plugin plugin = pluginFactory.build();
                            return plugin;
                        } catch (Exception e) {
                            LOG.warn("Plugin '{}' could not be loaded.", factoryClassName, e);
                        }

                    } catch (ClassNotFoundException e) {
                        LOG.warn("Plugin '{}' could not be loaded because it was not found on the classpath.", factoryClassName);
                    } catch (IllegalAccessException | InstantiationException e) {
                        LOG.warn("Plugin '{}' could not be loaded because the builder not be instantiated.", factoryClassName, e);
                    }

                    return null;
                })
                .filter(plugin -> plugin != null)
                .collect(Collectors.toList());
    }
}
