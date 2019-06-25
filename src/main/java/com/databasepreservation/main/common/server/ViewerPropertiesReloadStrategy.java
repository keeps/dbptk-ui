/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.main.common.server;

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class ViewerPropertiesReloadStrategy extends FileChangedReloadingStrategy {

  @Override
  public void reloadingPerformed() {
    ViewerFactory.getViewerConfiguration().clearViewerCachableObjectsAfterConfigurationChange();
    super.updateLastModified();
  }

}
