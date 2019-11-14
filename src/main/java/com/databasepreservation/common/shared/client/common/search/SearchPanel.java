/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.shared.client.common.search;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.common.lists.AsyncTableCell;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchPanel extends Composite implements HasValueChangeHandlers<String> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String FILTER_ICON = "<i class='fa fa-filter' aria-hidden='true'></i>";

  // private static final BrowseMessages messages =
  // GWT.create(BrowseMessages.class);

  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SearchPanel> {
  }

  @UiField
  FlowPanel searchPanel;

  @UiField
  Dropdown searchInputListBox;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  AccessibleFocusPanel searchAdvancedDisclosureButton;

  @UiField
  FlowPanel searchAdvancedPanel;

  @UiField
  FlowPanel searchAdvancedPanelButtons;

  @UiField
  Button searchAdvancedFieldOptionsAdd;

  @UiField
  Button searchAdvancedGo;

  @UiField
  Button clearSearchButton;

  @UiField
  Button saveSearchButton;

  @UiField
  FlowPanel searchPreFilters;

  private Filter defaultFilter;
  private String allFilter;
  private boolean defaultFilterIncremental = false;
  private AsyncCallback<Void> saveQueryCallback;
  private FlowPanel fieldsPanel;
  private AsyncTableCell<?, ?> list;

  public SearchPanel(Filter defaultFilter, String allFilter, String placeholder, boolean showSearchInputListBox,
    boolean showSearchAdvancedDisclosureButton, final AsyncCallback<Void> saveQueryCallback) {
    this.saveQueryCallback = saveQueryCallback;
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;

    initWidget(binder.createAndBindUi(this));

    if (placeholder != null) {
      searchInputBox.getElement().setPropertyString("placeholder", placeholder);
    }

    searchInputListBox.setVisible(showSearchInputListBox);
    searchAdvancedDisclosureButton.setVisible(showSearchAdvancedDisclosureButton);
    searchAdvancedPanel.setVisible(false);

    searchInputBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        doSearch();
      }
    });
    searchInputButton.addClickHandler(event -> doSearch());
    searchAdvancedDisclosureButton.addClickHandler(event -> showSearchAdvancedPanel());
    searchInputListBox.addValueChangeHandler(event -> onChange());
    if (showSearchAdvancedDisclosureButton) {
      searchPanel.addStyleName("searchPanelAdvanced");
    }

    saveSearchReset();

    searchPreFilters.setVisible(!defaultFilter.getParameters().isEmpty());
    drawSearchPreFilters();
  }

  private void drawSearchPreFilters() {
    searchPreFilters.clear();

    for (FilterParameter parameter : defaultFilter.getParameters()) {
      // HTML header = new HTML(SafeHtmlUtils.fromSafeConstant(FILTER_ICON));
      // header.addStyleName("inline gray");
      // searchPreFilters.add(header);

      HTML html = null;

      /*
       * Unused in DBViewer, for now if (parameter instanceof SimpleFilterParameter) {
       * SimpleFilterParameter p = (SimpleFilterParameter) parameter; html = new
       * HTML(messages.searchPreFilterSimpleFilterParameter
       * (messages.searchPreFilterName(p.getName()),
       * messages.searchPreFilterValue(p.getValue()))); } else if (parameter
       * instanceof BasicSearchFilterParameter) { BasicSearchFilterParameter p =
       * (BasicSearchFilterParameter) parameter; // TODO put '*' in some constant, see
       * Search if (!"*".equals(p.getValue())) { html = new HTML(messages
       * .searchPreFilterBasicSearchFilterParameter(messages.searchPreFilterName
       * (p.getName()), messages.searchPreFilterValue(p.getValue()))); } } else if
       * (parameter instanceof NotSimpleFilterParameter) { NotSimpleFilterParameter p
       * = (NotSimpleFilterParameter) parameter; html = new
       * HTML(messages.searchPreFilterNotSimpleFilterParameter(messages.
       * searchPreFilterName(p.getName()),
       * messages.searchPreFilterValue(p.getValue()))); } else if (parameter
       * instanceof EmptyKeyFilterParameter) { EmptyKeyFilterParameter p =
       * (EmptyKeyFilterParameter) parameter; html = new
       * HTML(messages.searchPreFilterEmptyKeyFilterParameter
       * (messages.searchPreFilterName(p.getName()))); } else { html = new
       * HTML(SafeHtmlUtils.fromString(parameter.getClass().getSimpleName())); }
       */

      if (html != null) {
        html.addStyleName("xsmall gray inline nowrap");
        searchPreFilters.add(html);
      }
    }
  }

  public void doSearch() {
    Filter filter = buildSearchFilter(searchInputBox.getText(), defaultFilter, allFilter, fieldsPanel,
      defaultFilterIncremental);
    list.setFilter(filter);
  }

  private Filter buildSearchFilter(String basicQuery, Filter defaultFilter, String allFilter, FlowPanel fieldsPanel,
    boolean defaultFilterIncremental) {
    List<FilterParameter> parameters = new ArrayList<>();

    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(allFilter, basicQuery));
    }

    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
        FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

        if (filterParameter != null) {
          parameters.add(filterParameter);
        }
      }
    }

    Filter filter;
    if (defaultFilterIncremental) {
      filter = new Filter(defaultFilter);
      filter.add(parameters);
      searchPreFilters.setVisible(!defaultFilter.getParameters().isEmpty());
      GWT.log("Incremental filter: " + filter);
    } else if (parameters.isEmpty()) {
      filter = defaultFilter;
      searchPreFilters.setVisible(!defaultFilter.getParameters().isEmpty());
      GWT.log("Default filter: " + filter);
    } else {
      filter = new Filter(parameters);
      searchPreFilters.setVisible(false);
      GWT.log("New filter: " + filter);
    }

    return filter;
  }

  public String getDropdownSelectedValue() {
    return searchInputListBox.getSelectedValue();
  }

  public void setDropdownLabel(String label) {
    searchInputListBox.setLabel(label);
  }

  public void addDropdownItem(String label, String value) {
    searchInputListBox.addItem(label, value);
  }

  private void showSearchAdvancedPanel() {
    searchAdvancedPanel.setVisible(!searchAdvancedPanel.isVisible());
    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    } else {
      searchAdvancedDisclosureButton.removeStyleName("open");
    }
  }

  public void openSearchAdvancedPanel() {
    if (!searchAdvancedPanel.isVisible()) {
      searchAdvancedPanel.setVisible(true);
    }

    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    }
  }

  public void addDropdownPopupStyleName(String styleName) {
    searchInputListBox.addPopupStyleName(styleName);
  }

  public void setFieldsPanel(FlowPanel fieldsPanel) {
    this.fieldsPanel = fieldsPanel;
    searchAdvancedPanel.clear();
    searchAdvancedPanel.add(fieldsPanel);
    searchAdvancedPanel.add(searchAdvancedPanelButtons);
  }

  public void setList(AsyncTableCell<?, ?> list) {
    this.list = list;
  }

  public void setDefaultFilter(Filter defaultFilter) {
    this.defaultFilter = defaultFilter;
    drawSearchPreFilters();
  }

  public void setAllFilter(String allFilter) {
    this.allFilter = allFilter;
  }

  public void setVariables(Filter defaultFilter, String allFilter, AsyncTableCell<?, ?> list, FlowPanel fieldsPanel) {
    setDefaultFilter(defaultFilter);
    setAllFilter(allFilter);
    setList(list);
    setFieldsPanel(fieldsPanel);
  }

  public void setDefaultFilterIncremental(boolean defaultFilterIncremental) {
    this.defaultFilterIncremental = defaultFilterIncremental;
  }

  public void clearSearchInputBox() {
    searchInputBox.setText("");
  }

  public void clearAdvancedSearchInputBox() {
    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
        searchAdvancedFieldPanel.clear();
      }
    }
  }

  public void setSearchAdvancedFieldOptionsAddVisible(boolean visible) {
    searchAdvancedFieldOptionsAdd.setVisible(visible);
  }

  public void setSearchAdvancedGoEnabled(boolean enabled) {
    searchAdvancedGo.setEnabled(enabled);
  }

  public void setClearSearchButtonEnabled(boolean enabled) {
    clearSearchButton.setEnabled(enabled);
  }

  public void addSearchAdvancedFieldAddHandler(ClickHandler handler) {
    searchAdvancedFieldOptionsAdd.addClickHandler(handler);
  }

  @UiHandler("searchAdvancedGo")
  void handleSearchAdvancedGo(ClickEvent e) {
    doSearch();
  }

  @UiHandler("clearSearchButton")
  void handleClearSearchButton(ClickEvent e) {
    clearAdvancedSearchInputBox();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, searchInputListBox.getSelectedValue());
  }

  public void updateSearchPanel(SearchInfo searchInfo) {
    if (SearchInfo.isPresentAndValid(searchInfo)) {
      this.defaultFilter = searchInfo.getDefaultFilter();
      this.searchInputBox.setText(searchInfo.getCurrentFilter());

      openSearchAdvancedPanel();
      if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
        for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
          searchAdvancedFieldPanel.setInputFromFilterParam(searchInfo.getFieldParameters().get(i));
        }
      }

      doSearch();
    }
  }

  public Filter getDefaultFilter() {
    return defaultFilter;
  }

  public String getCurrentFilter() {
    return searchInputBox.getText();
  }

  public List<FilterParameter> getAdvancedSearchFilterParameters() {
    List<FilterParameter> parameters = new ArrayList<>();

    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);

        parameters.add(searchAdvancedFieldPanel.getFilter());
      }
    }

    return parameters;
  }

  public List<SearchField> getAdvancedSearchSearchFields() {
    List<SearchField> searchFields = new ArrayList<>();

    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);

        searchFields.add(searchAdvancedFieldPanel.getSearchField());
      }
    }

    return searchFields;
  }

  private void saveSearchReset() {
    saveSearchButton.setText(messages.saveSearch());
    saveSearchButton.addStyleName("searchPanelAdvancedSaveSearchButton");
    saveSearchButton.setEnabled(saveQueryCallback != null);
    saveSearchButton.setVisible(saveQueryCallback != null);
  }

  @UiHandler("saveSearchButton")
  void saveSearchOpenPanel(ClickEvent e) {
    saveSearchButton.setEnabled(false);
    saveSearchButton.setText(messages.saving());
    saveQueryCallback.onSuccess(null);
  }

  public void querySavedHandler(boolean succeeded, ViewerDatabase database, String savedSearchUUID) {
    saveSearchReset();

    if (succeeded) {
      HistoryManager.gotoEditSavedSearch(database.getUUID(), savedSearchUUID);
    }
  }
}
