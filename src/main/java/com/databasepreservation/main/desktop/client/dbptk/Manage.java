package com.databasepreservation.main.desktop.client.dbptk;

import java.util.List;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.databasepreservation.main.desktop.client.common.helper.HelperUploadSIARDFile;
import com.databasepreservation.main.desktop.client.common.lists.DatabaseList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Manage extends Composite {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface ManageUiBinder extends UiBinder<Widget, Manage> {
  }

  private static ManageUiBinder binder = GWT.create(ManageUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField(provided = true)
  DatabaseList databaseList;

  @UiField
  Button create, open;

  private static Manage instance = null;

  public static Manage getInstance() {
    if (instance == null) {
      instance = new Manage();
    }
    return instance;
  }

  private Manage() {

    databaseList = new DatabaseList();

    initWidget(binder.createAndBindUi(this));

    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forManageSIARD();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

    searchInputBox.getElement().setPropertyString("placeholder", messages.searchPlaceholder());

    searchInputBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          doSearch();
        }
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });

    databaseList.getSelectionModel().addSelectionChangeHandler(event -> {
      ViewerDatabase selected = databaseList.getSelectionModel().getSelectedObject();
      if (selected != null) {
        if (ViewerDatabase.Status.INGESTING.equals(selected.getStatus())) {

        } else {
          databaseList.getSelectionModel().clear();
        }
      }
    });

    initButtons();
  }

  private void initButtons() {
    create.addClickHandler(event -> HistoryManager.gotoCreateSIARD());

    open.addClickHandler(event -> new HelperUploadSIARDFile().openFile(databaseList));
  }

  private void doSearch() {
    // start searching
    Filter filter;
    String searchText = searchInputBox.getText();
    if (ViewerStringUtils.isBlank(searchText)) {
      filter = ViewerConstants.DEFAULT_FILTER;
    } else {
      filter = new Filter(new BasicSearchFilterParameter(ViewerConstants.INDEX_SEARCH, searchText));
    }

    databaseList.setFilter(filter);
  }

  /**
   * This method is called immediately after a widget becomes attached to the
   * browser's document.
   */
  @Override
  protected void onLoad() {
    super.onLoad();
    databaseList.getSelectionModel().clear();
  }
}