package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.BinaryColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ClobColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.NestedColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.NumericColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual.VirtualColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual.VirtualReferenceOptionsPanel;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.formatters.Formatter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerType;

import config.i18n.client.ClientMessages;

/**
 * Registry Factory that encapsulates the creation and callback logic for Column
 * Options Dialogs.
 */
public class ColumnDialogFactory {

  public interface DialogHandler {
    void open(ViewerDatabase database, CollectionStatus collectionStatus, TableStatus tableStatus,
      ColumnStatus columnStatus, ClientMessages messages, Consumer<Boolean> saveCallback);
  }

  private static final Map<ViewerType.dbTypes, DialogHandler> handlers = new HashMap<>();
  private static final DialogHandler VIRTUAL_HANDLER = createVirtualHandler();
  private static final DialogHandler DEFAULT_HANDLER = createStandardHandler();

  static {
    handlers.put(ViewerType.dbTypes.NESTED, createNestedHandler());
    handlers.put(ViewerType.dbTypes.CLOB, createClobHandler());
    handlers.put(ViewerType.dbTypes.BINARY, createBinaryHandler());
    handlers.put(ViewerType.dbTypes.NUMERIC_FLOATING_POINT, createNumericHandler());
  }

  // Prevents instantiation
  private ColumnDialogFactory() {
  }

  public static void openDialog(ViewerDatabase database, CollectionStatus collectionStatus, TableStatus tableStatus,
    ColumnStatus columnStatus, ClientMessages messages, Consumer<Boolean> saveCallback) {
    if (columnStatus.isVirtual()) {
      VIRTUAL_HANDLER.open(database, collectionStatus, tableStatus, columnStatus, messages, saveCallback);
      return;
    }

    DialogHandler handler = handlers.getOrDefault(columnStatus.getType(), DEFAULT_HANDLER);
    handler.open(database, collectionStatus, tableStatus, columnStatus, messages, saveCallback);
  }

  // --- Handlers Configuration ---

