package com.databasepreservation.common.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.utils.ViewerAbstractConfiguration;
import com.databasepreservation.utils.FileUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Singleton configuration instance used by the Database Visualization Toolkit
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConfiguration extends ViewerAbstractConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerConfiguration.class);

  public static final String PROPERTY_SOLR_ZOOKEEPER_HOSTS = "solr.zookeeper.hosts";
  public static final String PROPERTY_SOLR_HEALTHCHECK_RETRIES = "solr.healthcheck.retries";
  public static final String PROPERTY_SOLR_HEALTHCHECK_TIMEOUT = "solr.healthcheck.timeout_ms";
  public static final String PROPERTY_SOLR_STEMMING_LANGUAGE = "solr.stemming.language";

  public static final String RESOURCES_SOLR_CONFIG_PATH = "solr-config";

  public static final String PROPERTY_BASE_UPLOAD_PATH = "manage.upload.basePath";

  public static final String PROPERTY_FILTER_AUTHENTICATION_RODA = "ui.filter.internal.enabled";

  public static final String PROPERTY_FILTER_AUTHENTICATION_CAS = "ui.filter.cas.enabled";
  public static final String PROPERTY_FILTER_AUTHENTICATION_CAS_SERVER_URL_PREFIX = "ui.filter.cas.casServerUrlPrefix";

  public static final String PROPERTY_AUTHORIZATION_ENABLED = "ui.authorization.roda.enabled";
  public static final String PROPERTY_AUTHORIZATION_GUEST_USERNAME = "ui.authorization.roda.guest.username";
  public static final String PROPERTY_AUTHORIZATION_GUEST_PASSWORD = "ui.authorization.roda.guest.password";
  public static final String PROPERTY_AUTHORIZATION_ADMINS = "ui.authorization.roda.users.admin";
  public static final String PROPERTY_AUTHORIZATION_MANAGERS = "ui.authorization.roda.users.manager";

  public static final String PROPERTY_AUTHORIZATION_RODA_CAS_SERVICE_NAME = "ui.authorization.roda.cas.serviceServerName";

  public static final String PROPERTY_AUTHORIZATION_RODA_DIP_SERVER = "ui.authorization.roda.dip.server";

  public static final String PROPERTY_RODA_ADDRESS = "ui.filter.internal.server";
  public static final String PROPERTY_RODA_PATH = "ui.filter.internal.path";
  public static final String PROPERTY_AUTHORIZATION_RODA_DIP_PATH = "ui.authorization.roda.dip.path";

  public static final String PROPERTY_FILTER_ONOFF_ALLOW_ALL_IPS = "ui.filter.onOff.protectedResourcesAllowAllIPs";
  public static final String PROPERTY_FILTER_ONOFF_WHITELISTED_IPS = "ui.filter.onOff.protectedResourcesWhitelistedIP";

  private static boolean instantiatedWithoutErrors = true;

  // configurable paths related objects
  private static Path viewerHomePath;
  private static Path lobsPath;
  private static Path logPath;
  private static Path configPath;
  private static Path exampleConfigPath;
  private static Path reportsPath;
  private static Path indexPath;
  private static Path SIARDFilesPath;

  // Configuration related objects
  private static CompositeConfiguration viewerConfiguration = null;
  private static List<String> configurationFiles = null;

  private List<String> cachedWhitelistedIPs = null;
  private Boolean cachedWhitelistAllIPs = null;
  private static LoadingCache<Locale, Messages> I18N_CACHE = CacheBuilder.newBuilder()
    .build(new CacheLoader<Locale, Messages>() {
      @Override
      public Messages load(Locale locale) throws Exception {
        return new Messages(locale, configPath.resolve(ViewerConstants.VIEWER_I18N_FOLDER));
      }
    });

  /**
   * Private constructor, use getInstance() instead
   */
  private ViewerConfiguration() {
    super(viewerConfiguration = new CompositeConfiguration());

    try {
      // determine DBVTK HOME
      viewerHomePath = determineViewerHomePath();
      LOGGER.debug("DBVTK HOME is {}", viewerHomePath);

      // instantiate essential directories
      instantiateEssentialDirectories();
      LOGGER.debug("Finished instantiating essential directories");

      // load core configurations
      configurationFiles = new ArrayList<String>();
      addConfiguration("dbvtk-viewer.properties");
      LOGGER.debug("Finished loading dbvtk-viewer.properties");
    } catch (ConfigurationException e) {
      LOGGER.error("Error loading dbvtk properties", e);
      instantiatedWithoutErrors = false;
    } catch (Exception e) {
      LOGGER.error("Error instantiating " + ViewerFactory.class.getSimpleName(), e);
      instantiatedWithoutErrors = false;
    }

    // last log message that state if system was loaded without errors or not
    LOGGER.info("DBVTK loading completed {}", instantiatedWithoutErrors ? "with success!"
      : "with some errors!!! See logs because these errors might cause instability in the system.");
  }

  private void instantiateSolr() {

  }

  /*
   * Singleton instance
   * ____________________________________________________________________________________________________________________
   */
  private static ViewerConfiguration instance = null;

  public synchronized static ViewerConfiguration getInstance() {
    if (instance == null) {
      instance = new ViewerConfiguration();
    }
    return instance;
  }

  /*
   * Implementation
   * ____________________________________________________________________________________________________________________
   */
  @Override
  public void clearViewerCachableObjectsAfterConfigurationChange() {
    I18N_CACHE.invalidateAll();
    cachedWhitelistAllIPs = null;
    cachedWhitelistedIPs = null;
    LOGGER.info("Reloaded dbvtk configurations after file change!");
  }

  @Override
  public Path getLobPath() {
    return lobsPath;
  }

  public Path getIndexPath() {
    return indexPath;
  }

  public Path getSIARDFilesPath() {
    return SIARDFilesPath;
  }

  /*
   * Specific parts to the configuration used in the DBVTK
   * ____________________________________________________________________________________________________________________
   */
  public boolean getIsAuthenticationEnabled() {
    return getViewerConfigurationAsBoolean(false, PROPERTY_FILTER_AUTHENTICATION_RODA)
      || getViewerConfigurationAsBoolean(false, PROPERTY_FILTER_AUTHENTICATION_CAS);
  }

  public static Messages getI18NMessages(String localeString) {
    return getI18NMessages(ServerTools.parseLocale(localeString));
  }

  public static Messages getI18NMessages(Locale locale) {
    checkForChangesInI18N();
    try {
      return I18N_CACHE.get(locale);
    } catch (ExecutionException e) {
      LOGGER.debug("Could not load messages", e);
      return null;
    }
  }

  public Path getReportPath(String databaseUUID) {
    return reportsPath.resolve("report-" + databaseUUID + ".md");
  }

  public Path getReportPathForMigration(String databaseUUID) {
    return reportsPath.resolve("report-migration-" + databaseUUID + ".md");
  }

  public Path getReportPathForValidation(String databaseUUID) {
    return reportsPath.resolve("report-validation-" + databaseUUID + ".md");
  }

  public List<String> getWhitelistedIPs() {
    if (cachedWhitelistedIPs == null) {
      cachedWhitelistedIPs = getViewerConfigurationAsList(ViewerConfiguration.PROPERTY_FILTER_ONOFF_WHITELISTED_IPS);
    }
    return cachedWhitelistedIPs;
  }

  public boolean getWhitelistAllIPs() {
    if (cachedWhitelistAllIPs == null) {
      cachedWhitelistAllIPs = getViewerConfigurationAsBoolean(false,
        ViewerConfiguration.PROPERTY_FILTER_ONOFF_ALLOW_ALL_IPS);
    }
    return cachedWhitelistAllIPs;
  }

  public String getDBPTKVersion() throws IOException {
    final Properties properties = new Properties();
    properties.load(ViewerConfiguration.class.getClassLoader().getResourceAsStream("main.properties"));
    return  properties.getProperty("version.dbptk");
  }

  /*
   * "Internal" helper methods
   * ____________________________________________________________________________________________________________________
   */
  private static void checkForChangesInI18N() {
    // i18n is cached and that cache is re-done when changes occur to
    // dbvtk-viewer.properties (for convenience)
    viewerConfiguration.getString("");
  }

  private static Path determineViewerHomePath() {
    Path viewerHomePath;
    if (System.getProperty(ViewerConstants.INSTALL_FOLDER_SYSTEM_PROPERTY) != null) {
      viewerHomePath = Paths.get(System.getProperty(ViewerConstants.INSTALL_FOLDER_SYSTEM_PROPERTY));
    } else if (System.getenv(ViewerConstants.INSTALL_FOLDER_ENVIRONMENT_VARIABLE) != null) {
      viewerHomePath = Paths.get(System.getenv(ViewerConstants.INSTALL_FOLDER_ENVIRONMENT_VARIABLE));
      // set dbvtk.home in order to correctly configure logging
      System.setProperty(ViewerConstants.INSTALL_FOLDER_SYSTEM_PROPERTY, viewerHomePath.toString());
    } else {
      // last attempt (using user home and hidden directory called .dbvtk)
      String userHome = System.getProperty("user.home");
      viewerHomePath = Paths.get(userHome, ViewerConstants.INSTALL_FOLDER_DEFAULT_SUBFOLDER_UNDER_HOME);
      if (!Files.exists(viewerHomePath)) {
        try {
          Files.createDirectories(viewerHomePath);
        } catch (IOException e) {
          throw new RuntimeException("Unable to create DBVTK HOME '" + viewerHomePath + "'. Aborting...", e);
        }
      }
      // set dbvtk.home in order to correctly configure logging
      System.setProperty(ViewerConstants.INSTALL_FOLDER_SYSTEM_PROPERTY, viewerHomePath.toString());
    }

    // instantiate essential directories
    configPath = viewerHomePath.resolve(ViewerConstants.VIEWER_CONFIG_FOLDER);
    exampleConfigPath = viewerHomePath.resolve(ViewerConstants.VIEWER_EXAMPLE_CONFIG_FOLDER);
    lobsPath = viewerHomePath.resolve(ViewerConstants.VIEWER_LOBS_FOLDER);
    logPath = viewerHomePath.resolve(ViewerConstants.VIEWER_LOG_FOLDER);
    reportsPath = viewerHomePath.resolve(ViewerConstants.VIEWER_REPORTS_FOLDER);
    indexPath = viewerHomePath.resolve(ViewerConstants.VIEWER_INDEX_FOLDER);
    SIARDFilesPath = viewerHomePath.resolve(ViewerConstants.VIEWER_SIARD_FILES_FOLDER);

    configureLogback();

    return viewerHomePath;
  }

  private static boolean isRunningInDocker() {
    return StringUtils.isNotBlank(System.getenv(ViewerConstants.RUNNING_IN_DOCKER_ENVIRONMENT_VARIABLE));
  }

  private static void configureLogback() {
    // used in logback.xml to set file or stdout logger
    System.setProperty(ViewerConstants.LOGGER_METHOD_PROPERTY,
      isRunningInDocker() ? ViewerConstants.LOGGER_DOCKER_METHOD : ViewerConstants.LOGGER_DEFAULT_METHOD);

    try {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      context.reset();
      configurator.doConfigure(getConfigurationFile("logback.xml"));
    } catch (JoranException e) {
      LOGGER.error("Error configuring logback", e);
    }
  }

  private static void instantiateEssentialDirectories() {
    List<Path> essentialDirectories = new ArrayList<>();
    essentialDirectories.add(configPath);
    essentialDirectories.add(viewerHomePath.resolve(ViewerConstants.VIEWER_LOG_FOLDER));
    essentialDirectories.add(lobsPath);
    essentialDirectories.add(logPath);
    essentialDirectories.add(exampleConfigPath);
    essentialDirectories.add(reportsPath);
    essentialDirectories.add(SIARDFilesPath);

    for (Path path : essentialDirectories) {
      try {
        if (!Files.exists(path)) {
          Files.createDirectories(path);
        }
      } catch (IOException e) {
        LOGGER.error("Unable to create " + path, e);
        instantiatedWithoutErrors = false;
      }
    }

    // copy configs folder from classpath to example folder
    try {
      FileUtils.deleteDirectoryRecursiveQuietly(exampleConfigPath);
      Files.createDirectories(exampleConfigPath);
      copyFilesFromClasspath(ViewerConstants.VIEWER_CONFIG_FOLDER + "/", exampleConfigPath, true,
        Arrays.asList(
          ViewerConstants.VIEWER_CONFIG_FOLDER + "/" + ViewerConstants.VIEWER_I18N_FOLDER + "/"
            + ViewerConstants.VIEWER_I18N_CLIENT_FOLDER,
          ViewerConstants.VIEWER_CONFIG_FOLDER + "/" + ViewerConstants.VIEWER_I18N_FOLDER + "/"
            + ViewerConstants.VIEWER_I18_GWT_XML_FILE));
    } catch (IOException e) {
      LOGGER.error("Unable to create " + exampleConfigPath, e);
      instantiatedWithoutErrors = false;
    }

  }

  /*
   * Configuration related functionalities
   */
  private static void addConfiguration(String configurationFile) throws ConfigurationException {
    Configuration configuration = getConfiguration(configurationFile);

    viewerConfiguration.addConfiguration(configuration);
    configurationFiles.add(configurationFile);
  }

  private static Configuration getConfiguration(String configurationFile) throws ConfigurationException {
    Path config = configPath.resolve(configurationFile);
    PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
    propertiesConfiguration.setDelimiterParsingDisabled(true);
    propertiesConfiguration.setEncoding(ViewerConstants.DEFAULT_ENCODING);

    if (Files.exists(config)) {
      LOGGER.trace("Loading configuration from file {}", config);
      propertiesConfiguration.load(config.toFile());
      ViewerPropertiesReloadStrategy rodaPropertiesReloadStrategy = new ViewerPropertiesReloadStrategy();
      rodaPropertiesReloadStrategy.setRefreshDelay(5000);
      propertiesConfiguration.setReloadingStrategy(rodaPropertiesReloadStrategy);
    } else {
      InputStream inputStream = ViewerFactory.class
        .getResourceAsStream("/" + ViewerConstants.VIEWER_CONFIG_FOLDER + "/" + configurationFile);
      if (inputStream != null) {
        LOGGER.trace("Loading configuration from classpath {}", configurationFile);
        propertiesConfiguration.load(inputStream);
      } else {
        LOGGER.trace("Configuration {} doesn't exist", configurationFile);
      }
    }

    return propertiesConfiguration.interpolatedConfiguration();
  }

  public static URL getConfigurationFile(String configurationFile) {
    Path config = configPath.resolve(configurationFile);
    URL configUri;
    if (Files.exists(config) && !Files.isDirectory(config)
      && config.toAbsolutePath().startsWith(configPath.toAbsolutePath().toString())) {
      try {
        configUri = config.toUri().toURL();
      } catch (MalformedURLException e) {
        LOGGER.error("Configuration {} doesn't exist", configurationFile);
        configUri = null;
      }
    } else {
      URL resource = ViewerConfiguration.class
        .getResource("/" + ViewerConstants.VIEWER_CONFIG_FOLDER + "/" + configurationFile);
      if (resource != null) {
        configUri = resource;
      } else {
        LOGGER.error("Configuration {} doesn't exist", configurationFile);
        configUri = null;
      }
    }

    return configUri;
  }

  public static InputStream getThemeFileAsStream(String configurationFile) {
    InputStream inputStream = null;
    if (configurationFile != null) {
      try {
        Path themePath = configPath.resolve(ViewerConstants.VIEWER_THEME_FOLDER);
        Path config = themePath.resolve(configurationFile);
        if (Files.exists(config) && !Files.isDirectory(config) && checkPathIsWithin(config, themePath)) {
          inputStream = Files.newInputStream(config);
          LOGGER.trace("Loading configuration from file {}", config);
        }
      } catch (IOException | NullPointerException e) {
        // do nothing
      }
      if (inputStream == null && !configurationFile.contains("src/test")) {
        inputStream = ViewerConfiguration.class.getResourceAsStream("/" + ViewerConstants.VIEWER_CONFIG_FOLDER + "/"
          + ViewerConstants.VIEWER_THEME_FOLDER + "/" + configurationFile);
        LOGGER.trace("Loading configuration from classpath {}", configurationFile);
      }
    }
    return inputStream;
  }

  private static boolean checkPathIsWithin(Path path, Path folder) {
    boolean ret = true;
    Path absolutePath = path.toAbsolutePath();
    // check against normalized path
    Path normalized = absolutePath.normalize();
    ret &= normalized.isAbsolute();
    ret &= normalized.startsWith(folder);
    return ret;
  }

  public static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory,
    boolean removeClasspathPrefixFromFinalPath) {
    copyFilesFromClasspath(classpathPrefix, destinationDirectory, removeClasspathPrefixFromFinalPath,
      Collections.emptyList());
  }

  protected static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory,
    boolean removeClasspathPrefixFromFinalPath, List<String> excludePaths) {

    List<ClassLoader> classLoadersList = new LinkedList<>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());

    Reflections reflections = new Reflections(
      new ConfigurationBuilder().setScanners(new ResourcesScanner()).setUrls(ClasspathHelper.forPackage(classpathPrefix,
        ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader())));

    Set<String> resources = reflections.getResources(Pattern.compile(".*"));
    resources = resources.stream().filter(r -> !shouldExclude(r, classpathPrefix, excludePaths))
      .collect(Collectors.toSet());

    LOGGER.info("Copying files from classpath prefix={}, destination={}, removePrefix={}, excludePaths={}",
      classpathPrefix, destinationDirectory, removeClasspathPrefixFromFinalPath, excludePaths, resources.size());

    for (String resource : resources) {

      InputStream originStream = ViewerFactory.class.getClassLoader().getResourceAsStream(resource);
      Path destinyPath;

      String resourceFileName = resource;

      // Removing ":" escape
      resourceFileName = resourceFileName.replace("::", ":");

      if (removeClasspathPrefixFromFinalPath) {
        destinyPath = destinationDirectory.resolve(resourceFileName.replaceFirst(classpathPrefix, ""));
      } else {
        destinyPath = destinationDirectory.resolve(resourceFileName);
      }

      try {
        // create all parent directories
        Files.createDirectories(destinyPath.getParent());
        // copy file
        Files.copy(originStream, destinyPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        LOGGER.error("Error copying file from classpath: {} to {} (reason: {})", originStream, destinyPath,
          e.getMessage());
        instantiatedWithoutErrors = false;
      } finally {
        IOUtils.closeQuietly(originStream);
      }
    }
  }

  private static boolean shouldExclude(String resource, String classpathPrefix, List<String> excludePaths) {
    boolean exclude = false;

    if (resource.startsWith(classpathPrefix)) {

      for (String excludePath : excludePaths) {
        if (resource.startsWith(excludePath)) {
          exclude = true;
          break;
        }
      }
    } else {
      exclude = true;
    }
    return exclude;
  }
}
