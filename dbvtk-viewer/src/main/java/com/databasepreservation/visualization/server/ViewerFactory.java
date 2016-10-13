package com.databasepreservation.visualization.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.ViewerConstants;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.utils.SolrManager;
import com.google.common.collect.Lists;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerFactory.class);

  private static CompositeConfiguration viewerConfiguration = null;

  private static boolean instantiated = false;
  private static SolrManager solr;
  private static Path workspace;

  public static void instantiate() {
    viewerConfiguration = new CompositeConfiguration();
    try {
      addConfiguration("dbvtk.properties");
    } catch (ConfigurationException e) {
      LOGGER.error("Error while loading DBVTK properties", e);
    }

    String solrUrl = getPropertyAsString("solr", "url");
    if (solrUrl == null) {
      solrUrl = ViewerConstants.DEFAULT_SOLR_URL;
    }
    instantiateSolrManager(solrUrl);
  }

  public static void shutdown() throws IOException {

  }

  private static void instantiateSolrManager(String solrUrl) {
    solr = new SolrManager(solrUrl);
    instantiated = true;
  }

  public static SolrManager getSolrManager() {
    if (!instantiated) {
      instantiateSolrManager(ViewerConstants.DEFAULT_SOLR_URL);
    }
    return solr;
  }

  private static void addConfiguration(String configurationFile) throws ConfigurationException {
    Configuration configuration = getConfiguration(configurationFile);
    viewerConfiguration.addConfiguration(configuration);
  }

  private static Configuration getConfiguration(String configurationFile) throws ConfigurationException {
    Path configFilePath = ViewerConstants.getWorkspaceForConfig().resolve(configurationFile);

    FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(
      PropertiesConfiguration.class)
        .configure(new Parameters().properties().setThrowExceptionOnMissing(false)
          .setListDelimiterHandler(new DisabledListDelimiterHandler()).setEncoding(RodaConstants.DEFAULT_ENCODING));
    PropertiesConfiguration propertiesConfiguration = builder.getConfiguration();

    InputStream configStream = null;

    try {
      if (Files.exists(configFilePath)) {
        LOGGER.trace("Loading configuration from file {}", configFilePath);
        configStream = Files.newInputStream(configFilePath);
      } else {
        LOGGER.trace("Loading configuration from classpath {}", configurationFile);
        configStream = ViewerFactory.class
          .getResourceAsStream("/" + ViewerSafeConstants.CLASSPATH_CONFIG_FOLDER + "/" + configurationFile);
      }
    } catch (IOException e) {
      LOGGER.debug("Could not load configuration", e);
    }

    if (configStream != null) {
      try {
        propertiesConfiguration.read(new InputStreamReader(configStream));
      } catch (IOException e) {
        LOGGER.debug("Could not read configuration", e);
      }
    }

    return propertiesConfiguration;
  }

  private static String getPropertyKey(String... keyParts) {
    StringBuilder sb = new StringBuilder();
    for (String part : keyParts) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(part);
    }
    return sb.toString();
  }

  public static String getPropertyAsString(String... keyParts) {
    return viewerConfiguration.getString(getPropertyKey(keyParts));
  }

  public static int getPropertyAsInt(String... keyParts) {
    return viewerConfiguration.getInt(getPropertyKey(keyParts), 0);
  }

  public static List<String> getPropertyAsList(String... keyParts) {
    String[] array = viewerConfiguration.getStringArray(getPropertyKey(keyParts));
    List<String> list = Lists.newArrayList(array);

    Iterator<String> iterator = list.iterator();
    while (iterator.hasNext()) {
      if (StringUtils.isBlank(iterator.next())) {
        iterator.remove();
      }
    }

    return list;
  }
}
