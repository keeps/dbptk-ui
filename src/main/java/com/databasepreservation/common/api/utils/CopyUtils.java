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
  private static final String HTML_OPEN_ROW = "<tr>";
  private static final String HTML_CLOSE_ROW = "</tr>";
  private static final String HTML_OPEN_HEADER = "<th>";
  private static final String HTML_CLOSE_HEADER = "</th>";
  private static final String TAB = "\t";

  public static void copyTableSearchResults(IndexResult<ViewerRow> results, List<ColumnStatus> columnsToCopy) {
    StringBuilder htmlSB = new StringBuilder();
    StringBuilder textSB = new StringBuilder();
    htmlSB.append(HTML_OPEN_TABLE);
    appendTableHeader(htmlSB, textSB, columnsToCopy);
    for (ViewerRow row : results.getResults()) {
      appendTableRow(htmlSB, textSB, row, columnsToCopy);
    }
    htmlSB.append("</table>");
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
    textSB.append("\n");
    htmlSB.append("<tr>");
    boolean isFirstRowColumn = true;
    for (ColumnStatus configColumn : columnsToCopy) {
      if (!NESTED.equals(configColumn.getType())) {
        // Treat as non nested
        if (!BINARY.equals(configColumn.getType()) && !CLOB.equals(configColumn.getType())) {
          if (!isFirstRowColumn) {
            textSB.append("\t");
          } else {
            isFirstRowColumn = false;
          }
          htmlSB.append("<td>");
          if (row.getCells().containsKey(configColumn.getId())) {
            htmlSB.append(SafeHtmlUtils.htmlEscape(row.getCells().get(configColumn.getId()).getValue()));
            textSB.append(row.getCells().get(configColumn.getId()).getValue());
          }
          htmlSB.append("</td>");
        }
      } else {
        if (!configColumn.getTypeName().contains("BINARY LARGE OBJECT")) {
          if (!isFirstRowColumn) {
            textSB.append("\t");
          } else {
            isFirstRowColumn = false;
          }
          htmlSB.append("<td>");
          boolean isFirstNestedName = true;
          for (String nestedSolrName : configColumn.getNestedColumns().getNestedSolrNames()) {
            String nestedKey = "nst_" + nestedSolrName;
            String nestedTable = configColumn.getNestedColumns().getOriginalTable();
            if (!isFirstNestedName) {
              htmlSB.append(", ");
              textSB.append(", ");
            } else {
              isFirstNestedName = false;
            }
            boolean isFirstNestedRow = true;
            for (ViewerRow nestedRow : row.getNestedRowList()) {
              if (nestedRow.getCells().containsKey(nestedKey) && nestedRow.getNestedTableId().equals(nestedTable)) {
                if (!isFirstNestedRow) {
                  htmlSB.append(" ");
                  textSB.append(" ");
                } else {
                  isFirstNestedRow = false;
                }
                htmlSB.append(nestedRow.getCells().get(nestedKey).getValue());
                textSB.append(nestedRow.getCells().get(nestedKey).getValue());
              }
            }
          }
          htmlSB.append("</td>");
        }
      }
    }
    htmlSB.append("</tr>");
  }
}
