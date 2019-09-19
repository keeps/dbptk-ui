package com.databasepreservation.main.common.shared.client.common.sidebar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class DatabaseSidebarAbstract extends Composite {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  protected static Map<String, DatabaseSidebarAbstract> instances = new HashMap<>();

  interface DatabaseSidebarAbstractUiBinder extends UiBinder<Widget, DatabaseSidebarAbstract> {
  }

  protected static DatabaseSidebarAbstractUiBinder uiBinder = GWT.create(DatabaseSidebarAbstractUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  @UiField
  FlowPanel searchPanel;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  protected ViewerDatabase database;
  protected String databaseUUID;
  protected boolean initialized = false;

  public boolean isInitialized() {
    return initialized;
  }

  public void init(ViewerDatabase db) {
    GWT.log("init with db: " + db + "; status: " + db.getStatus().toString());
    if (ViewerDatabase.Status.AVAILABLE.equals(db.getStatus())) {
      if (db != null && (databaseUUID == null || databaseUUID.equals(db.getUUID()))) {
        initialized = true;
        database = db;
        databaseUUID = db.getUUID();
        init();
      }
    }
  }

  public void init() {

  }

  protected void searchInit() {
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
