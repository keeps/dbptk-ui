package com.databasepreservation.common.server.batch.steps.extraction;

import java.nio.file.Path;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface LobTextExtractor {
  String extractText(Path lobPath, ExtractionContext context) throws Exception;

  record ExtractionContext(String databaseUUID, String schemaName, String tableName, String rowUuid, int columnIndex) {
  }
}
