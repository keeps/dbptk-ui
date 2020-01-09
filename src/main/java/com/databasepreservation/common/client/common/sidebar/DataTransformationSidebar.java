package com.databasepreservation.common.client.common.sidebar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.visualization.browse.configuration.DataTransformationProgressPanel;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.JobService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DataTransformationSidebar extends Composite implements Sidebar {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DataTransformationSidebar> instances = new HashMap<>();

  interface DatabaseSidebarUiBinder extends UiBinder<Widget, DataTransformationSidebar> {
  }

  private static DatabaseSidebarUiBinder uiBinder = GWT.create(DatabaseSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  FlowPanel controller;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  private ViewerDatabase database;
  private String databaseUUID;
  private CollectionStatus collectionStatus;
  private boolean initialized = false;
  private Map<String, SidebarHyperlink> list = new HashMap<>();
  private Button btnSaveConfiguration;
  private Button btnClearConfiguration;

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param databaseUUID
   *          the database UUID
   * @return a DatabaseSidebar instance
   */
  public static DataTransformationSidebar getInstance(String databaseUUID) {
    if (databaseUUID == null) {
      return getEmptyInstance();
    }

    DataTransformationSidebar instance = instances.get(databaseUUID);
    if (instance == null || instance.database == null
      || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DataTransformationSidebar(databaseUUID);
      instances.put(databaseUUID, instance);
    }
    return instance;
  }

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param database
   *          the database
   * @return a DatabaseSidebar instance
   */
  public static DataTransformationSidebar getInstance(ViewerDatabase database, CollectionStatus status) {
    if (database == null) {
      return getEmptyInstance();
    }

    DataTransformationSidebar instance = instances.get(database.getUuid());
    if (instance == null || instance.database == null
      || !ViewerDatabaseStatus.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DataTransformationSidebar(database, status);
      instances.put(database.getUuid(), instance);
    }

    return instance;
  }

  /**
   * Creates a new (dummy) DatabaseSidebar that is not visible. This method exists
   * so that pages can opt for not using a sidebar at all.
   *
   * @return a new invisible DatabaseSidebar
   */
  public static DataTransformationSidebar getEmptyInstance() {
    return new DataTransformationSidebar();
  }

  /**
   * Clone constructor, because the same DatabaseSidebar can not be child in more
   * than one widget
   *
   * @param other
   *          the DatabaseSidebar used in another widget
   */
  private DataTransformationSidebar(DataTransformationSidebar other) {
    initialized = other.initialized;
    initWidget(uiBinder.createAndBindUi(this));
    searchInputBox.setText(other.searchInputBox.getText());
    init(other.database, other.collectionStatus);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DataTransformationSidebar(ViewerDatabase database, CollectionStatus status) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database, status);
  }

  /**
   * Empty constructor, for pages that do not have a sidebar
   */
  private DataTransformationSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DataTransformationSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  public void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    SidebarHyperlink informationLink = new SidebarHyperlink(FontAwesomeIconManager
      .getTagSafeHtml(FontAwesomeIconManager.DATABASE_INFORMATION, messages.menusidebar_information()),
      HistoryManager.linkToDataTransformation(database.getUuid()));
    informationLink.setH5().setIndent0();
    list.put(database.getUuid(), informationLink);
    sidebarGroup.add(informationLink);

    /* Schemas */
    SidebarItem schemasHeader = createSidebarSubItemHeaderSafeHMTL("Tables", FontAwesomeIconManager.LIST);
    FlowPanel schemaItems = new FlowPanel();

    final int totalSchemas = metadata.getSchemas().size();

    String iconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR, "fa-sm");

    for (final ViewerSchema schema : metadata.getSchemas()) {
      schema.setViewsSchemaUUID();

      for (ViewerTable table : schema.getTables()) {
        if (!table.isCustomView() && !table.isMaterializedView()) {
          if (collectionStatus.showTable(table.getUuid())) {
            schemaItems.add(createTableItem(schema, table, totalSchemas, iconTag));
          }
        }
      }
    }

    createSubItem(schemasHeader, schemaItems, false);

    searchInit();
    setVisible(true);
  }

  private SidebarHyperlink createTableItem(final ViewerSchema schema, final ViewerTable table, final int totalSchemas,
    final String iconTag) {
    SafeHtml html;
    if (totalSchemas == 1) {
      html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE, table.getName());
    } else {
      html = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        schema.getName() + " " + iconTag + " " + table.getName());
    }
    SidebarHyperlink tableLink = new SidebarHyperlink(html,
      HistoryManager.linkToDataTransformationTable(database.getUuid(), table.getUuid()));
    tableLink.setH6().setIndent2();
    list.put(table.getUuid(), tableLink);
    sidebarGroup.add(tableLink);

    return tableLink;
  }

  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void init(ViewerDatabase db, CollectionStatus status) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    if (ViewerDatabaseStatus.AVAILABLE.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUuid()))) {
        initialized = true;
        database = db;
        collectionStatus = status;
        createControllerPanel();
        databaseUUID = db.getUuid();
        init();
      }
    }
  }

  private SidebarItem createSidebarSubItemHeaderSafeHMTL(String headerText, String headerIcon) {
    return new SidebarItem(FontAwesomeIconManager.getTagSafeHtml(headerIcon, headerText)).setH5().setIndent0();
  }

  private void createSubItem(SidebarItem header, FlowPanel content, boolean collapsed) {
    DisclosurePanel panel = new DisclosurePanel();
    panel.setOpen(!collapsed);
    panel.setAnimationEnabled(true);
    panel.setHeader(header);
    panel.setContent(content);
    panel.getElement().addClassName("sidebar-collapse");
    sidebarGroup.add(panel);
  }

  @Override
  public void select(String value) {
    for (Map.Entry<String, SidebarHyperlink> entry : list.entrySet()) {
      if (entry.getKey().equals(value)) {
        list.get(value).setSelected(true);
      } else {
        list.get(entry.getKey()).setSelected(false);
      }
    }
  }

  private void searchInit() {
    searchInputBox.getElement().setPropertyString("placeholder", messages.menusidebar_filterSidebar());
    searchInputBox.addChangeHandler(event -> doSearch());
    searchInputBox.addKeyUpHandler(event -> doSearch());
    searchInputButton.addClickHandler(event -> doSearch());
  }

  private void doSearch() {
    String searchValue = searchInputBox.getValue();

    if (ViewerStringUtils.isBlank(searchValue)) {
      showAll();
    } else {
      showMatching(searchValue);
    }
  }

  private void showAll() {
    // show all
    for (Widget widget : sidebarGroup) {
      widget.setVisible(true);

      if (widget instanceof DisclosurePanel) {
        DisclosurePanel disclosurePanel = (DisclosurePanel) widget;
        FlowPanel fp = (FlowPanel) disclosurePanel.getContent();
        for (Widget value : fp) {
          SidebarItem sb = (SidebarItem) value;
          sb.setVisible(true);
        }
      }
    }
  }

  private void showMatching(final String searchValue) {
    // show matching and their parents
    Set<DisclosurePanel> disclosurePanelsThatShouldBeVisible = new HashSet<>();

    for (Widget widget : sidebarGroup) {
      if (widget instanceof DisclosurePanel) {
        DisclosurePanel disclosurePanel = (DisclosurePanel) widget;
        disclosurePanel.setOpen(true);
        FlowPanel fp = (FlowPanel) disclosurePanel.getContent();

        for (Widget value : fp) {
          SidebarItem sb = (SidebarItem) value;
          if (sb.getText().toLowerCase().contains(searchValue.toLowerCase())) {
            sb.setVisible(true);
            disclosurePanelsThatShouldBeVisible.add(disclosurePanel);
          } else {
            sb.setVisible(false);
            disclosurePanel.setVisible(false);
          }
        }
      } else {
        widget.setVisible(true);
      }
    }

    for (DisclosurePanel disclosurePanel : disclosurePanelsThatShouldBeVisible) {
      disclosurePanel.setVisible(true);
    }
  }

  private void createControllerPanel() {
    btnSaveConfiguration = new Button();
    btnSaveConfiguration.setText(messages.basicActionSave());
    btnSaveConfiguration.setStyleName("btn btn-save");
    btnSaveConfiguration.setEnabled(false);

    Button btnDenormalize = new Button();
    btnDenormalize.setText("Run");
    btnDenormalize.addStyleName("btn btn-run");
    btnDenormalize.setEnabled(false);

    btnClearConfiguration = new Button();
    btnClearConfiguration.setText(messages.basicActionClear());
    btnClearConfiguration.addStyleName("btn btn-times-circle btn-danger");
    btnClearConfiguration.setEnabled(false);

    btnSaveConfiguration.addClickHandler(event -> {
      collectionStatus.getDenormalizations();
      btnDenormalize.setEnabled(true);
      btnClearConfiguration.setEnabled(false);
      btnSaveConfiguration.setEnabled(false);
    });

    btnDenormalize.addClickHandler(event -> {
      final DialogBox dialogBox = Dialogs.showWaitResponse(messages.dataTransformationSidebarDialogTitle(),
        messages.dataTransformationSidebarWaitDialogMessage());
      JobService.Util.call((Boolean result) -> {
        Dialogs.showInformationDialog(messages.dataTransformationSidebarDialogTitle(),
          messages.dataTransformationSidebarSuccessDialogMessage(), messages.basicActionClose());
        dialogBox.hide();
        btnDenormalize.setEnabled(false);
        DataTransformationProgressPanel progressPanel = DataTransformationProgressPanel.getInstance(database);
        progressPanel.initProgress();
        HistoryManager.gotoDataTransformation(databaseUUID);
      }, (String errorMessage) -> {
        Dialogs.showErrors(messages.dataTransformationSidebarDialogTitle(), errorMessage, messages.basicActionClose());
        btnDenormalize.setEnabled(false);
      }).denormalizeJob(databaseUUID);
    });

    btnClearConfiguration.addClickHandler(event -> {
      Window.Location.reload();
    });

    controller.add(btnSaveConfiguration);
    controller.add(btnClearConfiguration);
    controller.add(btnDenormalize);
  }

  public void enableSaveConfiguration(Boolean enable) {
    btnSaveConfiguration.setEnabled(enable);
    btnClearConfiguration.setEnabled(enable);
  }
}
