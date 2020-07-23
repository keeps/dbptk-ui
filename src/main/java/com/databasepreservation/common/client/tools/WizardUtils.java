package com.databasepreservation.common.client.tools;

import java.util.Map;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
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
    Map<String, ExternalLobParameter> externalLobParameterMap, Map<String, Boolean> viewMaterializationStatus,
    Map<String, Boolean> merkleTreeColumnStatus, ViewerMetadata metadata) {

    TableAndColumnsParameters tableAndColumnsParameters = new TableAndColumnsParameters();

    tables.forEach((schemaUUID, value) -> {
      value.getSelectionModel().getSelectedSet().forEach(table -> {
        TableAndColumnsParameter parameter = new TableAndColumnsParameter();
        parameter.setSchemaName(table.getSchemaName());
        parameter.setName(table.getName());
        getColumnParameter(columns, externalLobParameterMap, merkleTreeColumnStatus, parameter, table.getUuid());

        tableAndColumnsParameters.getTableAndColumnsParameterMap().put(table.getUuid(), parameter);
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
      });
    });

    if (!externalLobParameterMap.isEmpty()) {
      tableAndColumnsParameters.setExternalLobConfigurationSet(true);
    }

    tableAndColumnsParameters.setSelectedSchemas(
      metadata.getSchemas().stream().distinct().map(ViewerSchema::getName).collect(Collectors.toList()));

    return tableAndColumnsParameters;
  }

  private static void getColumnParameter(Map<String, MultipleSelectionTablePanel<ViewerColumn>> columns,
    Map<String, ExternalLobParameter> externalLobParameterMap, Map<String, Boolean> merkleTreeColumnStatus,
    TableAndColumnsParameter parameter, String uuid) {
    columns.get(uuid).getSelectionModel().getSelectedSet().forEach(column -> {
      ColumnParameter columnParameter = new ColumnParameter();
      columnParameter.setName(column.getDisplayName());
      columnParameter.setUseOnMerkle(merkleTreeColumnStatus.get(generateMerkleTreeMapKey(uuid, column.getDisplayName())));
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
