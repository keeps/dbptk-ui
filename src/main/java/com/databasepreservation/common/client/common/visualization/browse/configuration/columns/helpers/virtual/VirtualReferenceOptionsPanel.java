package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidatableOptionsPanel;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualReferenceStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class VirtualReferenceOptionsPanel extends ColumnOptionsPanel implements ValidatableOptionsPanel {

  interface VirtualReferenceOptionsPanelUiBinder extends UiBinder<Widget, VirtualReferenceOptionsPanel> {
  }

  private static final VirtualReferenceOptionsPanelUiBinder binder = GWT
    .create(VirtualReferenceOptionsPanelUiBinder.class);

  private final TableStatus currentTableStatus;
  private final ViewerDatabase database;
  private final CollectionStatus collectionStatus;
  private List<String> targetColumnsIds = new ArrayList<>();

  @UiField
  ClientMessages messages;
  @UiField
  FlowPanel virtualReferencePanel, fkReferencePanel, templateReferencedColumnsHint;
  @UiField
  ListBox referencedTableListBox;
  @UiField
  TextBox templateReferencedColumns;
  @UiField
  Label fkTableLabel, fkColumnsLabel;
  @UiField
  Label errorReferencedTable, errorTemplateReferencedColumns;

  public static VirtualReferenceOptionsPanel createInstance(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus tableStatus, ColumnStatus columnStatus) {
    return new VirtualReferenceOptionsPanel(database, collectionStatus, tableStatus, columnStatus);
  }

  private VirtualReferenceOptionsPanel(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus tableStatus, ColumnStatus columnStatus) {
    initWidget(binder.createAndBindUi(this));

    this.database = database;
    this.collectionStatus = collectionStatus;
    this.currentTableStatus = tableStatus;

    setupReferenceTableDropdown();
    bindEvents();
    populateVirtualReferenceFields(columnStatus);
  }

  private void setupReferenceTableDropdown() {
    referencedTableListBox.clear();
    referencedTableListBox.addItem("", "");
    collectionStatus.getTables().forEach(table -> referencedTableListBox.addItem(table.getId(), table.getId()));
  }

  private void bindEvents() {
    referencedTableListBox.addChangeHandler(event -> {
      clearError(referencedTableListBox, errorReferencedTable);
      onReferencedTableChanged();
    });

    templateReferencedColumns
      .addKeyUpHandler(event -> clearError(templateReferencedColumns, errorTemplateReferencedColumns));
  }

  private void onReferencedTableChanged() {
    String selectedTableId = referencedTableListBox.getSelectedValue();
    if (selectedTableId.isEmpty()) {
      resetVirtualFields();
    } else {
      TableStatus targetTable = collectionStatus.getTableStatusByTableId(selectedTableId);
      if (targetTable != null) {
        VirtualOptionsPanelUtils.renderColumnTemplateButtons(targetTable.getColumns(), templateReferencedColumnsHint,
          templateReferencedColumns, targetColumnsIds, messages, true);
      }
    }
  }

  @Override
  public boolean validate() {
    boolean isValid = true;

    clearError(referencedTableListBox, errorReferencedTable);
    clearError(templateReferencedColumns, errorTemplateReferencedColumns);

    if (fkReferencePanel.isVisible()) {
      return true;
    }

    String selectedTable = referencedTableListBox.getSelectedValue();
    boolean hasTable = !ViewerStringUtils.isBlank(selectedTable);
    boolean hasTemplate = !ViewerStringUtils.isBlank(templateReferencedColumns.getText());

    if (hasTemplate && !hasTable) {
      showError(referencedTableListBox, errorReferencedTable,
        messages.columnManagementLabelForReferencedTable() + " is required.");
      isValid = false;
    }

    if (hasTable && !hasTemplate) {
      showError(templateReferencedColumns, errorTemplateReferencedColumns,
        messages.columnManagementLabelForReferencedColumnsTemplate() + " is required.");
      isValid = false;
    }

    return isValid;
  }

  private void showError(Widget input, Label errorLabel, String message) {
    input.addStyleName("dialog-input-error");
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }

  private void clearError(Widget input, Label errorLabel) {
    input.removeStyleName("dialog-input-error");
    errorLabel.setText("");
    errorLabel.setVisible(false);
  }

  private void populateVirtualReferenceFields(ColumnStatus columnStatus) {
    ViewerTable table = database.getMetadata().getTable(currentTableStatus.getUuid());
    ViewerForeignKey fk = findForeignKey(table, columnStatus.getId());

    if (fk != null) {
      showFkInfo(fk, table);
    } else {
      showVirtualReferenceEditor(columnStatus.getVirtualReferenceStatus());
    }
  }

  private void showFkInfo(ViewerForeignKey fk, ViewerTable sourceTable) {
    fkReferencePanel.setVisible(true);
    virtualReferencePanel.setVisible(false);

    ViewerTable targetTable = database.getMetadata().getTable(fk.getReferencedTableUUID());
    fkTableLabel.setText(targetTable != null ? targetTable.getId() : "Unknown Table");

    if (sourceTable.getColumns() != null) {
      String columnNames = fk.getReferences().stream()
        .map(ref -> sourceTable.getColumns().get(ref.getSourceColumnIndex()).getDisplayName())
        .collect(Collectors.joining(", "));
      fkColumnsLabel.setText(columnNames);
    }
  }

  private void showVirtualReferenceEditor(VirtualReferenceStatus virtualStatus) {
    fkReferencePanel.setVisible(false);
    virtualReferencePanel.setVisible(true);

    if (virtualStatus == null)
      return;

    VirtualOptionsPanelUtils.selectListBoxValue(referencedTableListBox, virtualStatus.getReferencedTableUUID());

    // Trigger para carregar as colunas da tabela selecionada
    onReferencedTableChanged();

    if (virtualStatus.getReferencedTemplateStatus() != null) {
      templateReferencedColumns.setText(virtualStatus.getReferencedTemplateStatus().getTemplate());
      this.targetColumnsIds = new ArrayList<>(virtualStatus.getReferencedColumnsIds());
    }
  }

  private ViewerForeignKey findForeignKey(ViewerTable table, String columnSolrName) {
    if (table == null || table.getForeignKeys() == null)
      return null;
    return table.getForeignKeys().stream()
      .filter(fk -> fk.getReferences().stream()
        .anyMatch(ref -> table.getColumns().get(ref.getSourceColumnIndex()).getSolrName().equals(columnSolrName)))
      .findFirst().orElse(null);
  }

  private void resetVirtualFields() {
    templateReferencedColumns.setText("");
    templateReferencedColumnsHint.clear();
    targetColumnsIds.clear();
  }

  public VirtualReferenceStatus getVirtualReferenceStatus() {
    String selectedTable = referencedTableListBox.getSelectedValue();

    if (fkReferencePanel.isVisible() || ViewerStringUtils.isBlank(selectedTable)) {
      return null;
    }

    VirtualReferenceStatus status = new VirtualReferenceStatus();
    status.setReferencedTableUUID(selectedTable);
    status.setReferencedColumnsIds(targetColumnsIds);

    TemplateStatus template = new TemplateStatus();
    template.setTemplate(templateReferencedColumns.getText());
    status.setReferencedTemplateStatus(template);

    return status;
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getExportTemplate() {
    return null;
  }
}
