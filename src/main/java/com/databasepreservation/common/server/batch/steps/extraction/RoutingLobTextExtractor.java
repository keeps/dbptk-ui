package com.databasepreservation.common.server.batch.steps.extraction;

import java.nio.file.Path;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composite strategy that routes the extraction request to either the
 * external service or the local Tika instance based on the file extension.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RoutingLobTextExtractor implements LobTextExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoutingLobTextExtractor.class);

  private final LobTextExtractor externalExtractor;
  private final LobTextExtractor localExtractor;
  private final Set<String> externalExtensions;

  public RoutingLobTextExtractor(LobTextExtractor externalExtractor, LobTextExtractor localExtractor,
    Set<String> externalExtensions) {
    this.externalExtractor = externalExtractor;
    this.localExtractor = localExtractor;
    this.externalExtensions = externalExtensions;
  }

  @Override
  public String extractText(Path lobPath, ExtractionContext context) throws Exception {
    String extension = getExtension(lobPath);

    if (externalExtractor != null && externalExtensions.contains(extension)) {
      LOGGER.debug("Routing LOB extraction to EXTERNAL service for extension: {}", extension);
      return externalExtractor.extractText(lobPath, context);
    } else {
      LOGGER.debug("Routing LOB extraction to LOCAL Tika for extension: {}", extension);
      return localExtractor.extractText(lobPath, context);
    }
  }

  private String getExtension(Path path) {
    String fileName = path.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
      return fileName.substring(dotIndex + 1).toLowerCase();
    }
    return "";
  }
}
