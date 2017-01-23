package com.databasepreservation.visualization.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.utils.FileUtils;
import com.databasepreservation.visualization.ViewerConstants;
import com.databasepreservation.visualization.utils.ViewerAbstractConfiguration;

/**
 * Singleton configuration instance used by the Database Visualization Toolkit
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConfiguration extends ViewerAbstractConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerConfiguration.class);

  public static final String[] PROPERTY_AUTHORIZATION_ENABLED = new String[] {"ui", "authorization", "roda", "enabled"};
  public static final String[] PROPERTY_AUTHORIZATION_ADMINS = new String[] {"ui", "authorization", "roda", "users",
    "admin"};
  public static final String[] PROPERTY_AUTHORIZATION_MANAGERS = new String[] {"ui", "authorization", "roda", "users",
    "manager"};

  public static final String[] PROPERTY_RODA_ADDRESS = new String[] {"ui", "filter", "internal", "server"};
  public static final String[] PROPERTY_RODA_PATH = new String[] {"ui", "filter", "internal", "path"};

  public static final String[] PROPERTY_AUTHORIZATION_QUERY_USER_USERNAME = new String[] {"ui", "authorization",
    "roda", "query", "user", "username"};
  public static final String[] PROPERTY_AUTHORIZATION_QUERY_USER_PASSWORD = new String[] {"ui", "authorization",
    "roda", "query", "user", "password"};
  public static final String[] PROPERTY_AUTHORIZATION_QUERY_PATH = new String[] {"ui", "authorization", "roda",
    "query", "path"};
  public static final String[] PROPERTY_AUTHORIZATION_QUERY_PARAMETERS = new String[] {"ui", "authorization", "roda",
    "query", "parameters"};
  public static final String[] PROPERTY_AUTHORIZATION_CACHE_TTL = new String[] {"ui", "authorization", "roda", "cache",
    "ttl"};

  private static boolean instantiatedWithoutErrors = true;

  // configurable paths related objects
  private static Path viewerHomePath;
  private static Path lobsPath;
  private static Path logPath;
  private static Path configPath;
  private static Path exampleConfigPath;

  // Configuration related objects
  private static CompositeConfiguration viewerConfiguration = null;
  private static List<String> configurationFiles = null;
  private static Map<String, Map<String, String>> viewerPropertiesCache = null;

  /**
   * Private constructor, use getInstance() instead
   */
  private ViewerConfiguration() {
    super(viewerConfiguration = new CompositeConfiguration());

    try {
      // determine RODA HOME
      viewerHomePath = determineViewerHomePath();
      LOGGER.debug("DBVTK HOME is {}", viewerHomePath);

      // instantiate essential directories
      instantiateEssentialDirectories();
      LOGGER.debug("Finished instantiating essential directories");

      // load core configurations
      configurationFiles = new ArrayList<String>();
      viewerPropertiesCache = new HashMap<>();
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
    LOGGER.info("RODA Core loading completed {}", instantiatedWithoutErrors ? "with success!"
      : "with some errors!!! See logs because these errors might cause instability in the system.");
  }

  /*
   * Singleton instance
   * ____________________________________________________________________________________________________________________
   */
  private static ViewerConfiguration instance = null;

  public static ViewerConfiguration getInstance() {
    if (instance == null) {
      instance = new ViewerConfiguration();
    }
    return instance;
  }

  /*
   * Implementation-dependent parts
   * ____________________________________________________________________________________________________________________
   */
  @Override
  public void clearViewerCachableObjectsAfterConfigurationChange() {
    viewerPropertiesCache.clear();
    // I18N_CACHE.invalidateAll();
    LOGGER.info("Reloaded dbvtk configurations after file change!");
  }

  @Override
  public Path getLobPath() {
    return lobsPath;
  }

  /*
   * "Internal" helper methods
   * ____________________________________________________________________________________________________________________
   */
  private static Path determineViewerHomePath() {
    Path viewerHomePath;
    if (System.getProperty(ViewerConstants.INSTALL_FOLDER_SYSTEM_PROPERTY) != null) {
      viewerHomePath = Paths.get(System.getProperty(ViewerConstants.INSTALL_FOLDER_SYSTEM_PROPERTY));
    } else if (System.getenv(ViewerConstants.INSTALL_FOLDER_ENVIRONMENT_VARIABLE) != null) {
      viewerHomePath = Paths.get(System.getenv(ViewerConstants.INSTALL_FOLDER_ENVIRONMENT_VARIABLE));
    } else {
      // last attempt (using user home and hidden directory called .roda)
      String userHome = System.getProperty("user.home");
      viewerHomePath = Paths.get(userHome, ViewerConstants.INSTALL_FOLDER_DEFAULT_SUBFOLDER_UNDER_HOME);
      if (!Files.exists(viewerHomePath)) {
        try {
          Files.createDirectories(viewerHomePath);
        } catch (IOException e) {
          throw new RuntimeException("Unable to create DBVTK HOME '" + viewerHomePath + "'. Aborting...", e);
        }
      }

      // set roda.home in order to correctly configure logging even if no
      // property has been defined
      System.setProperty(ViewerConstants.INSTALL_FOLDER_SYSTEM_PROPERTY, viewerHomePath.toString());
    }

    // instantiate essential directories
    configPath = viewerHomePath.resolve(ViewerConstants.VIEWER_CONFIG_FOLDER);
    exampleConfigPath = viewerHomePath.resolve(ViewerConstants.VIEWER_EXAMPLE_CONFIG_FOLDER);
    lobsPath = viewerHomePath.resolve(ViewerConstants.VIEWER_LOBS_FOLDER);
    logPath = viewerHomePath.resolve(ViewerConstants.VIEWER_LOG_FOLDER);

    return viewerHomePath;
  }

  private static void instantiateEssentialDirectories() {
    List<Path> essentialDirectories = new ArrayList<>();
    essentialDirectories.add(configPath);
    essentialDirectories.add(viewerHomePath.resolve(ViewerConstants.CORE_LOG_FOLDER));
    essentialDirectories.add(lobsPath);
    essentialDirectories.add(logPath);
    essentialDirectories.add(exampleConfigPath);

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
      copyFilesFromClasspath(ViewerConstants.VIEWER_CONFIG_FOLDER + "/", exampleConfigPath, true);
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
      InputStream inputStream = ViewerFactory.class.getResourceAsStream("/" + ViewerConstants.VIEWER_CONFIG_FOLDER
        + "/" + configurationFile);
      if (inputStream != null) {
        LOGGER.trace("Loading configuration from classpath {}", configurationFile);
        propertiesConfiguration.load(inputStream);
      } else {
        LOGGER.trace("Configuration {} doesn't exist", configurationFile);
      }
    }

    return propertiesConfiguration;
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory) {
    copyFilesFromClasspath(classpathPrefix, destinationDirectory, false);
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory,
    boolean removeClasspathPrefixFromFinalPath) {
    copyFilesFromClasspath(classpathPrefix, destinationDirectory, removeClasspathPrefixFromFinalPath,
      Collections.emptyList());
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory,
    boolean removeClasspathPrefixFromFinalPath, List<String> excludePaths) {

    List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());

    Reflections reflections = new Reflections(new ConfigurationBuilder()
      .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(classpathPrefix)))
      .setScanners(new ResourcesScanner())
      .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[] {}))));

    Set<String> resources = reflections.getResources(Pattern.compile(".*"));

    LOGGER.info("Copy files from classpath prefix={}, destination={}, removePrefix={}, excludePaths={}, #resources={}",
      classpathPrefix, destinationDirectory, removeClasspathPrefixFromFinalPath, excludePaths, resources.size());

    for (String resource : resources) {
      boolean exclude = false;
      for (String excludePath : excludePaths) {
        if (resource.startsWith(excludePath)) {
          exclude = true;
          break;
        }
      }
      if (exclude) {
        continue;
      }

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
}
