/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.utils;

import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_1007;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_1007_EXT;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_128;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_128_EXT;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_V21;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_V22;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.databasepreservation.model.exception.SIARDVersionNotSupportedException;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class SiardUtils {

  private static final Pattern SIARD_DK_PART_FOLDER_PATTERN = Pattern
    .compile("^(AVID\\.[A-ZÆØÅ]{2,4}\\.[1-9][0-9]*)\\.[1-9][0-9]*$");

  public static Long calculateSize(Path siardPath, String siardVersion) throws SIARDVersionNotSupportedException {
    if (siardPath == null || !siardPath.toFile().exists()) {
      return 0L;
    }

    if (siardVersion.equals(SIARD_DK_128) || siardVersion.equals(SIARD_DK_1007) || siardVersion.equals(SIARD_DK_128_EXT)
      || siardVersion.equals(SIARD_DK_1007_EXT)) {
      return calculateSIARDDKSize(siardPath.toFile());
    } else if (siardVersion.equals(SIARD_V22) || siardVersion.equals(SIARD_V21)) {
      return siardPath.toFile().length();
    } else {
      throw new SIARDVersionNotSupportedException();
    }
  }

  private static long calculateSIARDDKSize(File selectedFolder) {
    Matcher matcher = SIARD_DK_PART_FOLDER_PATTERN.matcher(selectedFolder.getName());
    File parentFolder = selectedFolder.getParentFile();

    if (!matcher.matches() || parentFolder == null) {
      return FileUtils.sizeOfDirectory(selectedFolder);
    }

    String archivePrefix = matcher.group(1);
    File[] partFolders = parentFolder.listFiles(file -> {
      if (!file.isDirectory()) {
        return false;
      }
      Matcher siblingMatcher = SIARD_DK_PART_FOLDER_PATTERN.matcher(file.getName());
      return siblingMatcher.matches() && siblingMatcher.group(1).equals(archivePrefix);
    });

    if (partFolders == null || partFolders.length == 0) {
      return FileUtils.sizeOfDirectory(selectedFolder);
    }

    long totalSize = 0L;
    for (File partFolder : partFolders) {
      totalSize += FileUtils.sizeOfDirectory(partFolder);
    }
    return totalSize;
  }

}
