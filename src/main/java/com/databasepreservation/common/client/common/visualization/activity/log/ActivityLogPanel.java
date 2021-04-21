/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.activity.log;

import java.util.List;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.lists.ActivityLogList;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.common.search.SearchPanel;
import com.databasepreservation.common.client.common.search.panel.SearchFieldPanel;
import com.databasepreservation.common.client.common.search.panel.SearchFieldPanelFactory;
import com.databasepreservation.common.client.common.utils.AdvancedSearchUtils;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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
  FlowPanel mainHeader;

  @UiField
  SimplePanel description;

  @UiField(provided = true)
  ActivityLogList activityLogList;

  private static ActivityLogPanel instance = null;

  private SearchPanel searchPanel;
  private FlowPanel itemsSearchAdvancedFieldsPanel;

  private ActivityLogPanel() {
    activityLogList = new ActivityLogList(new Filter(),
      ClientConfigurationManager.FacetFactory.getFacets(ViewerConstants.ACTIVITY_LOG_PROPERTY), false, false);

    initWidget(uiBinder.createAndBindUi(this));

    initHeader();

    itemsSearchAdvancedFieldsPanel = new FlowPanel();
    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");

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
    advancedSearch.add(searchPanel);

    initAdvancedSearch();
    showSearchAdvancedFieldsPanel();
  }

  private void initHeader() {
    mainHeader.add(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.ACTIVITY_LOG),
      messages.activityLogMenuText(), "h1"));

    HTML html = new HTML(messages.activityLogDescription());
    html.addStyleName("font-size-description");

    description.setWidget(html);
  }

  public void showSearchAdvancedFieldsPanel() {
    searchPanel.setVariables(ViewerConstants.DEFAULT_FILTER, ViewerConstants.INDEX_SEARCH, activityLogList,
      itemsSearchAdvancedFieldsPanel);
  }

  private void initAdvancedSearch() {
    final List<SearchField> searchFieldsForLog = AdvancedSearchUtils.getSearchFieldsForLog();

    searchFieldsForLog.forEach(searchField -> {
      if (searchField.isFixed()) {
        final SearchFieldPanel searchFieldPanel = SearchFieldPanelFactory.getSearchFieldPanel(searchField);
        searchFieldPanel.setup();
        addSearchFieldPanel(searchFieldPanel);
      }
    });
  }

  private void addSearchFieldPanel(final SearchFieldPanel searchFieldPanel) {
    itemsSearchAdvancedFieldsPanel.add(searchFieldPanel);
    itemsSearchAdvancedFieldsPanel.removeStyleName("empty");

    searchPanel.setSearchAdvancedGoEnabled(true);
    searchPanel.setClearSearchButtonEnabled(true);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forActivityLog());
  }
}
