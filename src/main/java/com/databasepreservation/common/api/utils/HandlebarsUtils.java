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

  public static List<String> getRowValues(ViewerRow row,
    TableStatus configTable, List<String> fieldsToReturn) {
    List<String> values = new ArrayList<>();
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_ID);
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_UUID);

    for (String solrColumnName : fieldsToReturn) {
      ColumnStatus columnConfig = configTable.getColumnById(solrColumnName);
      values.addAll(getColumnValues(row, configTable, solrColumnName, columnConfig));
    }

    return values;
  }

  private static List<String> getColumnValues(ViewerRow row, TableStatus tableStatus, String solrColumnName,
    ColumnStatus columnStatus) {
    List<String> values = new ArrayList<>();

    if (columnStatus != null && ViewerType.dbTypes.NESTED.equals(columnStatus.getType())
      && columnStatus.getNestedColumns() != null) {
      values.addAll(getNestedColumnValues(row, columnStatus));
    } else {
      values.addAll(getNativeColumnValues(row, tableStatus, solrColumnName, columnStatus));
    }

    return values;
  }

  private static List<String> getNativeColumnValues(ViewerRow row, TableStatus tableStatus, String solrColumnName,
    ColumnStatus columnStatus) {
    ArrayList<String> values = new ArrayList<>();

    // treat non-nested
    if (row.getCells().get(solrColumnName) == null) {
      values.add("");
    } else {
      if (columnStatus != null) {
        final String applied = applyExportTemplate(row, tableStatus, columnStatus.getColumnIndex());
        if (StringUtils.isNotBlank(applied)) {
          if (columnStatus.getType().equals(ViewerType.dbTypes.BINARY)) {
            values.add(FilenameUtils.sanitizeFilename(applied));
          } else if (columnStatus.getSearchStatus().getList().isShowContent()) {
            values.add(row.getCells().get(solrColumnName).getValue());
          } else {
            values.add(applied);
          }
        } else {
          if (columnStatus.getType().equals(ViewerType.dbTypes.BINARY)) {
            values.add(LobManagerUtils.getDefaultFilename(row.getUuid()));
          } else {
            if (columnStatus.getType().equals(ViewerType.dbTypes.NUMERIC_FLOATING_POINT)) {
              values.add(new BigDecimal(row.getCells().get(solrColumnName).getValue()).toPlainString());
            } else {
              values.add(row.getCells().get(solrColumnName).getValue());
            }
          }
        }
      }
    }

    return values;
  }

  private static List<String> getNestedColumnValues(ViewerRow row, ColumnStatus columnStatus) {

    ArrayList<String> values = new ArrayList<>();
    StringBuilder stringBuilder = new StringBuilder();

    String template = columnStatus.getExportStatus().getTemplateStatus().getTemplate();
    if (template != null && !template.isEmpty()) {
      boolean first = true;
      for (ViewerRow nestedRow : row.getNestedRowList()) {
        if (nestedRow.getNestedUUID().equals(columnStatus.getNestedColumns().getReferenceUuid())) {
          final Map<String, String> map = cellsToJson(nestedRow.getCells(), columnStatus.getNestedColumns());
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
    }
    values.add(stringBuilder.toString());

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

  private static Map<String, String> cellsToJson(Map<String, ViewerCell> nestedRowCells,
    NestedColumnStatus nestedColumnStatus) {
    final List<String> columnFields = nestedColumnStatus.getNestedFields();
    final List<String> columnSolrNames = nestedColumnStatus.getNestedSolrNames();
    int index = 0;

    Map<String, String> nestedValues = new HashMap<>();

    if (nestedRowCells != null && !nestedRowCells.isEmpty()) {
      for (String nestedField : columnFields) {
        final String nestedRowSolrName = "nst_" + columnSolrNames.get(index++);
        if (nestedRowCells.containsKey(nestedRowSolrName)) {
          nestedValues.put(nestedField, nestedRowCells.get(nestedRowSolrName).getValue());
        } else {
          nestedValues.put(nestedField, "");
        }
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
