package com.databasepreservation.visualization.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.databasepreservation.visualization.api.common.ConsumesOutputStream;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DownloadUtils {
  public static ConsumesOutputStream stream(final InputStream input) {
    return new ConsumesOutputStream() {
      @Override
      public void consumeOutputStream(OutputStream output) throws IOException {
        IOUtils.copy(input, output);
        IOUtils.closeQuietly(input);
        IOUtils.closeQuietly(output);
      }

      @Override
      public String getFileName() {
        return null;
      }

      @Override
      public String getMediaType() {
        return null;
      }
    };
  }
}
