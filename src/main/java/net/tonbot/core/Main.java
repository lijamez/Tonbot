package net.tonbot.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private static final String CONFIG_DIR_OPT = "c";
	private static final String CONFIG_DIR_LONG_OPT = "configDir";

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
		ConfigManager configMgr = new ConfigManager(configDir);

		LOG.info("The config directory is: " + configMgr.getConfigDirPath());

		Config config = configMgr.readConfig();

		String token = config.getDiscordBotToken();

		if (StringUtils.isBlank(token)) {
			// An empty token is a sign that the bot isn't setup yet. In that case, return a
			// friendly error message.
			LOG.error("Tonbot is not configured! Please edit the config.json at " + configMgr.getConfigDirPath());
			System.exit(1);
		}

		IDiscordClient discordClient = new ClientBuilder()
				.withToken(token)
				.build();

		Tonbot bot = Guice.createInjector(
				new TonbotModule(config.getPrefix(), config.getPluginNames(), discordClient,
						configMgr.getConfigDirPath()))
				.getInstance(Tonbot.class);

		try {
			bot.run();
		} catch (Exception e) {
			LOG.error("Tonbot died due to an uncaught exception. RIP.", e);
		}
	}
}
