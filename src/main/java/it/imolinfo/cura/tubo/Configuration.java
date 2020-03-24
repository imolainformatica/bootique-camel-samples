package it.imolinfo.cura.tubo;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;

public class Configuration {
	String							basePath;
	Boolean							shouldCheckForFolderExistance;
	List<ReceiverConfiguration>		receivers;
	List<TransmitterConfiguration>	transmitters;

	public interface Headers {
		String FILE_NAME = "X-file-name";
	}

	protected enum FileType {
		DIRECTORY,
		FILE
	}

	@SuppressWarnings("unchecked")
	public Configuration (String configurationFilePath, Boolean checkForFolderExistance) throws Exception {
		Yaml yaml;
		DumperOptions options;
		Map<String, Object> configuration;

		this.basePath = FilenameUtils.getPrefix(configurationFilePath) + FilenameUtils.getPath(configurationFilePath);
		this.shouldCheckForFolderExistance = checkForFolderExistance;

		options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED);

		yaml = new Yaml(options);
		configuration = (Map<String, Object>) yaml.load(new FileReader(configurationFilePath));

		this.receivers = new LinkedList<>();
		this.transmitters = new LinkedList<>();

		for (Map<String, Object> receiverConfiguration: handleNull(configuration.get("receivers"))) {
			this.receivers.add(new ReceiverConfiguration(this.basePath, receiverConfiguration));
		}

		for (Map<String, Object> transmitterConfiguration: handleNull(configuration.get("transmitters"))) {
			this.transmitters.add(new TransmitterConfiguration(this.basePath, transmitterConfiguration));
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> handleNull(final Object obj) {
		if (obj == null) {
			return Collections.emptyList();
		}
		return (List<Map<String, Object>>) obj;
	}

	public List<ReceiverConfiguration> receivers() {
		return this.receivers;
	}

	public List<TransmitterConfiguration> transmitters() {
		return this.transmitters;
	}

	protected String fullPathFromRelative(String aBasePath, Map<String, Object> aConfiguration, String aKey, FileType aFileType) throws Exception {
		Path	directoryPath;
		String	configurationValue = (String) aConfiguration.get(aKey);
		String	result = FilenameUtils.concat(aBasePath, configurationValue);

		if (this.shouldCheckForFolderExistance) {
			if (aFileType == FileType.DIRECTORY) {
				directoryPath = Paths.get(result);
			} else {
				directoryPath = new File(result).getParentFile().toPath();
			}

			if (! Files.isDirectory(directoryPath)) {
				throw new Exception("Folder " + directoryPath.toString() + " does not exists. [" + aKey + ": " + configurationValue + "]");
			}
		}

		return result;
	}

	//==========================================================================

	public class ReceiverConfiguration {
		public final String name;
		public final String routeName;
		public final String path;
		public final String targetFolder;
		public final String logFile;
		public final Logger	logger;
		public final int	idempotentRepositoryEntries;

//	- name          : Nome1
//	  path          : /receiver
//	  targetFolder  : inbox/1
//    receiptFolder : inbox/receipt/1
//    duplicatedFolder: inbox/duplicates/1
//	  logFile       : ./receiver.log
//	  localKey      : *receiver_local
//	  remoteKey     : *transmitter_remote

		@SuppressWarnings("unchecked")
		public ReceiverConfiguration(String basePath, Map<String, Object> configuration) throws Exception {
			this.name = (String) configuration.get("name");
			this.routeName = "receiver-" + this.name;
			this.path = (String) configuration.get("path");
			this.targetFolder = fullPathFromRelative(basePath, configuration, "targetFolder", FileType.DIRECTORY);
			this.logFile = fullPathFromRelative(basePath, configuration, "logFile", FileType.FILE);
			this.logger = createLoggerFor("tubo.receiver." + this.name, this.logFile);
			if (configuration.containsKey("idempotentRepositoryEntries")) {
				this.idempotentRepositoryEntries = (Integer) configuration.get("idempotentRepositoryEntries");
			} else {
				this.idempotentRepositoryEntries = 2000;
			}
		}
	}


	//==========================================================================


	public class TransmitterConfiguration {

		public final String	name;
		public final String	localFolder;
		public final String	targetUrl;
		public final String	archiveFolder;
		public final String	errorFolder;
		public final String	logFile;
        public final String deliveryNotification;
		public final Logger	logger;

//	- name                 : Nome1
//	  targetUrl            : http://homer.local:8084/tubo/receiver2
//	  localFolder          : outbox
//	  archiveFolder        : archive
//    receiptFolder        : archive
//	  errorFolder          : errors
//	  logFile              : ./producer.log
//	  localKey             : *transmitter_local
//	  remoteKey            : *receiver_remote
//    deliveryNotification : local/notifications

		@SuppressWarnings("unchecked")
		private TransmitterConfiguration(String basePath, Map<String, Object> configuration) throws Exception {
			this.name = (String) configuration.get("name");
			this.localFolder = fullPathFromRelative(basePath, configuration, "localFolder", FileType.DIRECTORY);
			this.targetUrl = (String) configuration.get("targetUrl");
			this.archiveFolder = fullPathFromRelative(basePath, configuration, "archiveFolder", FileType.DIRECTORY);
			this.errorFolder = fullPathFromRelative(basePath, configuration, "errorFolder", FileType.DIRECTORY);
			this.logFile = fullPathFromRelative(basePath, configuration, "logFile", FileType.FILE);
            if (configuration.containsKey("deliveryNotification")) {
                this.deliveryNotification = fullPathFromRelative(basePath, configuration, "deliveryNotification", FileType.DIRECTORY);
            } else {
                this.deliveryNotification = null;
            }
			this.logger = createLoggerFor("tubo.receiver." + this.name, this.logFile);
		}
	}

	//==========================================================================

	private static Logger createLoggerFor(String categoryName, String filename) {
		LoggerContext						loggerContext;
		PatternLayoutEncoder				encoder;
		RollingFileAppender<ILoggingEvent>	fileAppender;
		ch.qos.logback.classic.Logger		logger;
		TimeBasedRollingPolicy				rollingPolicy;

		loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		encoder = new PatternLayoutEncoder();
		encoder.setPattern("%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSXXX\", UTC} %msg%n");
		encoder.setContext(loggerContext);
		encoder.start();

		fileAppender = new RollingFileAppender<>();

		rollingPolicy = new TimeBasedRollingPolicy();
		rollingPolicy.setContext(loggerContext);
		rollingPolicy.setFileNamePattern(filename + "-%d{yyyy-MM-dd}.log");
		rollingPolicy.setParent(fileAppender);
		rollingPolicy.start();

		fileAppender.setFile(filename);
		fileAppender.setEncoder(encoder);
		fileAppender.setContext(loggerContext);
		fileAppender.setRollingPolicy(rollingPolicy);
		fileAppender.start();

		logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(categoryName);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);
		logger.setAdditive(false); // set to true if root should log too

		return logger;
	}
}
