/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.utils;

import java.util.Date;

import com.databasepreservation.common.api.common.ConsumesOutputStream;
import com.databasepreservation.common.api.common.EntityResponse;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class StreamResponse implements EntityResponse {
  private String filename;
  private String mediaType;
  private long fileSize = -1;
  private Date lastModified;
  private ConsumesOutputStream stream;

  public StreamResponse(ConsumesOutputStream stream) {
    this.filename = stream.getFileName();
    this.fileSize = stream.getSize();
    this.mediaType = stream.getMediaType();
    this.lastModified = stream.getLastModified();
    this.stream = stream;
  }

  public StreamResponse(String filename, String mediaType, ConsumesOutputStream stream) {
    super();
    this.filename = filename;
    this.mediaType = mediaType;
    this.stream = stream;
    this.lastModified = stream.getLastModified();
    this.fileSize = stream.getSize();
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

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }
}