  private static DialogHandler createVirtualHandler() {
    return (database, collectionStatus, tableStatus, columnStatus, messages, saveCallback) -> {
      VirtualColumnOptionsPanel colPanel = VirtualColumnOptionsPanel.createInstance(tableStatus, columnStatus);
      ForeignKeysStatus fkStatus = collectionStatus.getForeignKeyByTableAndColumnId(tableStatus.getUuid(),
        columnStatus.getId());
      VirtualReferenceOptionsPanel refPanel = VirtualReferenceOptionsPanel.createInstance(database, collectionStatus,
        tableStatus, columnStatus, fkStatus);

      Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), "600px", messages.basicActionSave(),
        messages.basicActionCancel(), messages.basicActionDelete(), Arrays.asList(colPanel, refPanel),
        new DefaultAsyncCallback<Dialogs.DialogAction>() {
          @Override
          public void onSuccess(Dialogs.DialogAction action) {
            processVirtualColumnSaveAction(action, tableStatus, columnStatus, colPanel, refPanel, fkStatus,
              collectionStatus, saveCallback);
          }
        });
    };
  }

  private static DialogHandler createNestedHandler() {
    return (database, collectionStatus, tableStatus, columnStatus, messages, saveCallback) -> {
      NestedColumnOptionsPanel panel = (NestedColumnOptionsPanel) NestedColumnOptionsPanel.createInstance(columnStatus);
      Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), messages.basicActionSave(),
        messages.basicActionCancel(), panel, new DefaultAsyncCallback<Dialogs.DialogAction>() {
          @Override
          public void onSuccess(Dialogs.DialogAction action) {
            processNestedColumnSaveAction(action, tableStatus, columnStatus, panel, saveCallback);
          }
        });
    };
  }

  private static DialogHandler createClobHandler() {
    return (database, collectionStatus, tableStatus, columnStatus, messages, saveCallback) -> {
      ClobColumnOptionsPanel panel = (ClobColumnOptionsPanel) ClobColumnOptionsPanel.createInstance(tableStatus,
        tableStatus.getColumnById(columnStatus.getId()));
      Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), messages.basicActionSave(),
        messages.basicActionCancel(), panel, new DefaultAsyncCallback<Dialogs.DialogAction>() {
          @Override
          public void onSuccess(Dialogs.DialogAction action) {
            processClobColumnSaveAction(action, tableStatus, columnStatus, panel, saveCallback);
          }
        });
    };
  }

  private static DialogHandler createBinaryHandler() {
    return (database, collectionStatus, tableStatus, columnStatus, messages, saveCallback) -> {
      BinaryColumnOptionsPanel panel = (BinaryColumnOptionsPanel) BinaryColumnOptionsPanel.createInstance(tableStatus,
        tableStatus.getColumnById(columnStatus.getId()));
      Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), messages.basicActionSave(),
        messages.basicActionCancel(), panel, new DefaultAsyncCallback<Dialogs.DialogAction>() {
          @Override
          public void onSuccess(Dialogs.DialogAction action) {
            processBinaryColumnSaveAction(action, tableStatus, columnStatus, panel, saveCallback);
          }
        });
    };
  }

  private static DialogHandler createNumericHandler() {
    return (database, collectionStatus, tableStatus, columnStatus, messages, saveCallback) -> {
      NumericColumnOptionsPanel panel = (NumericColumnOptionsPanel) NumericColumnOptionsPanel
        .createInstance(collectionStatus.getColumnByTableIdAndColumn(tableStatus.getId(), columnStatus.getId()));
      Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), "400px", messages.basicActionSave(),
        messages.basicActionCancel(), panel, new DefaultAsyncCallback<Dialogs.DialogAction>() {
          @Override
          public void onSuccess(Dialogs.DialogAction action) {
            processNumericColumnSaveAction(action, tableStatus, columnStatus, panel, collectionStatus, messages,
              saveCallback);
          }
        });
    };
  }

  private static DialogHandler createStandardHandler() {
    return (database, collectionStatus, tableStatus, columnStatus, messages, saveCallback) -> {
      ForeignKeysStatus fkStatus = collectionStatus.getForeignKeyByTableAndColumnId(tableStatus.getUuid(),
        columnStatus.getId());
      VirtualReferenceOptionsPanel panel = VirtualReferenceOptionsPanel.createInstance(database, collectionStatus,
        tableStatus, columnStatus, fkStatus);

      Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), "600px", messages.basicActionSave(),
        messages.basicActionCancel(), messages.basicActionDelete(), Arrays.asList(panel),
        new DefaultAsyncCallback<Dialogs.DialogAction>() {
          @Override
          public void onSuccess(Dialogs.DialogAction action) {
            processStandardColumnSaveAction(action, tableStatus, panel, fkStatus, collectionStatus, saveCallback);
          }
        });
    };
  }

  private static void processVirtualColumnSaveAction(Dialogs.DialogAction action, TableStatus tableStatus,
    ColumnStatus columnStatus, VirtualColumnOptionsPanel colPanel, VirtualReferenceOptionsPanel refPanel,
    ForeignKeysStatus fkStatus, CollectionStatus collectionStatus, Consumer<Boolean> saveCallback) {
    ColumnStatus column = tableStatus.getColumnById(columnStatus.getId());
    ColumnStatus updatedColStatus = colPanel.getColumnStatus();
    ForeignKeysStatus updatedFkStatus = refPanel.getVirtualReferenceStatus();

    if (Dialogs.DialogAction.SAVE.equals(action)) {
      if (updatedFkStatus != null) {
        if (updatedFkStatus.getVirtualForeignKeysStatus() != null) {
          updatedFkStatus.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.TO_PROCESS);
          updatedFkStatus.getVirtualForeignKeysStatus().setLastUpdatedDate(new Date());
        }
        tableStatus.addOrUpdateForeignKeyStatus(updatedFkStatus);
      }
      updatedColStatus.getVirtualColumnStatus().setProcessingState(ProcessingState.TO_PROCESS);
      column.setDescription(updatedColStatus.getDescription());
      column.setCustomDescription(updatedColStatus.getDescription());
      column.setVirtualColumnStatus(updatedColStatus.getVirtualColumnStatus());

      collectionStatus.setNeedsToBeProcessed(true);
      saveCallback.accept(true);

    } else if (Dialogs.DialogAction.REMOVE.equals(action)) {
      if (fkStatus != null && fkStatus.getVirtualForeignKeysStatus() != null) {
        fkStatus.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.TO_REMOVE);
        fkStatus.getVirtualForeignKeysStatus().setLastUpdatedDate(new Date());
        tableStatus.addOrUpdateForeignKeyStatus(fkStatus);
      }
      updatedColStatus.getVirtualColumnStatus().setProcessingState(ProcessingState.TO_REMOVE);
      column.setVirtualColumnStatus(updatedColStatus.getVirtualColumnStatus());

      collectionStatus.setNeedsToBeProcessed(true);
      saveCallback.accept(true);
    }
  }

  private static void processNestedColumnSaveAction(Dialogs.DialogAction action, TableStatus tableStatus,
    ColumnStatus columnStatus, NestedColumnOptionsPanel panel, Consumer<Boolean> saveCallback) {
    if (Dialogs.DialogAction.SAVE.equals(action)) {
      String multiValueTableName = panel.getMultiValueTableName();
      String targetReferenceUuid = columnStatus.getNestedColumns().getReferenceUuid();

      if (targetReferenceUuid != null) {
        for (ColumnStatus col : tableStatus.getColumns()) {
          if (col.getNestedColumns() != null && targetReferenceUuid.equals(col.getNestedColumns().getReferenceUuid())) {
            col.updateNestedColumnsMultiValueTableName(multiValueTableName);
          }
        }
      }
      ColumnStatus col = tableStatus.getColumnById(columnStatus.getId());
      col.updateSearchListTemplate(panel.getSearchTemplate());
      col.updateDetailsTemplate(panel.getDetailsTemplate());
      col.updateExportTemplate(panel.getExportTemplate());
      col.updateNestedColumnsQuantityList(panel.getQuantityInList());

      saveCallback.accept(true);
    }
  }

  private static void processClobColumnSaveAction(Dialogs.DialogAction action, TableStatus tableStatus,
    ColumnStatus columnStatus, ClobColumnOptionsPanel panel, Consumer<Boolean> saveCallback) {
    if (Dialogs.DialogAction.SAVE.equals(action)) {
      ColumnStatus col = tableStatus.getColumnById(columnStatus.getId());
      col.updateExportTemplate(panel.getExportTemplate());
      col.updateSearchListTemplate(panel.getSearchTemplate());
      col.updateDetailsTemplate(panel.getDetailsTemplate());
      col.updateDetailsShowContent(panel.showContentInDetails());
      col.getSearchStatus().getList().setShowContent(panel.showContentInList());
      col.setApplicationType(panel.getApplicationType());

      saveCallback.accept(true);
    }
  }

  private static void processBinaryColumnSaveAction(Dialogs.DialogAction action, TableStatus tableStatus,
    ColumnStatus columnStatus, BinaryColumnOptionsPanel panel, Consumer<Boolean> saveCallback) {
    if (Dialogs.DialogAction.SAVE.equals(action)) {
      ColumnStatus col = tableStatus.getColumnById(columnStatus.getId());
      col.updateExportTemplate(panel.getExportTemplate());
      col.updateSearchListTemplate(panel.getSearchTemplate());
      col.updateDetailsTemplate(panel.getDetailsTemplate());
      col.setApplicationType(panel.getApplicationType());

      saveCallback.accept(true);
    }
  }

  private static void processNumericColumnSaveAction(Dialogs.DialogAction action, TableStatus tableStatus,
    ColumnStatus columnStatus, NumericColumnOptionsPanel panel, CollectionStatus collectionStatus,
    ClientMessages messages, Consumer<Boolean> saveCallback) {
    if (Dialogs.DialogAction.SAVE.equals(action)) {
      if (panel.validate()) {
        Formatter formatter = panel.getFormatter();
        collectionStatus.getColumnByTableIdAndColumn(tableStatus.getId(), columnStatus.getId()).setFormatter(formatter);
        saveCallback.accept(true);
      } else {
        Dialogs.showErrors(messages.columnManagementPageTitle(),
          messages.columnManagementPageDialogErrorValueMustBeAnInteger(), messages.basicActionClose());
      }
    }
  }

  private static void processStandardColumnSaveAction(Dialogs.DialogAction action, TableStatus tableStatus,
    VirtualReferenceOptionsPanel panel, ForeignKeysStatus fkStatus, CollectionStatus collectionStatus,
    Consumer<Boolean> saveCallback) {
    ForeignKeysStatus updatedFkStatus = panel.getVirtualReferenceStatus();

    if (updatedFkStatus != null) {
      if (Dialogs.DialogAction.SAVE.equals(action)) {
        updatedFkStatus.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.TO_PROCESS);
        updatedFkStatus.getVirtualForeignKeysStatus().setLastUpdatedDate(new Date());
        tableStatus.addOrUpdateForeignKeyStatus(updatedFkStatus);
        collectionStatus.setNeedsToBeProcessed(true);
        saveCallback.accept(true);

      } else if (Dialogs.DialogAction.REMOVE.equals(action)) {
        if (fkStatus != null && fkStatus.getVirtualForeignKeysStatus() != null) {
          fkStatus.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.TO_REMOVE);
          fkStatus.getVirtualForeignKeysStatus().setLastUpdatedDate(new Date());
          tableStatus.addOrUpdateForeignKeyStatus(fkStatus);
        }
        collectionStatus.setNeedsToBeProcessed(true);
        saveCallback.accept(true);
      }
    }
  }
}
