package com.databasepreservation.common.api.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.utils.LobManagerUtils;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

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
          if (columnConfig != null) {
            final String applied = applyExportTemplate(row, configTable, columnConfig.getColumnIndex());
            if (StringUtils.isNotBlank(applied)) {
              values.add(applied);
            } else {
              final String value = row.getCells().get(solrColumnName).getValue();
              if (columnConfig.getType().equals(ViewerType.dbTypes.BINARY)) {
                values.add(LobManagerUtils.getDefaultFilename(row.getUuid()));
              } else {
                values.add(row.getCells().get(solrColumnName).getValue());
              }
            }
          }
        }
      }
    }

    return values;
  }

  public static String applyExportTemplate(ViewerRow row, TableStatus tableConfiguration, int columnIndex) {
    Map<String, String> map = cellsToObject(row.getCells(), tableConfiguration);
    final String template = tableConfiguration.getColumnByIndex(columnIndex).getExportStatus().getTemplateStatus()
      .getTemplate();

    if (ViewerStringUtils.isBlank(template)) {
      return null;
    }

    Handlebars handlebars = new Handlebars();
    try {
      Template handlebarTemplate = handlebars.compileInline(template);
      return handlebarTemplate.apply(map);
    } catch (IOException e) {
      throw new RESTException(e);
    }
  }

  private static Map<String, String> cellsToObject(Map<String, ViewerCell> cells, TableStatus tableConfiguration) {
    Map<String, String> map = new HashMap<>();

    for (ColumnStatus column : tableConfiguration.getColumns()) {
      if (cells.get(column.getId()) != null) {
        map.put(ViewerStringUtils.replaceAllFor(column.getCustomName(), "\\s", "_"),
          cells.get(column.getId()).getValue());
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
}
