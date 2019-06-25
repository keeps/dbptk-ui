package com.databasepreservation.main.common.server.index.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.main.common.shared.exceptions.ViewerException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class JsonTransformer {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonTransformer.class);

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
