package com.databasepreservation.main.desktop.client.common.sidebar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarHyperlink;
import com.databasepreservation.main.common.shared.client.common.sidebar.SidebarItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <bferreira@keep.pt>
 */
public class MetadataEditSidebar extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataEditSidebar> instances = new HashMap<>();

  public static MetadataEditSidebar getInstance(String databaseUUID) {
    if (databaseUUID == null) {
      return getEmptyInstance();
    }

    MetadataEditSidebar instance = instances.get(databaseUUID);
    if (instance == null || instance.database == null
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new MetadataEditSidebar(databaseUUID);
      instances.put(databaseUUID, instance);
    } else {
      return new MetadataEditSidebar(instance);
    }
    return instance;
  }

  public static MetadataEditSidebar getInstance(ViewerDatabase database) {
    if (database == null) {
      return getEmptyInstance();
    }

    MetadataEditSidebar instance = instances.get(database.getUUID());
    if (instance == null || instance.database == null
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new MetadataEditSidebar(database);
      instances.put(database.getUUID(), instance);
    } else {
      // workaround because the same MetadataEditSidebar can not belong to multiple
      // widgets
      return new MetadataEditSidebar(instance);
    }
    return instance;
  }

  public static MetadataEditSidebar getEmptyInstance() {
    return new MetadataEditSidebar();
  }

  interface DatabaseSidebarUiBinder extends UiBinder<Widget, MetadataEditSidebar> {
  }

  private static DatabaseSidebarUiBinder uiBinder = GWT.create(DatabaseSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  private ViewerDatabase database;
  private String databaseUUID;
  private boolean initialized = false;

  private MetadataEditSidebar(MetadataEditSidebar other) {
    initialized = other.initialized;
    initWidget(uiBinder.createAndBindUi(this));
    searchInputBox.setText(other.searchInputBox.getText());
    init(other.database);
  }

  private MetadataEditSidebar(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database);
  }

  private MetadataEditSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  private MetadataEditSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  public void init(ViewerDatabase db) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    GWT.log("started");
    if (ViewerDatabase.Status.AVAILABLE.equals(db.getStatus())
      || ViewerDatabase.Status.METADATA_ONLY.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUUID()))) {
        GWT.log("initialize");
        initialized = true;
        database = db;
        databaseUUID = db.getUUID();
        init();
      }
    }
  }

  public boolean isInitialized() {
    return initialized;
  }

  private void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    sidebarGroup.add(
      new SidebarHyperlink(messages.menusidebar_database(), HistoryManager.linkToDatabaseMetadata(database.getUUID()))
        .addIcon(FontAwesomeIconManager.DATABASE).setH5().setIndent0());

    sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_usersRoles(),
      HistoryManager.linkToDatabaseMetadataUsers(database.getUUID())).addIcon(FontAwesomeIconManager.DATABASE_USERS)
        .setH5().setIndent0());

    for (final ViewerSchema schema : metadata.getSchemas()) {
      sidebarGroup.add(new SidebarItem(schema.getName()).addIcon(FontAwesomeIconManager.SCHEMA).setH5().setIndent0());

      // sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_structure(),
      // HistoryManager.linkToSchemaStructure(database.getUUID(), schema.getUUID()))
      // .addIcon(FontAwesomeIconManager.SCHEMA_STRUCTURE).setH6().setIndent1());
      //

//      DisclosurePanel panel = new DisclosurePanel("Teste");
//      panel.setOpen(true);
//      panel.setAnimationEnabled(true);
//      panel.getElement().setAttribute("style", "width:100%;");
//
//      panel.add(new SidebarHyperlink(messages.menusidebar_routines(),
//              HistoryManager.linkToSchemaRoutines(database.getUUID(), schema.getUUID()))
//              .addIcon(FontAwesomeIconManager.SCHEMA_ROUTINES).setH6().setIndent1());
//
//
//      sidebarGroup.add(panel);

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_routines(),
              HistoryManager.linkToSchemaRoutines(database.getUUID(), schema.getUUID()))
              .addIcon(FontAwesomeIconManager.SCHEMA_ROUTINES).setH6().setIndent1());

      for (ViewerRoutine routine : schema.getRoutines()) {
        sidebarGroup.add(new SidebarHyperlink(routine.getName(),
                HistoryManager.linkToRoutine(database.getUUID(), schema.getUUID(), routine.getUUID()))
                .addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2());
      }

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_views(),
        HistoryManager.linkToSchemaViews(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_VIEWS).setH6().setIndent1());

      for (ViewerView view : schema.getViews()) {
        sidebarGroup.add(new SidebarHyperlink(view.getName(),
          HistoryManager.linkToView(database.getUUID(), schema.getUUID(), view.getUUID()))
            .addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2());
      }

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_data(),
        HistoryManager.linkToSchemaData(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_DATA).setH6().setIndent1());

      for (ViewerTable table : schema.getTables()) {
        sidebarGroup
          .add(new SidebarHyperlink(table.getName(), HistoryManager.linkToTable(database.getUUID(), table.getUUID()))
            .addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2());
      }

      searchInit();
    }

    setVisible(true);
  }

  private void searchInit() {
    searchInputBox.getElement().setPropertyString("placeholder", messages.menusidebar_filterSidebar());

    searchInputBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        doSearch();
      }
    });

    searchInputBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });
  }

  private void doSearch() {
    String searchValue = searchInputBox.getValue();

    if (ViewerStringUtils.isBlank(searchValue)) {
      // show all
      for (Widget widget : sidebarGroup) {
        widget.setVisible(true);
      }
    } else {
      // show matching and their parents

      Set<SidebarItem> parentsThatShouldBeVisible = new HashSet<>();
      List<SidebarItem> parentsList = new ArrayList<>();

      for (Widget widget : sidebarGroup) {
        if (widget instanceof SidebarItem) {
          SidebarItem sidebarItem = (SidebarItem) widget;

          int indent = sidebarItem.getIndent();
          if (indent >= 0) {
            parentsList.add(indent, sidebarItem);
          }

          if (sidebarItem.getText().toLowerCase().contains(searchValue.toLowerCase())) {
            widget.setVisible(true);
            for (int i = 0; i < indent; i++) {
              parentsThatShouldBeVisible.add(parentsList.get(i));
            }
          } else {
            widget.setVisible(false);
          }
        } else {
          widget.setVisible(true);
        }
      }

      for (SidebarItem sidebarItem : parentsThatShouldBeVisible) {
        sidebarItem.setVisible(true);
      }
    }
  }
}
