package com.databasepreservation.common.client.common.visualization.activity.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.index.filter.Filter;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.ActivityLogList;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.common.search.SearchFieldPanel;
import com.databasepreservation.common.client.common.search.SearchPanel;
import com.databasepreservation.common.client.common.utils.AdvancedSearchUtils;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.ListboxUtils;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogPanel extends ContentPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static ActivityLogPanel getInstance() {
    if (instance == null) {
      instance = new ActivityLogPanel();
    }
    return instance;
  }

  interface ActivityLogPanelUiBinder extends UiBinder<Widget, ActivityLogPanel> {
  }

  private static ActivityLogPanelUiBinder uiBinder = GWT.create(ActivityLogPanelUiBinder.class);

  @UiField
  SimplePanel advancedSearch;

  @UiField
  SimplePanel mainHeader;

  @UiField
  SimplePanel description;

  @UiField(provided = true)
  ActivityLogList activityLogList;

  private static ActivityLogPanel instance = null;

  private SearchPanel searchPanel;
  private FlowPanel itemsSearchAdvancedFieldsPanel;
  private ListBox searchAdvancedFieldOptions;
  private final Map<String, SearchField> searchFields = new HashMap<>();

  private ActivityLogPanel() {
    activityLogList = new ActivityLogList(new Filter(),
      ClientConfigurationManager.FacetFactory.getFacets(ViewerConstants.ACTIVITY_LOG_PROPERTY), false, false);
    itemsSearchAdvancedFieldsPanel = new FlowPanel();
    searchAdvancedFieldOptions = new ListBox();
    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");

    initWidget(uiBinder.createAndBindUi(this));

    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.ACTIVITY_LOG),
      messages.activityLogMenuText(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.activityLogDescription());
    instance.setCSS("table-row-description", "font-size-description");

    description.setWidget(instance);

    activityLogList.getSelectionModel().addSelectionChangeHandler(event -> {
      ActivityLogEntry selected = activityLogList.getSelectionModel().getSelectedObject();
      if (selected != null) {
        activityLogList.getSelectionModel().clear();
        HistoryManager.gotoActivityLog(selected.getUuid());
      }
    });

    searchPanel = new SearchPanel(ViewerConstants.DEFAULT_FILTER, ViewerConstants.INDEX_SEARCH,
      messages.searchPlaceholder(), false, true);
    searchPanel.setList(activityLogList);
    searchPanel.setDefaultFilterIncremental(false);
    showSearchAdvancedFieldsPanel();

    advancedSearch.add(searchPanel);

    initAdvancedSearch();
  }

  public void showSearchAdvancedFieldsPanel() {
    searchPanel.setVariables(ViewerConstants.DEFAULT_FILTER, ViewerConstants.INDEX_SEARCH, activityLogList,
      itemsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
  }

  private void initAdvancedSearch() {
    final List<SearchField> searchFieldsForLog = AdvancedSearchUtils.getSearchFieldsForLog();

    for (SearchField searchField : searchFieldsForLog) {
      ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, searchField.getLabel(), searchField.getId());
      searchFields.put(searchField.getId(), searchField);
    }

    updateSearchFields(searchFieldsForLog);
  }

  private void updateSearchFields(List<SearchField> newSearchFields) {
    for (SearchField searchField : newSearchFields) {
      if (searchField.isFixed()) {
        final SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
        searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
        searchFieldPanel.setSearchFields(searchFields);
        addSearchFieldPanel(searchFieldPanel);
        searchFieldPanel.selectSearchField(searchField.getId());
      }
    }

    searchPanel.addSearchAdvancedFieldAddHandler(event -> {
      final SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
      searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
      searchFieldPanel.setSearchFields(searchFields);
      searchFieldPanel.selectFirstSearchField();
      addSearchFieldPanel(searchFieldPanel);
    });

  }

  private void addSearchFieldPanel(final SearchFieldPanel searchFieldPanel) {
    itemsSearchAdvancedFieldsPanel.add(searchFieldPanel);
    itemsSearchAdvancedFieldsPanel.removeStyleName("empty");

    searchPanel.setSearchAdvancedGoEnabled(true);
    searchPanel.setClearSearchButtonEnabled(true);

    ClickHandler clickHandler = event -> {
      itemsSearchAdvancedFieldsPanel.remove(searchFieldPanel);
      if (itemsSearchAdvancedFieldsPanel.getWidgetCount() == 0) {
        itemsSearchAdvancedFieldsPanel.addStyleName("empty");
        searchPanel.setSearchAdvancedGoEnabled(false);
        searchPanel.setClearSearchButtonEnabled(false);
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forActivityLog());
  }
}
