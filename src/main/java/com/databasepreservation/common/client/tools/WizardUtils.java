package com.databasepreservation.common.client.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.client.models.wizard.table.ColumnParameter;
import com.databasepreservation.common.client.models.wizard.table.ExternalLobParameter;
import com.databasepreservation.common.client.models.wizard.table.TableAndColumnsParameter;
import com.databasepreservation.common.client.models.wizard.table.TableAndColumnsParameters;
import com.databasepreservation.common.client.models.wizard.table.ViewAndColumnsParameter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class WizardUtils {

  public static TableAndColumnsParameters getTableAndColumnsParameter(ViewerMetadata metadata,
    Map<String, MultipleSelectionTablePanel<ViewerColumn>> columns,
    Map<String, ExternalLobParameter> externalLobParameterMap, Map<String, Boolean> viewMaterializationStatus) {

    TableAndColumnsParameters tableAndColumnsParameters = new TableAndColumnsParameters();
    Set<String> selectedSchemas = new HashSet<>();

    columns.forEach((key, value) -> {
      ViewerTable table = metadata.getTable(key);
      if (table != null) {
        TableAndColumnsParameter parameter = new TableAndColumnsParameter();
        parameter.setSchemaName(table.getSchemaName());
        parameter.setName(table.getName());

        value.getSelectionModel().getSelectedSet().forEach(column -> {
          ColumnParameter columnParameter = new ColumnParameter();
          columnParameter.setName(column.getDisplayName());
          columnParameter.setUseOnMerkle(true);
          columnParameter.setExternalLobParameter(getExternalLobParameter(table, column, externalLobParameterMap));

          parameter.getColumns().add(columnParameter);
        });

        tableAndColumnsParameters.getTableAndColumnsParameterMap().put(key, parameter);
        selectedSchemas.add(table.getSchemaName());
      } else {
        ViewerView view = metadata.getView(key);
        if (view != null) {
          ViewAndColumnsParameter parameter = new ViewAndColumnsParameter();
          parameter.setSchemaName(view.getSchemaName());
          parameter.setName(view.getName());
          parameter.setMaterialize(viewMaterializationStatus.get(view.getUuid()));

          value.getSelectionModel().getSelectedSet().forEach(column -> {
            ColumnParameter columnParameter = new ColumnParameter();
            columnParameter.setName(column.getDisplayName());
            columnParameter.setUseOnMerkle(true);

            parameter.getColumns().add(columnParameter);
          });

          tableAndColumnsParameters.getViewAndColumnsParameterMap().put(key, parameter);
          selectedSchemas.add(view.getSchemaName());
        }
      }
    });

    if (!externalLobParameterMap.isEmpty()) {
      tableAndColumnsParameters.setExternalLobConfigurationSet(true);
    }

    tableAndColumnsParameters.setSelectedSchemas(new ArrayList<>(selectedSchemas));

    return tableAndColumnsParameters;
  }

  private static ExternalLobParameter getExternalLobParameter(ViewerTable table, ViewerColumn column,
    Map<String, ExternalLobParameter> externalLobParameterMap) {
    String id = column.getDisplayName() + "_" + table.getUuid();
    return externalLobParameterMap.get(id);
  }
}
