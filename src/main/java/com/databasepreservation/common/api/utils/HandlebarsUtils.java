/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.ViewerCelllUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.utils.FilenameUtils;
import com.databasepreservation.common.utils.LobManagerUtils;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class HandlebarsUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(HandlebarsUtils.class);

  private static final Handlebars handlebars = new Handlebars();

  public static List<String> getCellValues(ViewerRow row, Map<String, IterableIndexResult> nestedRows,
    TableStatus configTable, List<String> fieldsToReturn) {
    List<String> values = new ArrayList<>();
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_ID);
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_UUID);

    for (String solrColumnName : fieldsToReturn) {
      final ColumnStatus columnConfig = configTable.getColumnById(solrColumnName);

      if (columnConfig != null && ViewerType.dbTypes.NESTED.equals(columnConfig.getType())
        && columnConfig.getNestedColumns() != null) {
        // treat nested
        if (!row.getNestedRowList().isEmpty()) {
          String template = columnConfig.getExportStatus().getTemplateStatus().getTemplate();
          StringBuilder stringBuilder = new StringBuilder();
          boolean first = true;
          Iterator<ViewerRow> iterator = nestedRows.get(columnConfig.getNestedColumns().getReferenceUuid()).iterator();
          ViewerRow r;
          while (iterator.hasNext()) {
            r = iterator.next();
            if (template != null && !template.isEmpty()) {
              final Map<String, String> map = cellsToJson(r.getCells(), columnConfig.getNestedColumns());
              try {
                Template handlebarTemplate = handlebars.compileInline(template);
                if (!first) {
                  stringBuilder.append(", ");
                } else {
                  first = false;
                }
                stringBuilder.append(handlebarTemplate.apply(map));
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
          /*
           * row.getNestedRowList().forEach(nestedRow -> { if
           * (nestedRow.getNestedUUID().equals(solrColumnName)) { if (template != null &&
           * !template.isEmpty()) { final Map<String, String> map =
           * cellsToJson(nestedRow.getCells(), columnConfig.getNestedColumns());
           * Handlebars handlebars = new Handlebars(); try { Template handlebarTemplate =
           * handlebars.compileInline(template);
           * stringBuilder.append(handlebarTemplate.apply(map)); } catch (IOException e) {
           * e.printStackTrace(); } } } });
           */
          values.add(stringBuilder.toString());
        }
      } else {
        // treat non-nested
        if (row.getCells().get(solrColumnName) == null) {
          values.add("");
        } else {
          if (columnConfig != null) {
            final String applied = applyExportTemplate(row, configTable, columnConfig.getColumnIndex());
            if (StringUtils.isNotBlank(applied)) {
              if (columnConfig.getType().equals(ViewerType.dbTypes.BINARY)) {
                values.add(FilenameUtils.sanitizeFilename(applied));
              } else if (columnConfig.getSearchStatus().getList().isShowContent()) {
                values.add(row.getCells().get(solrColumnName).getValue());
              } else {
                values.add(applied);
              }
            } else {
              if (columnConfig.getType().equals(ViewerType.dbTypes.BINARY)) {
                values.add(LobManagerUtils.getDefaultFilename(row.getUuid()));
              } else {
                if (columnConfig.getType().equals(ViewerType.dbTypes.NUMERIC_FLOATING_POINT)) {
                  values.add(new BigDecimal(row.getCells().get(solrColumnName).getValue()).toPlainString());
                } else {
                  values.add(row.getCells().get(solrColumnName).getValue());
                }
              }
            }
          }
        }
      }
    }

    return values;
  }

  public static String applyMimeTypeTemplate(ViewerRow row, TableStatus tableConfiguration, int columnIndex) {
    Map<String, String> map = cellsToObject(row.getCells(), tableConfiguration, row.getUuid(), columnIndex);
    final String template = tableConfiguration.getColumnByIndex(columnIndex).getApplicationType();

    if (ViewerStringUtils.isBlank(template) || !template.equals(ViewerCelllUtils.getAutoDetectMimeTypeTemplate())) {
      return null;
    }

    try {
      Template handlebarTemplate = handlebars.compileInline(template);
      return handlebarTemplate.apply(map);
    } catch (IOException e) {
      throw new RESTException(e);
    }
  }

  public static String applyExportTemplate(ViewerRow row, TableStatus tableConfiguration, int columnIndex) {
    Map<String, String> map = cellsToObject(row.getCells(), tableConfiguration, row.getUuid(), columnIndex);
    final String template = tableConfiguration.getColumnByIndex(columnIndex).getExportStatus().getTemplateStatus()
      .getTemplate();

    if (ViewerStringUtils.isBlank(template)) {
      return null;
    }

    try {
      Template handlebarTemplate = handlebars.compileInline(template);
      return handlebarTemplate.apply(map);
    } catch (IOException e) {
      throw new RESTException(e);
    }
  }

  private static Map<String, String> cellsToObject(Map<String, ViewerCell> cells, TableStatus tableConfiguration,
    String rowIndex, int columnIndex) {
    Map<String, String> map = new HashMap<>();

    for (ColumnStatus column : tableConfiguration.getColumns()) {
      ViewerCell cell = cells.get(column.getId());

      if (cell != null) {
        map.put(ViewerStringUtils.replaceAllFor(column.getCustomName(), "\\s", "_"), cell.getValue());

        if (column.getType().equals(ViewerType.dbTypes.BINARY) && column.getColumnIndex() == columnIndex) {
          map.put(ViewerConstants.TEMPLATE_LOB_ROW_INDEX, rowIndex);

          map.put(ViewerConstants.TEMPLATE_LOB_COLUMN_INDEX, String.valueOf(column.getColumnIndex()));

          if (StringUtils.isNotBlank(cell.getMimeType())) {
            map.put(ViewerConstants.TEMPLATE_LOB_AUTO_DETECTED_MIME_TYPE, cell.getMimeType());
          }

          if (StringUtils.isNotBlank(cell.getFileExtension())) {
            map.put(ViewerConstants.TEMPLATE_LOB_AUTO_DETECTED_EXTENSION, cell.getFileExtension());
          }
        }
      }
    }

    return map;
  }

  private static Map<String, String> cellsToJson(Map<String, ViewerCell> cells, NestedColumnStatus nestedConfig) {
    final List<String> nestedFields = nestedConfig.getNestedFields();
    final List<String> nestedSolrNames = nestedConfig.getNestedSolrNames();
    int index = 0;

    Map<String, String> nestedValues = new HashMap<>();

    if (cells != null && !cells.isEmpty()) {
      for (String nestedField : nestedFields) {
        final String solrName = nestedSolrNames.get(index++);
        nestedValues.put(nestedField, cells.get(solrName).getValue());
      }
    }

    return nestedValues;
  }

  public static String applyVirtualColumnTemplate(ViewerRow row, TableStatus tableStatus, VirtualColumnStatus vcs) {

    // 1. Early exit if there is no template to process
    if (vcs == null || vcs.getTemplateStatus() == null || vcs.getTemplateStatus().getTemplate() == null) {
      return null;
    }

    String templateString = vcs.getTemplateStatus().getTemplate();
    Map<String, Object> templateContext = new HashMap<>();

    // 2. Build a rich context mapping for the Handlebars engine
    if (tableStatus.getColumns() != null && row.getCells() != null) {
      for (ColumnStatus col : tableStatus.getColumns()) {
        ViewerCell cell = row.getCells().get(col.getId());

        if (cell != null && cell.getValue() != null) {
          // A) Bind by Solr technical ID (e.g., "col0_s") - Guarantees backend native
          // compatibility
          templateContext.put(col.getId(), cell.getValue());

          // B) Bind by original database readable name (e.g., "actor_id") - Matches UI
          // input expectations
          if (col.getName() != null && !col.getName().trim().isEmpty()) {
            templateContext.put(col.getName(), cell.getValue());
          }
        }
      }
    }

    // 3. Compile and apply the template
    try {
      Template template = handlebars.compileInline(templateString);
      String result = template.apply(templateContext);

      // 4. Defensive Sanitization:
      // Handlebars returns "" for unresolved variables or empty evaluations.
      // We MUST convert blank strings to null to avoid crashing Solr's strict type
      // converters.
      if (result != null && result.trim().isEmpty()) {
        return null;
      }

      return result;

    } catch (Exception e) {
      // Log the error but do not crash the batch job.
      // A bad template from a user should just result in a null value for that row.
      LOGGER.warn("Failed to evaluate Handlebars template '{}'. Defaulting to null. Reason: {}", templateString,
        e.getMessage());
      return null;
    }
  }
}
