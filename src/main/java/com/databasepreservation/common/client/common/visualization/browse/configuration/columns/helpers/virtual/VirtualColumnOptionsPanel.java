package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.models.status.collection.AdvancedStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.DetailsStatus;
import com.databasepreservation.common.client.models.status.collection.ExportStatus;
import com.databasepreservation.common.client.models.status.collection.ListStatus;
import com.databasepreservation.common.client.models.status.collection.SearchStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnOptionsPanel extends ColumnOptionsPanel {

  interface VirtualColumnOptionsPanelUiBinder extends UiBinder<Widget, VirtualColumnOptionsPanel> {
  }

  private static VirtualColumnOptionsPanelUiBinder binder = GWT.create(VirtualColumnOptionsPanelUiBinder.class);

  private final TableStatus tableStatus;
  private List<String> sourceColumnsIds = new ArrayList<>();

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox virtualColumnName, virtualColumnDescription, templateSourceColumns;

  @UiField
  FlowPanel templateSourceColumnsHint;

  public static VirtualColumnOptionsPanel createInstance(TableStatus tableStatus, ColumnStatus columnStatus) {
    return new VirtualColumnOptionsPanel(tableStatus, columnStatus);
  }

  private VirtualColumnOptionsPanel(TableStatus tableStatus, ColumnStatus columnStatus) {
    initWidget(binder.createAndBindUi(this));
    this.tableStatus = tableStatus;

    VirtualOptionsPanelUtils.renderColumnTemplateButtons(tableStatus.getColumns(), templateSourceColumnsHint,
      templateSourceColumns, sourceColumnsIds, messages);

    populateVirtualColumnFields(columnStatus);
  }

  private void populateVirtualColumnFields(ColumnStatus columnStatus) {
    if (columnStatus.getName() != null) {
      virtualColumnName.setText(columnStatus.getName());
      virtualColumnName.setEnabled(false);
    }

    if (columnStatus.getDescription() != null) {
      virtualColumnDescription.setText(columnStatus.getDescription());
    }

    if (columnStatus.getVirtualColumnStatus() != null) {
      VirtualColumnStatus virtualColumnStatus = columnStatus.getVirtualColumnStatus();
      if (virtualColumnStatus.getSourceColumnsIds() != null) {
        sourceColumnsIds = virtualColumnStatus.getSourceColumnsIds();
      }

      if (virtualColumnStatus.getSourceTemplateStatus() != null) {
        templateSourceColumns.setText(virtualColumnStatus.getSourceTemplateStatus().getTemplate());
      }
    }
  }

  public ColumnStatus getColumnStatus() {
    int columnIndex = tableStatus.getColumns().size() + 1;
    ColumnStatus columnStatus = new ColumnStatus();
    columnStatus.setId("col" + columnIndex + "v_s");
    columnStatus.setType(ViewerType.dbTypes.VIRTUAL);
    columnStatus.setName(virtualColumnName.getText());
    columnStatus.setCustomName(virtualColumnName.getText());
    columnStatus.setDescription(virtualColumnDescription.getText());
    columnStatus.setCustomDescription(virtualColumnDescription.getText());
    columnStatus.setOrder(tableStatus.getLastColumnOrder() + 1);

    columnStatus.setExportStatus(new ExportStatus());
    columnStatus.getExportStatus().setTemplateStatus(new TemplateStatus());

    columnStatus.setSearchStatus(new SearchStatus());
    columnStatus.getSearchStatus().setAdvanced(new AdvancedStatus());
    columnStatus.getSearchStatus().setList(new ListStatus());
    columnStatus.getSearchStatus().getList().setShow(true);
    columnStatus.getSearchStatus().getList().setTemplate(new TemplateStatus());

    columnStatus.setDetailsStatus(new DetailsStatus());
    columnStatus.getDetailsStatus().setShow(true);
    columnStatus.getDetailsStatus().setTemplateStatus(new TemplateStatus());

    columnStatus.setVirtualColumnStatus(getVirtualColumnStatus());

    return columnStatus;
  }

  @NotNull
  private VirtualColumnStatus getVirtualColumnStatus() {
    VirtualColumnStatus virtualColumnStatus = new VirtualColumnStatus();
    virtualColumnStatus.setSourceColumnsIds(sourceColumnsIds);
    TemplateStatus sourceTemplateStatus = new TemplateStatus();
    sourceTemplateStatus.setTemplate(templateSourceColumns.getText());
    virtualColumnStatus.setSourceTemplateStatus(sourceTemplateStatus);

    return virtualColumnStatus;
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
