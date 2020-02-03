package com.databasepreservation.common.api.utils;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class HandlebarsUtils {

  public static List<String> getCellValues(ViewerRow row, TableStatus configTable, List<String> fieldsToReturn) {
    List<String> values = new ArrayList<>();
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_ID);
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_UUID);

    for (String solrColumnName : fieldsToReturn) {
      final ColumnStatus columnConfig = configTable.getColumnById(solrColumnName);

      if (columnConfig != null && columnConfig.getType().equals(ViewerType.dbTypes.NESTED)) {
        // treat nested
        if (!row.getNestedRowList().isEmpty()) {
          String template = columnConfig.getExportStatus().getTemplateStatus().getTemplate();
          StringBuilder stringBuilder = new StringBuilder();
          row.getNestedRowList().forEach(nestedRow -> {
            if (nestedRow.getNestedUUID().equals(solrColumnName)) {
              if (template != null && !template.isEmpty()) {
                final Map<String, String> map = cellsToJson(nestedRow.getCells(), columnConfig.getNestedColumns());
                Handlebars handlebars = new Handlebars();
                try {
                  Template handlebarTemplate = handlebars.compileInline(template);
                  stringBuilder.append(handlebarTemplate.apply(map));
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
          });
          values.add(stringBuilder.toString());
        }
      } else {
        // treat non-nested
        if (row.getCells().get(solrColumnName) == null) {
          values.add("");
        } else {
          values.add(row.getCells().get(solrColumnName).getValue());
        }
      }
    }

    return values;
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
}
