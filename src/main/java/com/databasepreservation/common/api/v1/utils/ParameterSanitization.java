package com.databasepreservation.common.api.v1.utils;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.server.ViewerConfiguration;
import org.roda.core.data.exceptions.NotFoundException;

import java.nio.file.Path;

public class ParameterSanitization {

  public static void sanitizePath(String path, String errorMessage) throws IllegalArgumentException {
    if (path.contains("..") || path.contains("/") || path.contains("\\")) {
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
      throw new IllegalAccessException("Trying to access path outside the scope:" + scope);
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
