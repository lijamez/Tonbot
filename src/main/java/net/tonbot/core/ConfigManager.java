package net.tonbot.core;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;

class ConfigManager {

	private static final String DEFAULT_CONFIG_DIR_NAME = ".tonbot";
	private static final String CONFIG_FILE_NAME = "config.json";
	private static final String PLUGIN_CONFIG_DIR_NAME = "plugin_config";

	private final File configDir;

	/**
	 * Creates a {@link ConfigManager} for a particular config directory. If config
	 * dir is null or blank, it will default to a ".tonbot" directory in the user's
	 * home directory.
	 * 
	 * Regardless of what config directory is used, the config directory will be
	 * created if it doesn't exist and necessary files and directories will be added
	 * to the config dir if they are not present.
	 * 
	 * @param configDir
	 *            Configuration directory. Nullable.
	 * @throws UncheckedIOException
	 *             if config directory initialization failed.
	 */
	public ConfigManager(String configDirStr) {
		if (StringUtils.isBlank(configDirStr)) {
			// Default to user's home directory.
			String homeDir = System.getProperty("user.home");
			configDir = new File(homeDir + "/" + DEFAULT_CONFIG_DIR_NAME);
		} else {
			configDir = new File(configDirStr);
		}

		Preconditions.checkArgument(!configDir.exists() || configDir.isDirectory(),
				"configDir must not be pointing to a file.");

		initConfigDir();
	}

	private void initConfigDir() {
		if (!configDir.exists()) {
			boolean created = configDir.mkdirs();
			if (!created) {
				throw new RuntimeException("Unable to create directory " + configDir.getAbsolutePath());
			}
		}

		// Create initial config.json, if it doesn't exist.
		File configJson = new File(configDir, CONFIG_FILE_NAME);
		if (!configJson.exists()) {
			URL defaultConfigJson = Resources.getResource("initial_config.json");
			try {
				FileUtils.copyURLToFile(defaultConfigJson, configJson);
			} catch (IOException e) {
				throw new UncheckedIOException("Unable to create config.json.", e);
			}
		}

		// Create plugin_config directory, if it doesn't exist.
		File pluginConfigDir = new File(configDir, PLUGIN_CONFIG_DIR_NAME);
		pluginConfigDir.mkdirs();
	}

	/**
	 * Reads the config.json at the config directory and returns a new
	 * {@link Config} instance.
	 * 
	 * @return A new {@link Config} instance from the read config.json.
	 * @throws UncheckedIOException
	 *             if config.json could not be read.
	 */
	public Config readConfig() {
		File configFile = new File(configDir.getAbsolutePath() + "/" + CONFIG_FILE_NAME);
		Preconditions.checkArgument(configFile.exists(),
				"config file doesn't exist at: " + configFile.getAbsolutePath());

		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.enable(Feature.ALLOW_COMMENTS);
		ObjectMapper objMapper = new ObjectMapper(jsonFactory);

		try {
			Config config = objMapper.readValue(configFile, Config.class);
			return config;
		} catch (IOException e) {
			throw new UncheckedIOException("Unable to read config.json.", e);
		}
	}

	/**
	 * Gets the absolute path to the config directory.
	 * 
	 * @return The absolute path to the config directory.
	 */
	public String getConfigDirPath() {
		return configDir.getAbsolutePath();
	}
}
