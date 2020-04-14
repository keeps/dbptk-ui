package com.databasepreservation.common.client.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
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

  public static TableAndColumnsParameters getTableAndColumnsParameter(
    Map<String, MultipleSelectionTablePanel<ViewerTable>> tables,
    Map<String, MultipleSelectionTablePanel<ViewerView>> views,
    Map<String, MultipleSelectionTablePanel<ViewerColumn>> columns,
    Map<String, ExternalLobParameter> externalLobParameterMap,
    Map<String, Boolean> viewMaterializationStatus,
    Map<String, Boolean> merkleTreeColumnStatus) {

    TableAndColumnsParameters tableAndColumnsParameters = new TableAndColumnsParameters();
    Set<String> selectedSchemas = new HashSet<>();

    tables.forEach((schemaUUID, value) -> {
      value.getSelectionModel().getSelectedSet().forEach(table -> {
        TableAndColumnsParameter parameter = new TableAndColumnsParameter();
        parameter.setSchemaName(table.getSchemaName());
        parameter.setName(table.getName());
        getColumnParameter(columns, externalLobParameterMap, merkleTreeColumnStatus, parameter, table.getUuid());

        tableAndColumnsParameters.getTableAndColumnsParameterMap().put(table.getUuid(), parameter);
        selectedSchemas.add(table.getSchemaName());
      });
    });

    views.forEach((schemaUUID, value) -> {
      value.getSelectionModel().getSelectedSet().forEach(view -> {
        ViewAndColumnsParameter parameter = new ViewAndColumnsParameter();
        parameter.setSchemaName(view.getSchemaName());
        parameter.setName(view.getName());
        parameter.setMaterialize(viewMaterializationStatus.get(view.getUuid()));

        getColumnParameter(columns, externalLobParameterMap, merkleTreeColumnStatus, parameter, view.getUuid());

        tableAndColumnsParameters.getViewAndColumnsParameterMap().put(view.getUuid(), parameter);
        selectedSchemas.add(view.getSchemaName());
      });
    });

    if (!externalLobParameterMap.isEmpty()) {
      tableAndColumnsParameters.setExternalLobConfigurationSet(true);
    }

    tableAndColumnsParameters.setSelectedSchemas(new ArrayList<>(selectedSchemas));

    return tableAndColumnsParameters;
  }

  private static void getColumnParameter(Map<String, MultipleSelectionTablePanel<ViewerColumn>> columns,
    Map<String, ExternalLobParameter> externalLobParameterMap, Map<String, Boolean> merkleTreeColumnStatus, TableAndColumnsParameter parameter, String uuid) {
    columns.get(uuid).getSelectionModel().getSelectedSet().forEach(column -> {
      ColumnParameter columnParameter = new ColumnParameter();
      columnParameter.setName(column.getDisplayName());
      columnParameter.setUseOnMerkle(merkleTreeColumnStatus.get(generateMerkleTreeMapKey(uuid, column.getSolrName())));
      columnParameter.setExternalLobParameter(getExternalLobParameter(uuid, column, externalLobParameterMap));

      parameter.getColumns().add(columnParameter);
    });
  }

  private static ExternalLobParameter getExternalLobParameter(String uuid, ViewerColumn column,
    Map<String, ExternalLobParameter> externalLobParameterMap) {
    String id = column.getDisplayName() + "_" + uuid;
    return externalLobParameterMap.get(id);
  }

  public static String generateMerkleTreeMapKey(String uuid, String solr) {
    return uuid + "_" + solr;
  }
}
