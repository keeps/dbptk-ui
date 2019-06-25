package com.databasepreservation.main.visualization.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import com.databasepreservation.main.common.shared.ViewerConstants;
import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.NotFoundException;

import com.databasepreservation.main.visualization.api.common.ConsumesOutputStream;

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

  public static StreamResponse getReportResourceStreamResponse(final Path filepath, InputStream inputStream)
    throws IOException, NotFoundException {
    StreamResponse streamResponse = null;

    String resourceId = filepath.getFileName().toString();
    String mimeType;
    if (resourceId.endsWith(".html")) {
      mimeType = ViewerConstants.MEDIA_TYPE_TEXT_HTML;
    } else if (resourceId.endsWith(".css")) {
      mimeType = "text/css";
    } else if (resourceId.endsWith(".md")) {
      // 2017-05-31 bferreira: according to
      // https://stackoverflow.com/a/10837005/1483200
      // "text/markdown; charset=UTF-8" may be needed
      mimeType = "text/markdown";
    } else if (resourceId.endsWith(".png")) {
      mimeType = "image/png";
    } else if (resourceId.endsWith(".js")) {
      mimeType = "text/javascript";
    } else {
      mimeType = ViewerConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM;
    }

    ConsumesOutputStream stream = new ConsumesOutputStream() {
      @Override
      public String getMediaType() {
        return mimeType;
      }

      @Override
      public String getFileName() {
        return resourceId;
      }

      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {
        IOUtils.copy(inputStream, out);
      }
    };

    streamResponse = new StreamResponse(resourceId, mimeType, stream);

    return streamResponse;
  }

}
