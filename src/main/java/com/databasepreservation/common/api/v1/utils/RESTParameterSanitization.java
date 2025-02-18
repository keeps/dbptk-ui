package com.databasepreservation.common.api.v1.utils;

import com.databasepreservation.common.server.ViewerConfiguration;
import org.roda.core.data.exceptions.NotFoundException;

import java.nio.file.Path;

public class RESTParameterSanitization {

  public static void sanitizePath(String path, String errorMessage) throws IllegalArgumentException {
    if (path.contains("..") || path.contains("/") || path.contains("\\")) {
      throw new IllegalArgumentException(errorMessage);
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


  private RESTParameterSanitization() {
    // do nothing
  }
}
