package com.databasepreservation.visualization.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DownloadUtils {
  public static StreamingOutput stream(final InputStream input) {
    return new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        IOUtils.copy(input, output);
        IOUtils.closeQuietly(input);
      }
    };
  }
}
