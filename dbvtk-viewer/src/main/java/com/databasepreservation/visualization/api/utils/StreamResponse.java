/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.api.utils;

import javax.ws.rs.core.StreamingOutput;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class StreamResponse {
  private String filename;
  private String mediaType;
  private StreamingOutput stream;

  public StreamResponse(String filename, String mediaType, StreamingOutput stream) {
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

  public StreamingOutput getStream() {
    return stream;
  }

  public void setStream(StreamingOutput stream) {
    this.stream = stream;
  }
}
