package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.Date;
import java.util.StringJoiner;

import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.SavableOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidatableOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidationUiUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualForeignKeysStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.Alert.MessageAlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class VirtualReferenceOptionsPanel extends ColumnOptionsPanel
  implements ValidatableOptionsPanel, SavableOptionsPanel {

  interface VirtualReferenceOptionsPanelUiBinder extends UiBinder<Widget, VirtualReferenceOptionsPanel> {
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final VirtualReferenceOptionsPanelUiBinder binder = GWT
    .create(VirtualReferenceOptionsPanelUiBinder.class);

  private final CollectionStatus collectionStatus;
  private final TableStatus currentTableStatus;
  private final ColumnStatus currentColumnStatus;
  private final ViewerDatabase database;
  private final ForeignKeysStatus originalFkStatus;

  @UiField(provided = true)
  Alert fkReferencePanel;

  @UiField(provided = true)
  Alert virtualInfoAlert;

  @UiField(provided = true)
  Alert noCompatibleColumnsMessage;

  @UiField
  FlowPanel virtualReferencePanel;

  @UiField
  ListBox referencedTableListBox, referencedColumnListBox;

  @UiField
  Label errorReferencedTable, errorReferencedColumn;

  @UiField
  Button btnRemoveRelationship;

  private FlowPanel fkVisualPathContainer;
  private InlineLabel sourceColumnTypeLabel;

  public static VirtualReferenceOptionsPanel createInstance(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus tableStatus, ColumnStatus columnStatus, ForeignKeysStatus foreignKeysStatus) {
    return new VirtualReferenceOptionsPanel(database, collectionStatus, tableStatus, columnStatus, foreignKeysStatus);
  }

  private VirtualReferenceOptionsPanel(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus tableStatus, ColumnStatus columnStatus, ForeignKeysStatus foreignKeysStatus) {

    this.database = database;
    this.collectionStatus = collectionStatus;
    this.currentTableStatus = tableStatus;
    this.currentColumnStatus = columnStatus;
    this.originalFkStatus = foreignKeysStatus;

    fkReferencePanel = new Alert(MessageAlertType.SECONDARY, messages.columnManagementLabelFkDetected(),
      FontAwesomeIconManager.LOCK);
    fkReferencePanel.setVisible(false);

    virtualInfoAlert = new Alert(MessageAlertType.INFO, messages.columnManagementLabelForRelationship(),
      FontAwesomeIconManager.DATABASE_INFORMATION);

    noCompatibleColumnsMessage = new Alert(MessageAlertType.WARNING,
      messages.columnManagementTextNoCompatibleColumns());
    noCompatibleColumnsMessage.setVisible(false);

    initWidget(binder.createAndBindUi(this));

    Label fkSubtext = new Label(messages.columnManagementTextFkDetectedDescription());
    fkSubtext.addStyleName("text-muted small");
    fkReferencePanel.add(fkSubtext);

    fkVisualPathContainer = new FlowPanel();
    fkVisualPathContainer.addStyleName("dialog-fk-path-container reference-panel");
    fkReferencePanel.add(fkVisualPathContainer);

    FlowPanel sourceTypePanel = new FlowPanel();
    sourceColumnTypeLabel = new InlineLabel();
    sourceColumnTypeLabel.getElement().getStyle().setMarginRight(5, Style.Unit.PX);
    sourceColumnTypeLabel.addStyleName("font-bold");

    if (currentColumnStatus != null && currentColumnStatus.getType() != null) {
      sourceColumnTypeLabel.setText(messages.columnManagementTextSourceType(currentColumnStatus.getType().toString()));
    }

    sourceTypePanel.add(sourceColumnTypeLabel);
    sourceTypePanel.add(new InlineLabel(messages.columnManagementTextSourceTypeInfo()));
    virtualInfoAlert.add(sourceTypePanel);

    setupReferenceTableDropdown();
    bindEvents();
    populateVirtualReferenceFields(foreignKeysStatus);
  }

  private void setupReferenceTableDropdown() {
    referencedTableListBox.clear();
    referencedTableListBox.addItem("", "");
    collectionStatus.getTables().forEach(table -> referencedTableListBox.addItem(table.getId(), table.getUuid()));
  }

  private void bindEvents() {
    referencedTableListBox.addChangeHandler(event -> {
      ValidationUiUtils.clearError(referencedTableListBox, errorReferencedTable);
      onReferencedTableChanged();
    });

    referencedColumnListBox
      .addChangeHandler(event -> ValidationUiUtils.clearError(referencedColumnListBox, errorReferencedColumn));

    btnRemoveRelationship.addClickHandler(event -> {
      referencedTableListBox.setSelectedIndex(0);
      onReferencedTableChanged();
      btnRemoveRelationship.setVisible(false);
    });
  }

  private void onReferencedTableChanged() {
    referencedColumnListBox.clear();
    referencedColumnListBox.addItem("", "");
    noCompatibleColumnsMessage.setVisible(false);
    referencedColumnListBox.setVisible(true);

    String selectedTableId = referencedTableListBox.getSelectedItemText();
    int compatibleCount = 0;
    if (!selectedTableId.isEmpty()) {
      TableStatus targetTable = collectionStatus.getTableStatusByTableId(selectedTableId);
      if (targetTable != null) {
        compatibleCount = (int) targetTable.getColumns().stream()
          .filter(c -> VirtualOptionsPanelUtils.isSupportedColumnType(c, true))
          .filter(c -> c.getType() != null && c.getType().equals(currentColumnStatus.getType()))
          .peek(c -> referencedColumnListBox.addItem(c.getName(), c.getId())).count();
      }
      if (compatibleCount == 0) {
        referencedColumnListBox.setVisible(false);
        noCompatibleColumnsMessage.setVisible(true);
      }
    }
  }

  @Override
  public boolean validate() {
    boolean isValid = true;
    ValidationUiUtils.clearError(referencedTableListBox, errorReferencedTable);
    ValidationUiUtils.clearError(referencedColumnListBox, errorReferencedColumn);

    if (!fkReferencePanel.isVisible()) {
      String selectedTable = referencedTableListBox.getSelectedValue();
      String selectedColumn = referencedColumnListBox.getSelectedValue();

      if (!ViewerStringUtils.isBlank(selectedTable) && ViewerStringUtils.isBlank(selectedColumn)) {
        ValidationUiUtils.showError(referencedColumnListBox, errorReferencedColumn,
          messages.columnManagementErrorTargetColumnRequired());
        isValid = false;
      }
    }
    return isValid;
  }

  private void populateVirtualReferenceFields(ForeignKeysStatus foreignKeysStatus) {
    ViewerTable table = database.getMetadata().getTable(currentTableStatus.getUuid());

    if (foreignKeysStatus == null) {
      showVirtualReferenceEditor(new ForeignKeysStatus());
    } else if (foreignKeysStatus.getSourceType() != null
      && foreignKeysStatus.getSourceType().equals(ViewerSourceType.VIRTUAL)) {
      showVirtualReferenceEditor(foreignKeysStatus);
    } else {
      showFkInfo(foreignKeysStatus, table);
    }
  }

  private void showFkInfo(ForeignKeysStatus fk, ViewerTable sourceTable) {
    fkReferencePanel.setVisible(true);
    virtualReferencePanel.setVisible(false);

    ViewerTable targetTable = database.getMetadata().getTable(fk.getReferencedTableUUID());
    String targetTableName = targetTable != null ? targetTable.getName() : "";
    fkVisualPathContainer.clear();

    HTML referenceIcon = new HTML(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE)));
    referenceIcon.addStyleName("icon");
    fkVisualPathContainer.add(referenceIcon);

    if (sourceTable.getColumns() != null) {
      StringJoiner sourceCols = new StringJoiner(", ");
      StringJoiner targetCols = new StringJoiner(", ");

      for (ForeignKeysStatus.ReferencedColumnStatus ref : fk.getReferences()) {
        ColumnStatus sCol = collectionStatus.getColumnByTableIdAndColumn(sourceTable.getId(), ref.getSourceColumnId());
        sourceCols.add(sCol != null ? sCol.getCustomName() : ref.getSourceColumnId());

        if (targetTable != null) {
          ViewerColumn tCol = targetTable.getColumnBySolrName(ref.getReferencedColumnId());
          if (tCol != null)
            targetCols.add(tCol.getDisplayName());
        }
      }

      SafeHtml message = messages.dataTransformationTextForIsRelatedTo(targetTableName, targetCols.toString());
      fkVisualPathContainer.add(new HTML(message));
    }
  }

  private void showVirtualReferenceEditor(ForeignKeysStatus foreignKeysStatus) {
    fkReferencePanel.setVisible(false);
    virtualReferencePanel.setVisible(true);

    if (foreignKeysStatus != null && foreignKeysStatus.getVirtualForeignKeysStatus() != null) {
      VirtualOptionsPanelUtils.selectListBoxValue(referencedTableListBox, foreignKeysStatus.getReferencedTableUUID());
      onReferencedTableChanged();
      if (!foreignKeysStatus.getReferences().isEmpty()) {
        VirtualOptionsPanelUtils.selectListBoxValue(referencedColumnListBox,
          foreignKeysStatus.getReferences().get(0).getReferencedColumnId());
      }
      btnRemoveRelationship.setVisible(true);
    } else {
      btnRemoveRelationship.setVisible(false);
    }
  }

  @Override
  public boolean hasChanges() {
    if (fkReferencePanel.isVisible())
      return false;

    String currentTableUUID = referencedTableListBox.getSelectedValue() != null
      ? referencedTableListBox.getSelectedValue()
      : "";
    String currentColumnId = referencedColumnListBox.getSelectedValue() != null
      ? referencedColumnListBox.getSelectedValue()
      : "";
    String originalTableUUID = "";
    String originalColumnId = "";

    if (originalFkStatus != null && ViewerSourceType.VIRTUAL.equals(originalFkStatus.getSourceType())) {
      originalTableUUID = originalFkStatus.getReferencedTableUUID() != null ? originalFkStatus.getReferencedTableUUID()
        : "";
      if (!originalFkStatus.getReferences().isEmpty()) {
        originalColumnId = originalFkStatus.getReferences().get(0).getReferencedColumnId();
        if (originalColumnId == null)
          originalColumnId = "";
      }
    }
    return !currentTableUUID.equals(originalTableUUID) || !currentColumnId.equals(originalColumnId);
  }

  @Override
  public void applyChanges(ColumnStatus columnStatus, TableStatus tableStatus, CollectionStatus collectionStatus) {
    if (!hasChanges())
      return;

    String selectedTableUUID = referencedTableListBox.getSelectedValue();
    String selectedTable = referencedTableListBox.getSelectedItemText();

    if (ViewerStringUtils.isBlank(selectedTable)) {
      if (originalFkStatus != null && ViewerSourceType.VIRTUAL.equals(originalFkStatus.getSourceType())) {
        originalFkStatus.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.TO_REMOVE);
        originalFkStatus.getVirtualForeignKeysStatus().setLastUpdatedDate(new Date());
        tableStatus.addOrUpdateForeignKeyStatus(originalFkStatus);
      }
      return;
    }

    ForeignKeysStatus fkStatusToSave = (originalFkStatus != null
      && ViewerSourceType.VIRTUAL.equals(originalFkStatus.getSourceType())) ? originalFkStatus
        : new ForeignKeysStatus();

    if (fkStatusToSave.getId() == null) {
      fkStatusToSave.setSourceType(ViewerSourceType.VIRTUAL);
      fkStatusToSave.setId("virtual_fk_" + selectedTable);
      fkStatusToSave.setName("virtual_fk_" + selectedTable);
    }

    fkStatusToSave.getReferences().clear();
    fkStatusToSave.setReferencedTableId(selectedTable);
    fkStatusToSave.setReferencedTableUUID(selectedTableUUID);

    ForeignKeysStatus.ReferencedColumnStatus viewerReference = new ForeignKeysStatus.ReferencedColumnStatus();
    viewerReference.setReferencedColumnId(referencedColumnListBox.getSelectedValue());
    viewerReference.setSourceColumnId(currentColumnStatus.getId());
    fkStatusToSave.getReferences().add(viewerReference);

    VirtualForeignKeysStatus virtualStatus = new VirtualForeignKeysStatus();
    TemplateStatus template = new TemplateStatus();
    template.setTemplate("{{" + referencedColumnListBox.getSelectedItemText() + "}}");
    virtualStatus.setTemplateStatus(template);
    virtualStatus.setProcessingState(ProcessingState.TO_PROCESS);
    virtualStatus.setLastUpdatedDate(new Date());

    fkStatusToSave.setVirtualForeignKeysStatus(virtualStatus);
    tableStatus.addOrUpdateForeignKeyStatus(fkStatusToSave);
  }

  @Override
  public boolean requiresProcessing() {
    return true;
  }
}
