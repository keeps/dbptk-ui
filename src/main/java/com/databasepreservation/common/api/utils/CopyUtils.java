package com.databasepreservation.common.api.utils;

import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.BINARY;
import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.CLOB;
import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.NESTED;

import java.util.List;

import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class CopyUtils {
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
  private static final String NESTED_CELL_FIELDS_SEPARATOR = " , ";
  private static final String NESTED_CELL_ROWS_SEPARATOR = " ; ";

  public static void copyTableSearchResults(IndexResult<ViewerRow> results, List<ColumnStatus> columnsToCopy) {
    StringBuilder htmlSB = new StringBuilder();
    StringBuilder textSB = new StringBuilder();
    htmlSB.append(HTML_OPEN_TABLE);
    appendTableHeader(htmlSB, textSB, columnsToCopy);
    for (ViewerRow row : results.getResults()) {
      appendTableRow(htmlSB, textSB, row, columnsToCopy);
    }
    htmlSB.append(HTML_CLOSE_TABLE);
    JavascriptUtils.copyHTMLToClipboard(htmlSB.toString(), textSB.toString());
  }

  private static void appendTableHeader(StringBuilder htmlBuilder, StringBuilder textBuilder,
    List<ColumnStatus> columnsToCopy) {
    htmlBuilder.append(HTML_OPEN_ROW);
    boolean isFirstHeaderColumn = true;
    for (ColumnStatus configColumn : columnsToCopy) {
      if ((!NESTED.equals(configColumn.getType()) && !BINARY.equals(configColumn.getType())
        && !CLOB.equals(configColumn.getType()))
        || (NESTED.equals(configColumn.getType()) && !configColumn.getTypeName().contains("BINARY LARGE OBJECT"))) {
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
    }
    htmlBuilder.append(HTML_CLOSE_ROW);
  }

  private static void appendTableRow(StringBuilder htmlSB, StringBuilder textSB, ViewerRow row,
    List<ColumnStatus> columnsToCopy) {
    textSB.append(NEW_LINE);
    htmlSB.append(HTML_OPEN_ROW);
    boolean isFirstRowColumn = true;
    for (ColumnStatus columnStatus : columnsToCopy) {
      if (!NESTED.equals(columnStatus.getType())) {
        if (!BINARY.equals(columnStatus.getType()) && !CLOB.equals(columnStatus.getType())) {
          if (!isFirstRowColumn) {
            textSB.append(TAB);
          } else {
            isFirstRowColumn = false;
          }
          appendNativeColumn(htmlSB, textSB, row, columnStatus);
        }
      } else {
        if (!columnStatus.getTypeName().contains("BINARY LARGE OBJECT")) {
          if (!isFirstRowColumn) {
            textSB.append(TAB);
          } else {
            isFirstRowColumn = false;
          }
          appendNestedColumn(htmlSB, textSB, row, columnStatus);
        }
      }
    }
    htmlSB.append(HTML_CLOSE_ROW);
  }

  private static void appendNativeColumn(StringBuilder htmlSB, StringBuilder textSB, ViewerRow row,
    ColumnStatus columnStatus) {
    htmlSB.append(HTML_OPEN_CELL);
    if (row.getCells().containsKey(columnStatus.getId())) {
      htmlSB.append(SafeHtmlUtils.htmlEscape(row.getCells().get(columnStatus.getId()).getValue()));
      textSB.append(row.getCells().get(columnStatus.getId()).getValue());
    }
    htmlSB.append(HTML_CLOSE_CELL);
  }

  private static void appendNestedColumn(StringBuilder htmlSB, StringBuilder textSB, ViewerRow row,
    ColumnStatus columnStatus) {
    htmlSB.append(HTML_OPEN_CELL);
    boolean isFirstNestedRow = true;
    for (ViewerRow nestedRow : row.getNestedRowList()) {
      if (nestedRow.getNestedUUID().equals(columnStatus.getNestedColumns().getReferenceUuid())) {
        if (!isFirstNestedRow) {
          htmlSB.append(NESTED_CELL_ROWS_SEPARATOR);
          textSB.append(NESTED_CELL_ROWS_SEPARATOR);
        } else {
          isFirstNestedRow = false;
        }
        boolean isFirstNestedField = true;
        for (String nestedSolrName : columnStatus.getNestedColumns().getNestedSolrNames()) {
          String nestedKey = "nst_" + nestedSolrName;
          if (!isFirstNestedField) {
            htmlSB.append(NESTED_CELL_FIELDS_SEPARATOR);
            textSB.append(NESTED_CELL_FIELDS_SEPARATOR);
          } else {
            isFirstNestedField = false;
          }
          if (nestedRow.getCells().containsKey(nestedKey)) {
            htmlSB.append(nestedRow.getCells().get(nestedKey).getValue());
            textSB.append(nestedRow.getCells().get(nestedKey).getValue());
          }
        }
      }
    }
    htmlSB.append(HTML_CLOSE_CELL);
  }
}
