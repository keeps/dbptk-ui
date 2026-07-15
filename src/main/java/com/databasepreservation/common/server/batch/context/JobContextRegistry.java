/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class JobContextRegistry {
  private final Map<String, JobContext> registry = new ConcurrentHashMap<>();

  public void register(String databaseUUID, JobContext context) {
    if (databaseUUID != null && context != null) {
      registry.put(databaseUUID, context);
    }
  }

  public JobContext get(String databaseUUID) {
    return registry.get(databaseUUID);
  }

  public void unregister(String databaseUUID) {
    if (databaseUUID != null) {
      registry.remove(databaseUUID);
    }
  }
}
