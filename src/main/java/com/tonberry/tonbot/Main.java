package com.tonberry.tonbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.io.IOException;
import java.net.URL;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String CONFIG_DIR_OPT = "c";
    private static final String CONFIG_DIR_LONG_OPT = "configDir";

    private static final String DEFAULT_CONFIG_DIR = "";
    private static final String CONFIG_FILE_NAME = "config.json";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(CONFIG_DIR_OPT, CONFIG_DIR_LONG_OPT, true, "specify a custom config directory");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            LOG.error("Could not parse command line arguments.", e);
            System.exit(1);
        }

        String configDir = cmd.getOptionValue(CONFIG_DIR_LONG_OPT);
        if (configDir == null) {
            configDir = DEFAULT_CONFIG_DIR;
        } else if (!configDir.endsWith("/")){
            configDir = configDir + "/";
        }

        LOG.info("The config directory is: " + configDir);

        Config config = readConfig(configDir);

        IDiscordClient discordClient = new ClientBuilder()
                .withToken(config.getDiscordBotToken())
                .build();

        Tonbot bot = Guice.createInjector(
                new TonbotModule(config.getDiscordBotToken(), config.getPrefix(), config.getPluginNames(), discordClient, configDir))
                .getInstance(Tonbot.class);

        try {
            bot.run();
        } catch (Exception e) {
            LOG.error("Tonbot died due to an uncaught exception. RIP.", e);
        }
    }

    private static Config readConfig(String configDir) {
        URL url = Resources.getResource(configDir + CONFIG_FILE_NAME);

        ObjectMapper objMapper = new ObjectMapper();
        try {
            Config config = objMapper.readValue(url, Config.class);
            return config;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read config.json.", e);
        }
    }
}
