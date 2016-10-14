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

  public static void instantiate() {
    viewerConfiguration = new CompositeConfiguration();
    try {
      addConfiguration("dbvtk.properties");
    } catch (ConfigurationException e) {
      LOGGER.error("Error while loading DBVTK properties", e);
    }

    String solrUrl = ViewerConstants.DEFAULT_SOLR_URL;
    if (StringUtils.isNotBlank(Config.getSolrEndpoint()) && StringUtils.isNotBlank(Config.getSolrHostname())
      && StringUtils.isNotBlank(Config.getZookeeperHostname()) && Config.getSolrPort() > 0
      && Config.getZookeeperPort() > 0) {

      solrUrl = "http://" + Config.getSolrHostname() + ":" + Config.getSolrPort() + "/" + Config.getSolrEndpoint()
        + "/";
    } else {
      LOGGER.debug("There was a problem reading solrURL and default was used. Bad value: http://{}:{}/{}/}",
        Config.getSolrHostname(), Config.getSolrPort(), Config.getSolrEndpoint());
    }
    instantiateSolrManager(solrUrl);
  }

  public static void shutdown() throws IOException {

  }

  public static class Config {
    private static String solrHostname;
    private static Integer solrPort;
    private static String solrEndpoint;

    private static String zookeeperHostname;
    private static Integer zookeeperPort;

    private static String dbvtkLob;

    public static String getSolrHostname() {
      return solrHostname != null ? solrHostname : (solrHostname = getPropertyAsString("solr", "hostname"));
    }

    public static Integer getSolrPort() {
      return solrPort != null ? solrPort : (solrPort = getPropertyAsInt("solr", "port"));
    }

    public static String getSolrEndpoint() {
      return solrEndpoint != null ? solrEndpoint : (solrEndpoint = getPropertyAsString("solr", "endpoint"));
    }

    public static String getZookeeperHostname() {
      return zookeeperHostname != null ? zookeeperHostname
        : (zookeeperHostname = getPropertyAsString("zookeeper", "hostname"));
    }

    public static Integer getZookeeperPort() {
      return zookeeperPort != null ? zookeeperPort : (zookeeperPort = getPropertyAsInt("zookeeper", "port"));
    }

    public static String getDbvtkLob() {
      return dbvtkLob != null ? dbvtkLob : (dbvtkLob = getPropertyAsString("dbvtk", "lob"));
    }
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

  private static String getPropertyAsString(String... keyParts) {
    return viewerConfiguration.getString(getPropertyKey(keyParts));
  }

  private static int getPropertyAsInt(int defaultValue, String... keyParts) {
    return viewerConfiguration.getInt(getPropertyKey(keyParts), defaultValue);
  }

  private static int getPropertyAsInt(String... keyParts) {
    return getPropertyAsInt(0, keyParts);
  }

  private static List<String> getPropertyAsList(String... keyParts) {
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
