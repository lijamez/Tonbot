package net.tonbot.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

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
		} else if (!configDir.endsWith("/")) {
			configDir = configDir + "/";
		}

		LOG.info("The config directory is: " + configDir);

		Config config = readConfig(configDir);

		IDiscordClient discordClient = new ClientBuilder()
				.withToken(config.getDiscordBotToken())
				.build();

		Tonbot bot = Guice.createInjector(
				new TonbotModule(config.getPrefix(), config.getPluginNames(), discordClient, configDir))
				.getInstance(Tonbot.class);

		try {
			bot.run();
		} catch (Exception e) {
			LOG.error("Tonbot died due to an uncaught exception. RIP.", e);
		}
	}

	private static Config readConfig(String configDir) {
		File configFile = new File(configDir + "/" + CONFIG_FILE_NAME);
		Preconditions.checkArgument(configFile.exists(),
				"config file doesn't exist at: " + configFile.getAbsolutePath());

		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.enable(Feature.ALLOW_COMMENTS);
		ObjectMapper objMapper = new ObjectMapper(jsonFactory);
		
		try {
			Config config = objMapper.readValue(configFile, Config.class);
			return config;
		} catch (IOException e) {
			throw new RuntimeException("Unable to read config.json.", e);
		}
	}
}
