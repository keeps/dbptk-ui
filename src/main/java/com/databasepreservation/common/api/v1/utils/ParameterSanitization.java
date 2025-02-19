/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1.utils;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.server.ViewerConfiguration;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ParameterSanitization {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParameterSanitization.class);
  public static void sanitizePath(String path, String errorMessage) throws IllegalArgumentException {
    if (path != null && (path.contains("..") || path.contains("/") || path.contains("\\"))) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  public static void checkPathIsWithin(Path scope, Path toVerify) throws IllegalAccessException {
    boolean ret = true;
    Path absolutePath = toVerify.toAbsolutePath();
    // check against normalized scope
    Path normalized = absolutePath.normalize();
    ret &= normalized.isAbsolute();
    ret &= normalized.startsWith(scope);

    if (!ret) {
      LOGGER.debug("Trying to access {} outside the scope {}", normalized, scope);
      throw new IllegalAccessException("Trying to access path outside the scope");
    }
  }

  public static Path sanitizeSiardPath(String path, String errorMessage) throws NotFoundException {
    Path safeDirectory = ViewerConfiguration.getInstance().getSIARDFilesPath().normalize().toAbsolutePath();
    Path resolvedPath = safeDirectory.resolve(path).normalize().toAbsolutePath();
    if (!resolvedPath.startsWith(safeDirectory)) {
      throw new NotFoundException(errorMessage);
    }
    return resolvedPath;
  }


  private ParameterSanitization() {
    // do nothing
  }
}
