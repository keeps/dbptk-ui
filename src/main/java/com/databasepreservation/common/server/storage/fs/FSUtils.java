package com.databasepreservation.common.server.storage.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FSUtils {
  public static final Logger LOGGER = LoggerFactory.getLogger(FSUtils.class);

  public static boolean exists(Path file) {
    return file != null && file.toFile().exists();
  }
}
