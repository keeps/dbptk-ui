package com.databasepreservation.common.client.common.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.lists.DatabaseSelectList;
import com.databasepreservation.common.client.common.lists.utils.AsyncTableCellOptions;
import com.databasepreservation.common.client.common.lists.utils.ListBuilder;
import com.databasepreservation.common.client.common.search.SearchPanelAbstract;
import com.databasepreservation.common.client.common.search.SearchPanelWithSearchAll;
import com.databasepreservation.common.client.common.search.SearchWrapper;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.OneOfManyFilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.select.SelectedItems;
import com.databasepreservation.common.client.index.select.SelectedItemsFilter;
import com.databasepreservation.common.client.index.select.SelectedItemsList;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class DatabaseSelectDialog extends DialogBox {
  private ClientMessages messages;
  private SearchWrapper databasesSearchWrapper;
  private SearchPanelWithSearchAll parentSearchPanel;
  private Filter defaultFilter;

  private long notLoaded;
  private long notSearchable;
  private SimplePanel exludedInfo;

  private SearchPanelAbstract searchPanel;
  private DatabaseSelectList list;

  public DatabaseSelectDialog() {
    super(true, true);
  }

  public <T extends IsIndexed> DatabaseSelectDialog(Filter defaultFilter, String allFilter, ClientMessages messages,
    SearchPanelWithSearchAll parentSearchPanel) {
    super(false, true);
    init(defaultFilter, allFilter, messages, parentSearchPanel);
  }

  public void init(Filter defaultFilter, String allFilter, ClientMessages messages,
    SearchPanelWithSearchAll parentSearchPanel) {
    setText(messages.manageDatabaseSearchAllSelectDatabases());

    FlowPanel layout = new FlowPanel();

    FlowPanel description = new FlowPanel();
    description.add(new SimplePanel(new Label(messages.manageDatabaseSearchAllAllowedInfo())));
    this.exludedInfo = new SimplePanel(new HTML());
    description.add(this.exludedInfo);
    this.resetExcludedInfo();
    description.add(new SimplePanel(new Label(messages.manageDatabaseSearchAllContactInfo())));

    ListBuilder<ViewerDatabase> databaseMetadataList = new ListBuilder<>(() -> {
      Filter available = new Filter();
      available
        .add(new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.AVAILABLE.name()));
      DatabaseSelectList metadataDatabaseList = new DatabaseSelectList(available, null, null, true, false);
      metadataDatabaseList.getSelectionModel().addSelectionChangeHandler(event -> {
        ViewerDatabase selected = metadataDatabaseList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryManager.gotoSIARDInfo(selected.getUuid());
          metadataDatabaseList.getSelectionModel().clear();
        }
        list = metadataDatabaseList;
      });

      return metadataDatabaseList;
    }, new AsyncTableCellOptions<>(ViewerDatabase.class, "DatabaseList_metadata"));
    this.parentSearchPanel = parentSearchPanel;
    this.defaultFilter = defaultFilter;
    this.messages = messages;
    this.databasesSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(databaseMetadataList, false);
    this.searchPanel = this.databasesSearchWrapper.getComponents()
      .getSearchPanel(databaseMetadataList.getOptions().getListId());
    Button clearButton = new Button(messages.clear());
    clearButton.addStyleName("btn btn-danger");
    clearButton.addClickHandler(event -> clearHandler());
    Button cancelButton = new Button(messages.basicActionCancel());
    cancelButton.addStyleName("btn btn-link");
    cancelButton.addClickHandler(event -> cancelHandler());
    Button selectButton = new Button(messages.basicActionSelect());
    selectButton.addStyleName("btn btn-primary");
    selectButton.addClickHandler(event -> confirmHandler());
    FlowPanel buttonsRow = new FlowPanel();
    FlowPanel buttonsRowLeft = new FlowPanel();
    FlowPanel buttonsRowRight = new FlowPanel();

    buttonsRowLeft.add(clearButton);
    buttonsRowLeft.addStyleName("searchall-button-row-left");
    buttonsRowRight.add(cancelButton);
    buttonsRowRight.add(selectButton);
    buttonsRowRight.addStyleName("searchall-button-row-right");
    buttonsRow.add(buttonsRowLeft);
    buttonsRow.add(buttonsRowRight);
    buttonsRow.addStyleName("searchall-button-row");

    layout.add(description);
    layout.add(databasesSearchWrapper);
    layout.add(buttonsRow);
    layout.addStyleName("wui-dialog-layout");

    setWidget(layout);
    setWidth("1400px");
    addStyleName("wui-dialog-confirm");
    setGlassEnabled(true);

    doParentSearch();
  }

  public void resetExcludedInfo() {
    FindRequest notLoadedDatabasesRequest = new FindRequest(ViewerDatabase.class.getName(),
      new Filter(
        new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.METADATA_ONLY.name())),
      Sorter.NONE, new Sublist(), Facets.NONE, false, Collections.singletonList(ViewerConstants.INDEX_ID));
    DatabaseService.Util.call((IndexResult<ViewerDatabase> result) -> {
      this.notLoaded = result.getTotalCount();
      this.updateExcludedInfo();
    }).find(notLoadedDatabasesRequest, LocaleInfo.getCurrentLocale().getLocaleName());

    FindRequest notSearchableDatabasesRequest = new FindRequest(ViewerDatabase.class.getName(),
      new Filter(new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL, "false")),
      Sorter.NONE, new Sublist(), Facets.NONE, false, Collections.singletonList(ViewerConstants.INDEX_ID));
    DatabaseService.Util.call((IndexResult<ViewerDatabase> result) -> {
      this.notSearchable = result.getTotalCount();
      this.updateExcludedInfo();
    }).find(notSearchableDatabasesRequest, LocaleInfo.getCurrentLocale().getLocaleName());
  }

  public void updateExcludedInfo() {
    this.exludedInfo
      .setWidget(new HTML(SafeHtmlUtils.fromSafeConstant(messages.manageDatabaseSearchAllExcludedLoaded(this.notLoaded)
        + " " + messages.manageDatabaseSearchAllExcludedPrivacy(this.notSearchable))));
  }

  public void writeSelectedObjectsToSessionStorage() {
    Storage sessionStorage = Storage.getSessionStorageIfSupported();
    if (sessionStorage != null) {
      SelectedItems<ViewerDatabase> selectedItems = this.list.getSelected();
      if (selectedItems instanceof SelectedItemsList<?>) {
        sessionStorage.setItem(ViewerConstants.LOCAL_STORAGE_SEARCH_ALL_SELECTION,
          String.join(",", ((SelectedItemsList<ViewerDatabase>) selectedItems).getIds()));
      } else if (selectedItems instanceof SelectedItemsFilter<?>) {
        sessionStorage.setItem(ViewerConstants.LOCAL_STORAGE_SEARCH_ALL_SELECTION,
          ViewerConstants.SEARCH_ALL_SELECTED_ALL);
      }
    }
  }

  private void confirmHandler() {
    this.writeSelectedObjectsToSessionStorage();
    this.doParentSearch();
    this.hide();
  }

  private void cancelHandler() {
    Storage sessionStorage = Storage.getSessionStorageIfSupported();
    if (sessionStorage != null) {
      String uuidsString = sessionStorage.getItem(ViewerConstants.LOCAL_STORAGE_SEARCH_ALL_SELECTION);
      if (uuidsString != null && !uuidsString.equals(ViewerConstants.SEARCH_ALL_SELECTED_ALL)) {
        List<String> selectedUUIDs = new ArrayList<>();
        if (!uuidsString.isEmpty()) {
          Collections.addAll(selectedUUIDs, uuidsString.split(","));
        }
        this.list.setSelectedByUUIDs(selectedUUIDs);
      } else {
        this.list.setSelectedByUUIDs(new ArrayList<>());
      }
    }
    this.hide();
  }

  private void clearHandler() {
    this.searchPanel.clearSearchInputBox();
    this.list.setSelectingAll(false);
    this.list.setSelected(new HashSet<>());
  }

  private void doParentSearch() {
    Storage sessionStorage = Storage.getSessionStorageIfSupported();
    List<String> selectedUUIDs = null;
    boolean selectedAll = false;
    if (sessionStorage != null) {
      String uuidsString = sessionStorage.getItem(ViewerConstants.LOCAL_STORAGE_SEARCH_ALL_SELECTION);
      if (uuidsString != null) {
        if (uuidsString.equals(ViewerConstants.SEARCH_ALL_SELECTED_ALL)) {
          selectedAll = true;
        } else {
          selectedUUIDs = new ArrayList<>();
          if (!uuidsString.isEmpty()) {
            Collections.addAll(selectedUUIDs, uuidsString.split(","));
          }
        }
      }
    }
    if (selectedUUIDs != null) {
      Filter newFilter = new Filter(this.defaultFilter);
      newFilter.add(new OneOfManyFilterParameter(ViewerConstants.SOLR_ROWS_DATABASE_UUID, selectedUUIDs));
      this.parentSearchPanel.setSearchAllTotalDatabases(String.valueOf(selectedUUIDs.size()));
      this.parentSearchPanel.setDefaultFilter(newFilter);
      this.parentSearchPanel.doSearch();
    } else if (selectedAll || ClientConfigurationManager.getStringWithDefault(ViewerConstants.SEARCH_ALL_SELECTED_ALL,
      ViewerConstants.PROPERTY_SEARCH_ALL_DEFAULT_SELECTION).equals(ViewerConstants.SEARCH_ALL_SELECTED_ALL)) {
      this.parentSearchPanel.setSearchAllTotalDatabases("all");
      this.parentSearchPanel.setDefaultFilter(defaultFilter);
      this.parentSearchPanel.doSearch();
    } else {
      this.parentSearchPanel.setSearchAllTotalDatabases("0");
      this.parentSearchPanel.setDefaultFilter(defaultFilter);
      this.parentSearchPanel.doSearch();
    }
  }
}
