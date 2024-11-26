package com.databasepreservation.common.client.common.search;

import java.util.LinkedHashMap;
import java.util.Map;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.lists.utils.AsyncTableCell;
import com.databasepreservation.common.client.common.lists.utils.ListBuilder;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.filter.Filter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SearchWrapper extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final boolean hasMultipleSearchPanels;
  private final String preselectedDropdownValue;

  private final FlowPanel rootPanel;

  // this being not null means that lists should be created inside a ScrollPanel
  // and that the ScrollPanel should be using the specified CSS classes
  private String scrollPanelCssClasses = null;

  private Dropdown searchPanelSelectionDropdown;

  private HandlerRegistration searchAllButtonValueChangeHandler;

  private final Components components;

  private String currentClassSimpleName;

  public SearchWrapper(boolean hasMultipleSearchPanels, String preselectedDropdownValue) {
    this.searchPanelSelectionDropdown = null;
    this.hasMultipleSearchPanels = hasMultipleSearchPanels;
    this.preselectedDropdownValue = preselectedDropdownValue;
    this.components = new Components();

    rootPanel = new FlowPanel();
    initWidget(rootPanel);
  }

  public SearchWrapper(boolean hasMultipleSearchPanels) {
    this(hasMultipleSearchPanels, null);
  }

  public <T extends IsIndexed> SearchWrapper createListAndSearchPanel(ListBuilder<T> listBuilder, boolean isSearchAll) {
    AsyncTableCell<T, Void> list = listBuilder.build();

    SearchPanelAbstract searchPanel;

    Filter filter = list.getFilter();
    String allFilter = SearchFilters.searchField();
    boolean incremental = SearchFilters.shouldBeIncremental(filter) || isSearchAll;

    // get configuration

    boolean searchEnabled = true;

    String defaultLabelText = ClientConfigurationManager.resolveTranslation(ViewerConstants.UI_LISTS_PROPERTY,
      listBuilder.getOptions().getListId(), ViewerConstants.UI_LISTS_SEARCH_SELECTEDINFO_LABEL_DEFAULT_I18N_PROPERTY);

    if (defaultLabelText == null) {
      defaultLabelText = messages.someOfAObject(listBuilder.getOptions().getClassToReturn().getName());
    }

    String dropdownValue = listBuilder.getOptions().getListId();

    // create
    if (isSearchAll) {
      searchPanel = new SearchPanelWithSearchAll(filter, allFilter, messages.searchPlaceholder(),
        hasMultipleSearchPanels, false);
    } else {
      searchPanel = new SearchPanel(filter, allFilter, messages.searchPlaceholder(), hasMultipleSearchPanels, false);
    }
    searchPanel.setList(list);
    searchPanel.setDefaultFilterIncremental(incremental);
    searchPanel.setSearchPanelSelectionDropdownWrapperVisible(hasMultipleSearchPanels);
    if (hasMultipleSearchPanels) {
      initSearchPanelSelectionDropdown();
      searchPanelSelectionDropdown.addItem(defaultLabelText, dropdownValue);
    }
    if (isSearchAll) {
      ((SearchPanelWithSearchAll) searchPanel).initSearchAll();
    }
    searchPanel.setVisible(searchEnabled);

    components.put(listBuilder.getOptions().getListId(), searchPanel, list);

    // add search panel if none has been added yet, note that if there is a
    // preselectedDropdownValue then only the corresponding search panel should be
    // used as the default search panel
    if (rootPanel.getWidgetCount() == 0) {
      if (preselectedDropdownValue != null) {
        if (preselectedDropdownValue.equals(dropdownValue)) {
          attachComponents(dropdownValue);
        }
      } else {
        attachComponents(dropdownValue);
      }
    }

    return this;
  }

  private void initSearchPanelSelectionDropdown() {
    if (searchPanelSelectionDropdown == null) {
      searchPanelSelectionDropdown = new Dropdown();
      searchPanelSelectionDropdown.addStyleName("searchInputListBox");
      searchPanelSelectionDropdown.addPopupStyleName("searchInputListBoxPopup");
      searchPanelSelectionDropdown.addValueChangeHandler(event -> attachComponents(event.getValue()));
    }
  }

  private void addSearchAllSelectionChangeHandler(SearchPanelWithSearchAll panel) {
    this.searchAllButtonValueChangeHandler = panel.getSearchAllButtonWrapper()
      .addValueChangeHandler(event -> onSearchAllSelectionChanged(event.getValue()));
  }

  private void dettachSearchAllSelectionChangeHandler() {
    if (searchAllButtonValueChangeHandler != null) {
      this.searchAllButtonValueChangeHandler.removeHandler();
      this.searchAllButtonValueChangeHandler = null;
    }
  }

  private <T extends IsIndexed> void attachComponents(String objectClassSimpleName) {
    SearchPanelAbstract searchPanel = components.getSearchPanel(objectClassSimpleName);
    AsyncTableCell<T, Void> list = components.getList(objectClassSimpleName);

    this.currentClassSimpleName = objectClassSimpleName;

    rootPanel.clear();
    rootPanel.add(searchPanel);

    if (!(searchPanel instanceof SearchPanelWithSearchAll)
      || !((SearchPanelWithSearchAll) searchPanel).getTotalSelected().equals("0")) {
      if (scrollPanelCssClasses != null) {
        ScrollPanel scrollPanel = new ScrollPanel(list);
        scrollPanel.addStyleName(scrollPanelCssClasses);
        rootPanel.add(scrollPanel);
      } else {
        rootPanel.add(list);
      }
    } else {
      SimplePanel messagePanel = new SimplePanel(
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.manageDatabaseSearchAllNoneSelected())));
      messagePanel.addStyleName("searchall-warning");
      rootPanel.add(messagePanel);
    }

    if (hasMultipleSearchPanels) {
      searchPanelSelectionDropdown.setSelectedValue(objectClassSimpleName, false);
      searchPanel.attachSearchPanelSelectionDropdown(searchPanelSelectionDropdown);
    }

    dettachSearchAllSelectionChangeHandler();
    if (searchPanel instanceof SearchPanelWithSearchAll) {
      addSearchAllSelectionChangeHandler((SearchPanelWithSearchAll) searchPanel);
    }
  }

  private <T extends IsIndexed> void reattachComponentsWithErrorMessage(HTML message) {
    SearchPanelAbstract searchPanel = components.getSearchPanel(this.currentClassSimpleName);

    rootPanel.clear();
    rootPanel.add(searchPanel);
    SimplePanel messagePanel = new SimplePanel(message);
    messagePanel.addStyleName("searchall-warning");
    rootPanel.add(messagePanel);
  }

  public Components getComponents() {
    return components;
  }

  public void onSearchAllSelectionChanged(String totalSelected) {
    if (totalSelected.equals("0")) {
      reattachComponentsWithErrorMessage(
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.manageDatabaseSearchAllNoneSelected())));
    } else {
      attachComponents(currentClassSimpleName);
    }
  }

  /**
   * Auxiliary manager for inner components (groups of one searchWrapper and one
   * BasicAsyncTableCell, at least for now) that is used to enforce type coherence
   */
  @SuppressWarnings("unchecked")
  public class Components {
    private final Map<String, SearchPanelAbstract> searchPanels = new LinkedHashMap<>();
    private final Map<String, AsyncTableCell<? extends IsIndexed, Void>> lists = new LinkedHashMap<>();

    /**
     * Add a new set of components associated with a class.
     *
     * @param listId
     *          the listId to associate the components with
     * @param searchPanel
     *          the searchWrapper component
     * @param list
     *          the BasicAsyncTableCell component
     * @param <T>
     *          extends IsIndexed, type parameter shared by this set of components
     */
    public <T extends IsIndexed> void put(String listId, SearchPanelAbstract searchPanel,
      AsyncTableCell<T, Void> list) {
      searchPanels.put(listId, searchPanel);
      lists.put(listId, list);
    }

    public SearchPanelAbstract getSearchPanel(String listId) {
      return searchPanels.get(listId);
    }

    public <T extends IsIndexed> AsyncTableCell<T, Void> getList(String listId) {
      return (AsyncTableCell<T, Void>) lists.get(listId);
    }
  }
}
