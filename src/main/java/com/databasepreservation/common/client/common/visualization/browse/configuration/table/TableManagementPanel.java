package com.databasepreservation.common.client.common.visualization.browse.configuration.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.cells.RequiredEditableCell;
import com.databasepreservation.common.client.common.lists.cells.TextAreaInputCell;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.helpers.ConfigurationHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableManagementPanel extends ContentPanel {
  private ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel header;

  @UiField
  FlowPanel content;

  interface TableManagementPanelUiBinder extends UiBinder<Widget, TableManagementPanel> {
  }

  private static TableManagementPanelUiBinder binder = GWT.create(TableManagementPanelUiBinder.class);

  private static Map<String, TableManagementPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private ViewerCollectionConfiguration viewerCollectionConfiguration;
  private Button btnSave = new Button();
  private boolean changes = false;
  private Map<String, MultipleSelectionTablePanel<ViewerTable>> tables = new HashMap<>();
  private Map<String, Boolean> initialLoading = new HashMap<>();
  private Map<String, ConfigurationHelper> editableValues = new HashMap<>();

  public static TableManagementPanel getInstance(ViewerDatabase database, ViewerCollectionConfiguration status) {
    return instances.computeIfAbsent(database.getUuid(), k -> new TableManagementPanel(database, status));
  }

  private TableManagementPanel(ViewerDatabase database, ViewerCollectionConfiguration viewerCollectionConfiguration) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    this.viewerCollectionConfiguration = viewerCollectionConfiguration;

    init();
  }

  private void init() {
    configureHeader();

    for (ViewerSchema schema : database.getMetadata().getSchemas()) {
      MultipleSelectionTablePanel<ViewerTable> schemaTable = createCellTableForViewerTable();
      schemaTable.setHeight("100%");
      populateTable(schemaTable, database.getMetadata().getSchema(schema.getUuid()));
      tables.put(schema.getUuid(), schemaTable);
      content.add(schemaTable);
    }

    configureButtonsPanel();
  }

  private void configureButtonsPanel() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");
    btnSave.setText(messages.basicActionSave());
    btnSave.addStyleName("btn btn-primary btn-save");

    btnCancel.addClickHandler(e -> handleCancelEvent(changes));

    btnSave.addClickHandler(clickEvent -> {
      if (selectionValidation()) {
        if (inputValidation()) {
          if (uniquenessValidation()) {
            for (MultipleSelectionTablePanel<ViewerTable> value : tables.values()) {
              for (ViewerTable table : database.getMetadata().getTables().values()) {
                viewerCollectionConfiguration.updateTableShowCondition(table.getUuid(), value.getSelectionModel().isSelected(table));
                if (editableValues.get(table.getUuid()) != null) {
                  viewerCollectionConfiguration.updateTableCustomDescription(table.getUuid(),
                      editableValues.get(table.getUuid()).getDescription());
                  viewerCollectionConfiguration.updateTableCustomName(table.getUuid(), editableValues.get(table.getUuid()).getLabel());
                }
              }
            }

            CollectionService.Util.call((Boolean result) -> {
              final CollectionObserver collectionObserver = ObserverManager.getCollectionObserver();
              collectionObserver.setCollectionStatus(viewerCollectionConfiguration);
              Toast.showInfo(messages.tableManagementPageTitle(), messages.tableManagementPageToastDescription());
              changes = false;
            }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), viewerCollectionConfiguration);
          } else {
            Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogUniqueError(),
                messages.basicActionClose());
          }
        } else {
          Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogInputError(),
            messages.basicActionClose());
        }
      } else {
        Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogSelectionError(),
          messages.basicActionClose());
      }
    });

    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel)));
  }

  private void configureHeader() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      messages.tableManagementPageTitle(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.tableManagementPageTableTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");
    content.add(instance);
  }

  private void handleCancelEvent(boolean changes) {
    if (changes) {
      Dialogs.showConfirmDialog(messages.columnManagementPageTitle(), messages.columnManagementPageCancelEventDialog(),
          messages.basicActionDiscard(), "btn btn-danger btn-times-circle", messages.basicActionBack(), "btn btn-link",
          new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
              if (!aBoolean) {
                instances.entrySet().removeIf(e -> e.getKey().startsWith(database.getUuid()));
                HistoryManager.gotoAdvancedConfiguration(database.getUuid());
              }
            }
          });
    } else {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    }
  }

  private MultipleSelectionTablePanel<ViewerTable> createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    selectionTablePanel.createTable(new FlowPanel(), Arrays.asList(1, 2), viewerSchema.getTables().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForShow(), 4,
        new Column<ViewerTable, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerTable viewerTable) {
            if (initialLoading.get(viewerTable.getUuid()) == null) {
              initialLoading.put(viewerTable.getUuid(), false);
              selectionTablePanel.getSelectionModel().setSelected(viewerTable,
                viewerCollectionConfiguration.showTable(viewerTable.getUuid()));
            }

            return selectionTablePanel.getSelectionModel().isSelected(viewerTable);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 15,
        new TextColumn<ViewerTable>() {
          @Override
          public String getValue(ViewerTable table) {
            return table.getName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForLabel(), 20,
        getLabelColumn()),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForDescription(), 0,
        getDescriptionColumn()));
  }

  private Column<ViewerTable, String> getLabelColumn() {
    Column<ViewerTable, String> label = new Column<ViewerTable, String>(new RequiredEditableCell() {}) {
      @Override
      public String getValue(ViewerTable table) {
        if (editableValues.get(table.getUuid()) == null) {
          ConfigurationHelper helper = new ConfigurationHelper(viewerCollectionConfiguration.getViewerTableConfiguration(table.getUuid()).getCustomName(),
              viewerCollectionConfiguration.getViewerTableConfiguration(table.getUuid()).getCustomDescription());
          editableValues.put(table.getUuid(), helper);
          return viewerCollectionConfiguration.getViewerTableConfiguration(table.getUuid()).getCustomName();
        } else {
          return editableValues.get(table.getUuid()).getLabel();
        }
      }
    };

    label.setFieldUpdater((index, table, value) -> {
      if (editableValues.get(table.getUuid()) != null) {
        final ConfigurationHelper configurationHelper = editableValues.get(table.getUuid());
        configurationHelper.setLabel(value);
        editableValues.put(table.getUuid(), configurationHelper);
      } else {
        ConfigurationHelper helper = new ConfigurationHelper(value,
          viewerCollectionConfiguration.getViewerTableConfiguration(table.getUuid()).getCustomDescription());
        editableValues.put(table.getUuid(), helper);
      }

      changes = true;
    });
    return label;
  }

  private Column<ViewerTable, String> getDescriptionColumn() {
    Column<ViewerTable, String> description = new Column<ViewerTable, String>(new TextAreaInputCell() {}) {
      @Override
      public String getValue(ViewerTable table) {
        if (editableValues.get(table.getUuid()) == null) {
          return viewerCollectionConfiguration.getViewerTableConfiguration(table.getUuid()).getCustomDescription();
        } else {
          return editableValues.get(table.getUuid()).getDescription();
        }
      }
    };

    description.setFieldUpdater((index, table, value) -> {
      if (editableValues.get(table.getUuid()) != null) {
        final ConfigurationHelper configurationHelper = editableValues.get(table.getUuid());
        configurationHelper.setDescription(value);
        editableValues.put(table.getUuid(), configurationHelper);
      } else {
        ConfigurationHelper helper = new ConfigurationHelper(viewerCollectionConfiguration.getViewerTableConfiguration(table.getUuid()).getCustomName(), value);
        editableValues.put(table.getUuid(), helper);
      }
    });
    return description;
  }

  private boolean selectionValidation() {
    for (MultipleSelectionTablePanel<ViewerTable> value : tables.values()) {
      if (value.getSelectionModel().getSelectedSet().isEmpty())
        return false;
    }

    return true;
  }

  private boolean uniquenessValidation() {
    Set<String> uniques = new HashSet<>();

    for (ConfigurationHelper value : editableValues.values()) {
      if (!uniques.add(value.getLabel())) {
        return false;
      }
    }

    return true;
  }

  private boolean inputValidation() {
    for (ConfigurationHelper value : editableValues.values()) {
      if (ViewerStringUtils.isBlank(value.getLabel())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forTableManagement(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }
}