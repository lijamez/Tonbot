package net.tonbot.core;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;

class ConfigManager {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

	private static final String DEFAULT_CONFIG_DIR_NAME = ".tonbot";
	private static final String CONFIG_FILE_NAME = "tonbot.config";
	private static final String PLUGIN_CONFIG_DIR_NAME = "plugins";

	private static final String INITIAL_CONFIG_FILE_NAME = "initial_config.json";

	private final ObjectMapper objMapper;
	private final File configDir;

	/**
	 * Creates a {@link ConfigManager} for a particular config directory. If config
	 * dir is null or blank, it will default to a ".tonbot" directory in the user's
	 * home directory.<br/>
	 * 
	 * The config directory is not created immediately upon {@link ConfigManager}
	 * construction. The {@link #initConfigDir()} method must be called for
	 * initialization.
	 * 
	 * @param configDir
	 *            Configuration directory. Nullable.
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

		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.enable(Feature.ALLOW_COMMENTS);
		this.objMapper = new ObjectMapper(jsonFactory);
	}

	/**
	 * Initializes the config directory, wherever necessary.<br/>
	 * 
	 * If the directory is missing, then it is created with the basic structure and
	 * initial config files. <br/>
	 * 
	 * If the directory exists, but some of its essential files are missing, then
	 * those files will be created.
	 */
	public void initConfigDir() {
		if (!configDir.exists()) {
			LOG.info("The config directory {} doesn't exist. Creating it...", configDir.getAbsolutePath());
			boolean created = configDir.mkdirs();
			if (!created) {
				throw new RuntimeException("Unable to create directory " + configDir.getAbsolutePath());
			}
		}

		// Create initial config.json, if it doesn't exist.
		File configJson = new File(configDir, CONFIG_FILE_NAME);
		if (!configJson.exists()) {
			URL defaultConfigJson = Resources.getResource(INITIAL_CONFIG_FILE_NAME);
			try {
				FileUtils.copyURLToFile(defaultConfigJson, configJson);
			} catch (IOException e) {
				throw new UncheckedIOException("Unable to create " + CONFIG_FILE_NAME, e);
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
	 * @throws IllegalStateException
	 *             if the config.json doesn't exist.
	 */
	public Config readConfig() {
		File configFile = new File(configDir.getAbsolutePath() + "/" + CONFIG_FILE_NAME);
		Preconditions.checkState(configFile.exists(),
				"config file doesn't exist at: " + configFile.getAbsolutePath());

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
	 * @return The path to the config directory.
	 */
	public Path getConfigDirPath() {
		return Paths.get(configDir.getAbsolutePath());
	}

	/**
	 * Determines whether if the config directory exists. Does not check the
	 * validity of the config dir's files.
	 * 
	 * @return True iff the config directory exists.
	 */
	public boolean configDirExists() {
		return this.configDir.exists();
	}
}
