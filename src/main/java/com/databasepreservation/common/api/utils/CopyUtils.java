/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.utils;

import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.BINARY;
import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.NESTED;
import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.NUMERIC_FLOATING_POINT;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class CopyUtils {
  private CopyUtils() {
    /* Do nothing */
  }

  private static final String HTML_OPEN_TABLE = "<table>";
  private static final String HTML_CLOSE_TABLE = "</table>";
  private static final String HTML_OPEN_ROW = "<tr>";
  private static final String HTML_CLOSE_ROW = "</tr>";
  private static final String HTML_OPEN_HEADER = "<th>";
  private static final String HTML_CLOSE_HEADER = "</th>";
  private static final String HTML_OPEN_CELL = "<td>";
  private static final String HTML_CLOSE_CELL = "</td>";
  private static final String TAB = "\t";
  private static final String NEW_LINE = "\n";
  private static final String NESTED_CELL_ROWS_SEPARATOR = ", ";

  public static void copyTableSearchResults(IndexResult<ViewerRow> results, TableStatus tableStatus) {
    List<ColumnStatus> columnsToCopy = tableStatus.getVisibleColumnsList();
    StringBuilder htmlSB = new StringBuilder();
    StringBuilder textSB = new StringBuilder();
    htmlSB.append(HTML_OPEN_TABLE);
    appendTableHeader(htmlSB, textSB, columnsToCopy);
    for (ViewerRow row : results.getResults()) {
      appendTableRow(htmlSB, textSB, row, tableStatus, columnsToCopy);
    }
    htmlSB.append(HTML_CLOSE_TABLE);
    JavascriptUtils.copyHTMLToClipboard(htmlSB.toString(), textSB.toString());
  }

  private static void appendTableHeader(StringBuilder htmlBuilder, StringBuilder textBuilder,
    List<ColumnStatus> columnsToCopy) {
    htmlBuilder.append(HTML_OPEN_ROW);
    boolean isFirstHeaderColumn = true;
    for (ColumnStatus configColumn : columnsToCopy) {
      htmlBuilder.append(HTML_OPEN_HEADER);
      if (!isFirstHeaderColumn) {
        textBuilder.append(TAB);
      } else {
        isFirstHeaderColumn = false;
      }
      htmlBuilder.append(SafeHtmlUtils.htmlEscape(configColumn.getCustomName()));
      textBuilder.append(configColumn.getCustomName());
      htmlBuilder.append(HTML_CLOSE_HEADER);
    }
    htmlBuilder.append(HTML_CLOSE_ROW);
  }

  private static void appendTableRow(StringBuilder htmlSB, StringBuilder textSB, ViewerRow row, TableStatus tableStatus,
    List<ColumnStatus> columnsToCopy) {
    textSB.append(NEW_LINE);
    htmlSB.append(HTML_OPEN_ROW);
    boolean isFirstRowColumn = true;
    Map<String, String> cellData = cellsToObject(row.getCells(), tableStatus);
    for (ColumnStatus columnStatus : columnsToCopy) {
      if (!isFirstRowColumn) {
        textSB.append(TAB);
      } else {
        isFirstRowColumn = false;
      }
      String value;
      if (NESTED.equals(columnStatus.getType()) && columnStatus.getNestedColumns() != null) {
        value = getNestedColumnValue(row, columnStatus);
      } else {
        value = getNativeColumnValue(row, cellData, columnStatus);
      }
      htmlSB.append(HTML_OPEN_CELL).append(SafeHtmlUtils.htmlEscape(value)).append(HTML_CLOSE_CELL);
      textSB.append(value);
    }
    htmlSB.append(HTML_CLOSE_ROW);
  }

  private static String getNativeColumnValue(ViewerRow row, Map<String, String> cellData, ColumnStatus columnStatus) {
    ViewerCell cell = row.getCells().get(columnStatus.getId());
    if (cell == null) {
      return "";
    }

    String template = columnStatus.getExportStatus().getTemplateStatus().getTemplate();
    String applied = null;
    if (!ViewerStringUtils.isBlank(template)) {
      if (BINARY.equals(columnStatus.getType())) {
        applied = applyTemplate(template, withLobData(cellData, row, cell, columnStatus));
      } else {
        applied = applyTemplate(template, cellData);
      }
    }
    if (!ViewerStringUtils.isBlank(applied)) {
      if (BINARY.equals(columnStatus.getType())) {
        return sanitizeFilename(applied);
      } else if (columnStatus.getSearchStatus().getList().isShowContent()) {
        return cell.getValue();
      }
      return applied;
    }

    if (BINARY.equals(columnStatus.getType())) {
      return ViewerConstants.SIARD_RECORD_PREFIX + row.getUuid() + ViewerConstants.SIARD_LOB_FILE_EXTENSION;
    }
    if (NUMERIC_FLOATING_POINT.equals(columnStatus.getType())) {
      try {
        return new BigDecimal(cell.getValue()).toPlainString();
      } catch (NumberFormatException e) {
        return cell.getValue();
      }
    }
    return cell.getValue();
  }

  private static String getNestedColumnValue(ViewerRow row, ColumnStatus columnStatus) {
    String template = columnStatus.getExportStatus().getTemplateStatus().getTemplate();
    if (ViewerStringUtils.isBlank(template) || row.getNestedRowList() == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    boolean isFirstNestedRow = true;
    for (ViewerRow nestedRow : row.getNestedRowList()) {
      if (nestedRow.getNestedUUID().equals(columnStatus.getNestedColumns().getReferenceUuid())) {
        String applied = applyTemplate(template,
          nestedCellsToObject(nestedRow.getCells(), columnStatus.getNestedColumns()));
        if (!isFirstNestedRow) {
          sb.append(NESTED_CELL_ROWS_SEPARATOR);
        } else {
          isFirstNestedRow = false;
        }
        if (applied != null) {
          sb.append(applied);
        }
      }
    }
    return sb.toString();
  }

  private static String applyTemplate(String template, Map<String, String> cellData) {
    if (ViewerStringUtils.isBlank(template)) {
      return null;
    }
    String[] cellDataArray = new String[cellData.size() * 2];
    int i = 0;
    for (Map.Entry<String, String> entry : cellData.entrySet()) {
      cellDataArray[i++] = entry.getKey();
      cellDataArray[i++] = entry.getValue() != null ? entry.getValue() : "";
    }
    return JavascriptUtils.compileTemplate(template, JSOUtils.cellsToJson(cellDataArray));
  }

  /**
   * {@link com.databasepreservation.common.utils.FilenameUtils#sanitizeFilename(String)}
   */
  private static String sanitizeFilename(String name) {
    return name.replaceAll("[:\\\\/*?|<>]", "_");
  }

  /**
   * {@link com.databasepreservation.common.api.utils.HandlebarsUtils#cellsToObject(Map, TableStatus, String, int)}
   */
  private static Map<String, String> cellsToObject(Map<String, ViewerCell> cells, TableStatus tableStatus) {
    Map<String, String> map = new HashMap<>();
    for (ColumnStatus column : tableStatus.getColumns()) {
      ViewerCell cell = cells.get(column.getId());
      if (cell != null) {
        map.put(ViewerStringUtils.replaceAllFor(column.getCustomName(), "\\s", "_"), cell.getValue());
      }
    }
    return map;
  }

  private static Map<String, String> nestedCellsToObject(Map<String, ViewerCell> nestedRowCells,
    NestedColumnStatus nestedColumnStatus) {
    Map<String, String> map = new HashMap<>();
    if (nestedRowCells != null && !nestedRowCells.isEmpty()) {
      List<String> nestedFields = nestedColumnStatus.getNestedFields();
      List<String> nestedSolrNames = nestedColumnStatus.getNestedSolrNames();
      for (int i = 0; i < nestedFields.size(); i++) {
        ViewerCell cell = nestedRowCells.get("nst_" + nestedSolrNames.get(i));
        map.put(nestedFields.get(i), cell != null ? cell.getValue() : "");
      }
    }
    return map;
  }

  /**
   * Copy of the row's cell data with the LOB placeholders of the given binary
   * column, which only that column's export template can use.
   */
  private static Map<String, String> withLobData(Map<String, String> cellData, ViewerRow row, ViewerCell cell,
    ColumnStatus binaryColumn) {
    Map<String, String> map = new HashMap<>(cellData);
    map.put(ViewerConstants.TEMPLATE_LOB_ROW_INDEX, row.getUuid());
    map.put(ViewerConstants.TEMPLATE_LOB_COLUMN_INDEX, String.valueOf(binaryColumn.getColumnIndex()));
    if (!ViewerStringUtils.isBlank(cell.getMimeType())) {
      map.put(ViewerConstants.TEMPLATE_LOB_AUTO_DETECTED_MIME_TYPE, cell.getMimeType());
    }
    if (!ViewerStringUtils.isBlank(cell.getFileExtension())) {
      map.put(ViewerConstants.TEMPLATE_LOB_AUTO_DETECTED_EXTENSION, cell.getFileExtension());
    }
    return map;
  }
}
