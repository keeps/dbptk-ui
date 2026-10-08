/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.databasepreservation.common.server.ViewerConfiguration;

@Configuration
public class SearchContentConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchContentConfig.class);

  public static final String SEARCH_CONTENT_EXECUTOR_BEAN_NAME = "searchContentTaskExecutor";

  private static final int DEFAULT_POOL_SIZE = 4;
  private static final int DEFAULT_COLLECTION_TIME_ALLOWED_MILLIS = 30_000;
  private static final long DEFAULT_SEARCH_TIMEOUT_MILLIS = 120_000L;

  private int poolSize;
  private int collectionTimeAllowedMillis;
  private long searchTimeoutMillis;

  public SearchContentConfig() {
    ViewerConfiguration configuration = ViewerConfiguration.getInstance();
    setPoolSize(
      configuration.getViewerConfigurationAsInt(DEFAULT_POOL_SIZE, ViewerConfiguration.PROPERTY_SEARCH_ALL_POOL_SIZE));
    setCollectionTimeAllowedMillis(configuration.getViewerConfigurationAsInt(DEFAULT_COLLECTION_TIME_ALLOWED_MILLIS,
      ViewerConfiguration.PROPERTY_SEARCH_ALL_COLLECTION_TIMEOUT));
    setSearchTimeoutMillis(configuration.getViewerConfigurationAsLong(DEFAULT_SEARCH_TIMEOUT_MILLIS,
      ViewerConfiguration.PROPERTY_SEARCH_ALL_TIMEOUT));
  }

  @Bean(name = SEARCH_CONTENT_EXECUTOR_BEAN_NAME)
  public ThreadPoolTaskExecutor searchAllTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setThreadNamePrefix("search-all-");
    executor.initialize();
    return executor;
  }

  public int getCollectionTimeAllowedMillis() {
    return collectionTimeAllowedMillis;
  }

  public long getSearchTimeoutMillis() {
    return searchTimeoutMillis;
  }

  private void setPoolSize(int poolSize) {
    if (poolSize < 1) {
      LOGGER.warn("Invalid value {} for {}, using {}", poolSize, ViewerConfiguration.PROPERTY_SEARCH_ALL_POOL_SIZE,
        DEFAULT_POOL_SIZE);
      poolSize = DEFAULT_POOL_SIZE;
    }
    this.poolSize = poolSize;
  }

  private void setCollectionTimeAllowedMillis(int collectionTimeAllowedMillis) {
    if (collectionTimeAllowedMillis < 1) {
      LOGGER.warn("Invalid value {} for {}, using {}", collectionTimeAllowedMillis,
        ViewerConfiguration.PROPERTY_SEARCH_ALL_COLLECTION_TIMEOUT, DEFAULT_COLLECTION_TIME_ALLOWED_MILLIS);
      collectionTimeAllowedMillis = DEFAULT_COLLECTION_TIME_ALLOWED_MILLIS;
    }
    this.collectionTimeAllowedMillis = collectionTimeAllowedMillis;
  }

  private void setSearchTimeoutMillis(long searchTimeoutMillis) {
    if (searchTimeoutMillis < 1) {
      LOGGER.warn("Invalid value {} for {}, using {}", searchTimeoutMillis,
        ViewerConfiguration.PROPERTY_SEARCH_ALL_TIMEOUT, DEFAULT_SEARCH_TIMEOUT_MILLIS);
      searchTimeoutMillis = DEFAULT_SEARCH_TIMEOUT_MILLIS;
    }
    this.searchTimeoutMillis = searchTimeoutMillis;
  }
}
