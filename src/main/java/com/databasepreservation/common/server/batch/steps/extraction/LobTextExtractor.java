/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.steps.extraction;

import java.nio.file.Path;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface LobTextExtractor {
  String extractText(Path lobPath, ExtractionContext context) throws Exception;

  record ExtractionContext(String databaseUUID, String schemaName, String tableName, String rowUuid, int columnIndex,
    boolean isMultiFile, String fileName) {
  }
}
