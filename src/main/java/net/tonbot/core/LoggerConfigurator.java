package net.tonbot.core;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.common.base.Preconditions;

class LoggerConfigurator {

	private static final String APPLICATION_LOG_NAME = "application.log";

	/**
	 * Configures Log4j 2 by adding a file appender to the root logger. The file
	 * named {@code application.log} will be located in a directory specified by
	 * {@code logRootPath}.
	 * 
	 * @param logRootPath
	 *            A path to a directory which will contain logs. Directory will be
	 *            created if it doesn't exist. Non-null.
	 */
	public static void configureLog4j(Path logRootPath) {
		Preconditions.checkNotNull(logRootPath, "logRootPath must be non-null.");

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		PatternLayout layout = PatternLayout.newBuilder()
				.withPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN)
				.withConfiguration(config)
				.build();

		Path applicationLogPath = Paths.get(logRootPath.toString(), APPLICATION_LOG_NAME);

		FileAppender appender = FileAppender.newBuilder()
				.withFileName(applicationLogPath.toString())
				.withImmediateFlush(true)
				.withLayout(layout)
				.withAppend(true)
				.withName("File Appender")
				.build();
		appender.start();
		config.addAppender(appender);
		ctx.getRootLogger().addAppender(appender);

	}
}
