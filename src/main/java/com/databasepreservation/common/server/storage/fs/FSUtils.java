/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.storage.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FSUtils {
  public static final Logger LOGGER = LoggerFactory.getLogger(FSUtils.class);

  public static boolean exists(Path file) {
    return file != null && file.toFile().exists();
  }

  public static boolean delete(Path file) {
    try {
      Files.delete(file);
      return true;
    } catch (IOException e) {
      LOGGER.debug("File not found", e);
      return false;
    }
  }

  public static boolean createDirectory(Path path) {
    if (!Files.exists(path)) {
      try {
        Files.createDirectories(path);
        return true;
      } catch (IOException e) {
        return false;
      }
    }

    return false;
  }
}
