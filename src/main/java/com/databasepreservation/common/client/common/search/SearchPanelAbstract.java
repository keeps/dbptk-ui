package com.databasepreservation.common.client.common.search;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.common.lists.utils.AsyncTableCell;
import com.databasepreservation.common.client.common.search.panel.SearchFieldPanel;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public abstract class SearchPanelAbstract extends Composite implements HasValueChangeHandlers<String> {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  protected static final String FILTER_ICON = "<i class='fa fa-filter' aria-hidden='true'></i>";

  @UiField
  FlowPanel searchPanel;

  @UiField
  FlowPanel searchContextPanel;

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
  Button searchAdvancedGo;

  @UiField
  Button clearSearchButton;

  @UiField
  Button saveSearchButton;

  @UiField
  FlowPanel searchPreFilters;

  @UiField
  SimplePanel searchPanelSelectionDropdownWrapper;

  protected Filter defaultFilter;
  protected String allFilter;
  protected boolean defaultFilterIncremental = false;
  protected AsyncCallback<Void> saveQueryCallback;
  protected FlowPanel fieldsPanel;
  protected AsyncTableCell<?, ?> list;

  protected SearchPanelAbstract(Filter defaultFilter, String allFilter, String placeholder,
    boolean showSearchInputListBox, boolean showSearchAdvancedDisclosureButton) {
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;

    bindUIAndInitWidget();

    if (placeholder != null) {
      searchInputBox.getElement().setPropertyString("placeholder", placeholder);
    }

    searchInputListBox.setVisible(showSearchInputListBox);
    searchAdvancedDisclosureButton.setVisible(showSearchAdvancedDisclosureButton);
    searchAdvancedPanel.setVisible(false);
    searchContextPanel.setVisible(false);

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
  }

  protected SearchPanelAbstract(Filter defaultFilter, String allFilter, String placeholder, String context,
    boolean showSearchAdvancedDisclosureButton, final AsyncCallback<Void> saveQueryCallback) {
    this(defaultFilter, allFilter, placeholder, false, showSearchAdvancedDisclosureButton, saveQueryCallback);
    searchContextPanel.setVisible(true);
    searchContextPanel.add(new HTML(FontAwesomeIconManager.loaded(FontAwesomeIconManager.TABLE, context)));
  }

  protected SearchPanelAbstract(Filter defaultFilter, String allFilter, String placeholder,
    boolean showSearchInputListBox, boolean showSearchAdvancedDisclosureButton,
    final AsyncCallback<Void> saveQueryCallback) {
    this.saveQueryCallback = saveQueryCallback;
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;

    bindUIAndInitWidget();

    if (placeholder != null) {
      searchInputBox.getElement().setPropertyString("placeholder", placeholder);
    }

    searchInputListBox.setVisible(showSearchInputListBox);
    searchAdvancedDisclosureButton.setVisible(showSearchAdvancedDisclosureButton);
    searchContextPanel.setVisible(false);
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
  }

  public abstract void bindUIAndInitWidget();

  public void doSearch() {
    Filter filter = buildSearchFilter(searchInputBox.getText(), defaultFilter, allFilter, fieldsPanel,
      defaultFilterIncremental);
    list.setFilter(filter);
  }

  public void attachSearchPanelSelectionDropdown(Dropdown dropdown) {
    searchPanelSelectionDropdownWrapper.setWidget(dropdown);
  }

  private Filter buildSearchFilter(String basicQuery, Filter defaultFilter, String allFilter, FlowPanel fieldsPanel,
    boolean defaultFilterIncremental) {
    List<FilterParameter> parameters = new ArrayList<>();

    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(allFilter, basicQuery));
    }

    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        if (fieldsPanel.getWidget(i) instanceof SearchFieldPanel) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
          FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

          if (filterParameter != null) {
            parameters.add(filterParameter);
          }
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
  }

  public void setDefaultFilter(Filter defaultFilter, boolean defaultFilterIncremental) {
    this.defaultFilter = defaultFilter;
    this.defaultFilterIncremental = defaultFilterIncremental;
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

  public void setSearchPanelSelectionDropdownWrapperVisible(boolean value) {
    this.searchPanelSelectionDropdownWrapper.setVisible(value);
  }

  public void clearAdvancedSearchInputBox() {
    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        if (fieldsPanel.getWidget(i) instanceof SearchFieldPanel) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
          searchAdvancedFieldPanel.clear();
        }
      }
    }
  }

  public void setSearchAdvancedGoEnabled(boolean enabled) {
    searchAdvancedGo.setEnabled(enabled);
  }

  public void setClearSearchButtonEnabled(boolean enabled) {
    clearSearchButton.setEnabled(enabled);
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

      GWT.log("search info: " + searchInfo.asJson());
      int fieldParameterIndex = 0;
      if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
        for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
          if (fieldsPanel.getWidget(i) instanceof SearchFieldPanel) {
            SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
            FilterParameter filterParameter;
            try {
              filterParameter = searchInfo.getFieldParameters().get(fieldParameterIndex);
            } catch (IndexOutOfBoundsException e) {
              filterParameter = null;
            }
            if (filterParameter != null) {
              searchAdvancedFieldPanel.setInputFromFilterParam(filterParameter);
            }
            fieldParameterIndex++;
          }
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
        if (fieldsPanel.getWidget(i) instanceof SearchFieldPanel) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
          parameters.add(searchAdvancedFieldPanel.getFilter());
        }
      }
    }

    return parameters;
  }

  public List<SearchField> getAdvancedSearchSearchFields() {
    List<SearchField> searchFields = new ArrayList<>();

    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        if (fieldsPanel.getWidget(i) instanceof SearchFieldPanel) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
          searchFields.add(searchAdvancedFieldPanel.getSearchField());
        }
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
      HistoryManager.gotoEditSavedSearch(database.getUuid(), savedSearchUUID);
    }
  }
}
