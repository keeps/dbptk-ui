/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.server.storage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Date;

import com.databasepreservation.common.api.common.ConsumesSkipableOutputStream;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */

public class BinaryConsumesOutputStream implements ConsumesSkipableOutputStream {
  private final Path path;
  private final long size;
  private final String filename;
  private  final String mediaType;

  public BinaryConsumesOutputStream(Path path, long size, String filename, String mediaType) {
    this.path = path;
    this.size = size;
    this.filename = filename;
    this.mediaType = mediaType;
  }

  @Override
  public void consumeOutputStream(OutputStream output) throws IOException {
    // TODO document why this method is empty
  }

  @Override
  public Date getLastModified() {
    return null;
  }


  @Override
  public long getSize() {
    return this.size;
  }

  @Override
  public void consumeOutputStream(OutputStream out, long from, long end) {
    try {
      File file = path.toFile();
      byte[] buffer = new byte[1024];
      try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
        long pos = from;
        randomAccessFile.seek(pos);
        while (pos < end) {
          randomAccessFile.read(buffer);
          out.write(buffer);
          pos += buffer.length;
        }
        out.flush();
      }
    } catch (IOException e) {
      // ignore
    }

  }

  @Override
  public String getFileName() {
    return this.filename;
  }

  @Override
  public String getMediaType() {
    return this.mediaType;
  }
}