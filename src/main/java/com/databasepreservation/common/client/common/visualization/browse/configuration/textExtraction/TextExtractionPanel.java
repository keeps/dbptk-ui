/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.textExtraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TextExtractionPanel extends ContentPanel {
  private static final TableManagementPanelUiBinder binder = GWT.create(TableManagementPanelUiBinder.class);
  private static final Map<String, TextExtractionPanel> instances = new HashMap<>();
  private final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ViewerDatabase database;
  private final CollectionStatus collectionStatus;
  private final Button btnSave = new Button();
  private final Map<String, MultipleSelectionTablePanel<ViewerColumn>> tables = new HashMap<>();
  private final Map<String, Set<String>> initialLoading = new HashMap<>();
  @UiField
  FlowPanel header;
  @UiField
  FlowPanel content;

  private TextExtractionPanel(ViewerDatabase database, CollectionStatus collectionStatus) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    this.collectionStatus = collectionStatus;

    init();
  }

  public static TextExtractionPanel getInstance(ViewerDatabase database, CollectionStatus status) {
    return instances.computeIfAbsent(database.getUuid(), k -> new TextExtractionPanel(database, status));
  }

  private void init() {
    configureHeader();

    for (ViewerSchema schema : database.getMetadata().getSchemas()) {
      for (ViewerTable table : schema.getTables()) {
        if (!table.getBinaryColumns().isEmpty()) {
          MultipleSelectionTablePanel<ViewerColumn> columnsTable = createCellTableForViewerTable();
          tables.put(table.getUuid(), columnsTable);
          initialLoading.put(table.getUuid(), new HashSet<>());
          columnsTable.setHeight("100%");
          populateTable(columnsTable, table);
          FlowPanel widgets = CommonClientUtils.wrapOnDiv("table-management-schema-divider",
            CommonClientUtils.getHeader(SafeHtmlUtils.fromSafeConstant(schema.getName() + " - " + table.getName()),
              "table-management-schema-title"),
            columnsTable);
          content.add(widgets);
        }
      }
    }

    configureButtonsPanel();
  }

  private void configureButtonsPanel() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");
    btnSave.setText(messages.basicActionConfirm());
    btnSave.addStyleName("btn btn-primary btn-save");

    btnCancel.addClickHandler(event -> HistoryManager.gotoAdvancedConfiguration(database.getUuid()));

    btnSave.addClickHandler(clickEvent -> {
      for (String tableUUID : initialLoading.keySet()) {
        SelectionModel<ViewerColumn> selectedColumns = tables.get(tableUUID).getSelectionModel();
        for (ViewerColumn lobColumn : database.getMetadata().getTable(tableUUID).getBinaryColumns()) {
          collectionStatus.updateColumnTextExtractionPolicy(tableUUID, lobColumn.getSolrName(),
            selectedColumns.isSelected(lobColumn));
        }
      }

      CollectionService.Util.call((Boolean result) -> {
        final CollectionObserver collectionObserver = ObserverManager.getCollectionObserver();
        collectionObserver.setCollectionStatus(collectionStatus);
        CollectionService.Util.call((StringResponse response) -> {
          Toast.showInfo(messages.textExtractionPageTitle(), messages.textExtractionPageToastDescription());
        }).extractAndIndexDatabaseLobText(database.getUuid(), database.getUuid());
      }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), collectionStatus);
    });

    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel)));
  }

  private void configureHeader() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      messages.textExtractionPageTitle(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.textExtractionPageTableTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");
    content.add(instance);
  }

  private MultipleSelectionTablePanel<ViewerColumn> createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    final ViewerTable viewerTable) {

    selectionTablePanel.createTable(new FlowPanel(), Arrays.asList(1, 2), viewerTable.getBinaryColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.textExtractionPageTableHeaderTextForExtractPolicy(), 4,
        getCheckboxColumn(selectionTablePanel, viewerTable.getUuid())),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 15,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn column) {
            return column.getDisplayName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForDescription(), 15,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn column) {
            return column.getDescription();
          }
        }));
  }

  private Column<ViewerColumn, Boolean> getCheckboxColumn(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    String tableUUID) {
    Column<ViewerColumn, Boolean> column = new Column<ViewerColumn, Boolean>(new CheckboxCell(false, false)) {
      @Override
      public Boolean getValue(ViewerColumn viewerColumn) {
        if (!initialLoading.get(tableUUID).contains(viewerColumn.getSolrName())) {
          initialLoading.get(tableUUID).add(viewerColumn.getSolrName());
          selectionTablePanel.getSelectionModel().setSelected(viewerColumn, collectionStatus.getTableStatus(tableUUID)
            .getColumnById(viewerColumn.getSolrName()).getLobTextExtractionPolicy().getExtractAndIndexLobText());
        }

        return selectionTablePanel.getSelectionModel().isSelected(viewerColumn);
      }
    };

    column.setFieldUpdater((i, viewerColumn, value) -> {
      selectionTablePanel.getSelectionModel().setSelected(viewerColumn, value);
    });

    return column;
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forTextExtraction(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  interface TableManagementPanelUiBinder extends UiBinder<Widget, TextExtractionPanel> {
  }
}