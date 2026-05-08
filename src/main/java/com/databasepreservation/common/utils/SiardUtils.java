package com.databasepreservation.common.utils;

import com.databasepreservation.model.exception.SIARDVersionNotSupportedException;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

import static com.databasepreservation.common.client.ViewerConstants.SIARD_V22;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_V21;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_128;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_128_EXT;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_1007;
import static com.databasepreservation.common.client.ViewerConstants.SIARD_DK_1007_EXT;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class SiardUtils {

  public static Long calculateSize(Path siardPath, String siardVersion) throws SIARDVersionNotSupportedException {
    if (siardPath == null || !siardPath.toFile().exists()) {
      return 0L;
    }

    if (siardVersion.equals(SIARD_DK_128) || siardVersion.equals(SIARD_DK_1007) || siardVersion.equals(SIARD_DK_128_EXT)
      || siardVersion.equals(SIARD_DK_1007_EXT)) {
      return FileUtils.sizeOfDirectory(siardPath.toFile());
    } else if (siardVersion.equals(SIARD_V22) || siardVersion.equals(SIARD_V21)) {
      return siardPath.toFile().length();
    } else {
      throw new SIARDVersionNotSupportedException();
    }
  }

}
