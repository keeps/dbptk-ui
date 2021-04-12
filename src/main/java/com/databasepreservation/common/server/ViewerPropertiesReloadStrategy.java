/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server;

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class ViewerPropertiesReloadStrategy extends FileChangedReloadingStrategy {

  @Override
  public void reloadingPerformed() {
    ViewerFactory.getViewerConfiguration().clearViewerCachableObjectsAfterConfigurationChange();
    super.updateLastModified();
  }

}
