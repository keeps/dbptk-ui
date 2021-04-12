/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.utils;

import com.databasepreservation.common.api.common.ConsumesOutputStream;
import com.databasepreservation.common.api.common.EntityResponse;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class StreamResponse implements EntityResponse {
  private String filename;
  private String mediaType;
  private ConsumesOutputStream stream;

  public StreamResponse(ConsumesOutputStream stream) {
    this.filename = stream.getFileName();
    this.mediaType = stream.getMediaType();
    this.mediaType = stream.getMediaType();
    this.stream = stream;
  }

  public StreamResponse(String filename, String mediaType, ConsumesOutputStream stream) {
    super();
    this.filename = filename;
    this.mediaType = mediaType;
    this.stream = stream;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public ConsumesOutputStream getStream() {
    return stream;
  }

  public void setStream(ConsumesOutputStream stream) {
    this.stream = stream;
  }

}
