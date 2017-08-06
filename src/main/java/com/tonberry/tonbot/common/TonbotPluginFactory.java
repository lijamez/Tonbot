package com.tonberry.tonbot.common;

/**
 * Tonbot uses classes of this type to instantiate {@link Plugin}s.
 * The Tonbot plugin loader will create an instance of this class using a zero-parameter constructor.
 */
public interface TonbotPluginFactory {

    /**
     * Accepta a set of arguments from Tonbot. Tonbot will supply these arguments before build() is called.
     * This method should also be used initialize plugin-specific configurations, such as reading from a config file.
     *
     * @param args {@link TonbotPluginArgs}. Non-null.
     */
    void initialize(TonbotPluginArgs args);

    /**
     * Called by the Tonbot plugin loader to create a new {@link Plugin}.
     * @return {@link Plugin}. Null values will be ignored by the plugin loader.
     */
    Plugin build();
}
