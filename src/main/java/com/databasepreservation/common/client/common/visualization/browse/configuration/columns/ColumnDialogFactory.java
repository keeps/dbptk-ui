package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.BinaryColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ClobColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.NestedColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.NumericColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.SavableOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.TabbedColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual.VirtualColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual.VirtualReferenceOptionsPanel;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * Dynamic pipeline factory for building and processing Column Configuration
 * dialogs.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ColumnDialogFactory {

  private ColumnDialogFactory() {
  }

  public static void openDialog(ViewerDatabase database, CollectionStatus collectionStatus, TableStatus tableStatus,
    ColumnStatus columnStatus, ClientMessages messages, Consumer<Boolean> saveCallback) {

    List<ColumnOptionsPanel> panels = new ArrayList<>();

    // 1. Type-Specific Formatting Tab (Numeric, Clob, Binary, Nested)
    ColumnOptionsPanel typePanel = getTypeSpecificPanel(tableStatus, columnStatus, collectionStatus);
    if (typePanel != null) {
      panels.add(typePanel);
    }

    // 2. Virtual Definition Tab
    if (columnStatus.isVirtual()) {
      panels.add(VirtualColumnOptionsPanel.createInstance(tableStatus, columnStatus));
    }

    // 3. Relationship Tab (Allowed for primitive types)
    if (isRelationshipAllowed(columnStatus.getType())) {
      ForeignKeysStatus fkStatus = collectionStatus.getForeignKeyByTableAndColumnId(tableStatus.getUuid(),
        columnStatus.getId());
      panels.add(
        VirtualReferenceOptionsPanel.createInstance(database, collectionStatus, tableStatus, columnStatus, fkStatus));
    }

    if (panels.isEmpty())
      return;

    // 4. UI Assembly
    final TabbedColumnOptionsPanel container = new TabbedColumnOptionsPanel();
    for (ColumnOptionsPanel p : panels) {
      container.addTab(getTabLabel(p, messages), p);
    }

    // 5. UX: Global Delete button is ONLY shown if the column itself is virtual
    String deleteBtnText = columnStatus.isVirtual() ? messages.columnManagementButtonTextForDeleteVirtualColumn()
      : null;

    Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), "600px", messages.basicActionSave(),
      messages.basicActionCancel(), deleteBtnText, Arrays.asList(container),
      new DefaultAsyncCallback<Dialogs.DialogAction>() {
        @Override
        public void onSuccess(Dialogs.DialogAction action) {
          handleGlobalAction(action, container, columnStatus, tableStatus, collectionStatus, saveCallback);
        }
      });
  }

  private static void handleGlobalAction(Dialogs.DialogAction action, TabbedColumnOptionsPanel container,
    ColumnStatus col, TableStatus table, CollectionStatus status, Consumer<Boolean> callback) {

    if (Dialogs.DialogAction.SAVE.equals(action)) {
      boolean stateChanged = false;
      boolean requiresBatchJob = false;

      for (Widget p : container.getPanels()) {
        if (p instanceof SavableOptionsPanel) {
          SavableOptionsPanel savablePanel = (SavableOptionsPanel) p;

          if (savablePanel.hasChanges()) {
            savablePanel.applyChanges(col, table, status);
            stateChanged = true;

            if (savablePanel.requiresProcessing()) {
              requiresBatchJob = true;
            }
          }
        }
      }
      if (stateChanged) {
        if (requiresBatchJob) {
          status.setNeedsToBeProcessed(true);
        }
        callback.accept(true);
      }

    } else if (Dialogs.DialogAction.REMOVE.equals(action)) {
      // Cascading global delete (Virtual Column + its Relations)
      if (col.isVirtual() && col.getVirtualColumnStatus() != null) {
        col.getVirtualColumnStatus().setProcessingState(ProcessingState.TO_REMOVE);
      }

      ForeignKeysStatus fkStatus = status.getForeignKeyByTableAndColumnId(table.getUuid(), col.getId());
      if (fkStatus != null && fkStatus.getVirtualForeignKeysStatus() != null) {
        fkStatus.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.TO_REMOVE);
        fkStatus.getVirtualForeignKeysStatus().setLastUpdatedDate(new Date());
        table.addOrUpdateForeignKeyStatus(fkStatus);
      }

      status.setNeedsToBeProcessed(true);
      callback.accept(true);
    }
  }

  private static ColumnOptionsPanel getTypeSpecificPanel(TableStatus table, ColumnStatus col, CollectionStatus status) {
    ViewerType.dbTypes type = col.getType();
    if (type == null)
      return null;

    switch (type) {
      case NESTED:
        return NestedColumnOptionsPanel.createInstance(col);
      case CLOB:
        return ClobColumnOptionsPanel.createInstance(table, col);
      case BINARY:
        return BinaryColumnOptionsPanel.createInstance(table, col);
      case NUMERIC_FLOATING_POINT:
      case NUMERIC_INTEGER:
        return NumericColumnOptionsPanel.createInstance(status.getColumnByTableIdAndColumn(table.getId(), col.getId()));
      default:
        return null;
    }
  }

  private static boolean isRelationshipAllowed(ViewerType.dbTypes type) {
    return type != null && !ViewerType.dbTypes.NESTED.equals(type) && !ViewerType.dbTypes.CLOB.equals(type)
      && !ViewerType.dbTypes.BINARY.equals(type);
  }

  private static String getTabLabel(ColumnOptionsPanel p, ClientMessages clientMessages) {
    if (p instanceof VirtualColumnOptionsPanel)
      return clientMessages.columnManagementLabelForVirtualColumn();
    if (p instanceof VirtualReferenceOptionsPanel)
      return clientMessages.columnManagementLabelForRelationship();
    return clientMessages.columnManagementLabelForPresentation();
  }
}
