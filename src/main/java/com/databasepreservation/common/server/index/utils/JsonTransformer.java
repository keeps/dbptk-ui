package com.databasepreservation.common.server.index.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.exceptions.ViewerException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class JsonTransformer {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonTransformer.class);

  public static <T> T readObjectFromFile(Path jsonFile, Class<T> objectClass) throws ViewerException {
    try (InputStream stream = Files.newInputStream(jsonFile)) {
      return getObjectFromJson(stream, objectClass);
    } catch (IOException e) {
      throw new ViewerException(e);
    }
  }

  public static void writeObjectToFile(Object object, Path file) throws ViewerException {
      try {
        String json = getJsonFromObject(object);
        if (json != null) {
          Files.createDirectories(file.getParent());
          Files.write(file, json.getBytes());
        }
      } catch (IOException e) {
        throw new ViewerException("Error writing object, as json, to file", e);
      }
  }


  public static String getJsonFromObject(Object object) throws ViewerException {
    String ret = null;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.writeValueAsString(object);
    } catch (IOException e) {
      throw new ViewerException("Error transforming object '" + object + "' to json string", e);
    }
    return ret;
  }

  public static <T> T getObjectFromJson(InputStream json, Class<T> objectClass) throws ViewerException {
    T ret;
    try {
      String jsonString = IOUtils.toString(json, RodaConstants.DEFAULT_ENCODING);
      ret = getObjectFromJson(jsonString, objectClass);
    } catch (IOException e) {
      throw new ViewerException(e);
    } finally {
      IOUtils.closeQuietly(json);
    }
    return ret;
  }

  public static <T> T getObjectFromJson(String json, Class<T> objectClass) throws ViewerException {
    if (json == null) {
      return null;
    }

    T ret;
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ret = mapper.readValue(json, objectClass);
    } catch (IOException e) {
      throw new ViewerException("Error while parsing JSON", e);
    }
    return ret;
  }
}
