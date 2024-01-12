/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import org.roda.core.data.exceptions.AlreadyExistsException;

import com.databasepreservation.common.server.storage.ContentPayload;
import com.databasepreservation.common.server.storage.fs.FSPathContentPayload;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class Browser {
  private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);
  public static void createFile(InputStream uploadedInputStream, String fileName, Path path)
          throws AlreadyExistsException, GenericException {
    try{

      Path file = Files.createTempFile("siard", ".tmp");
      Files.copy(uploadedInputStream, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      payload.writeToPath(path);
      LOGGER.info("Created file {} in {}", fileName, path);
    } catch (FileAlreadyExistsException e){
      System.out.println("File exist on path: " + Paths.get(path.toString(), fileName));
      throw new AlreadyExistsException();
    } catch (IOException e){
      throw new GenericException("Cannot create file", e);
    }

  }
}
