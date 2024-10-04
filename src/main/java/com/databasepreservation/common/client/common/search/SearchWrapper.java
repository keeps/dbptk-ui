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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

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

  private final Components components;

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

  public <T extends IsIndexed> SearchWrapper createListAndSearchPanel(ListBuilder<T> listBuilder) {
    AsyncTableCell<T, Void> list = listBuilder.build();

    SearchPanel searchPanel;

    Filter filter = list.getFilter();
    String allFilter = SearchFilters.searchField();
    boolean incremental = SearchFilters.shouldBeIncremental(filter);

    // get configuration

    boolean searchEnabled = true;

    String defaultLabelText = ClientConfigurationManager.resolveTranslation(ViewerConstants.UI_LISTS_PROPERTY,
      listBuilder.getOptions().getListId(), ViewerConstants.UI_LISTS_SEARCH_SELECTEDINFO_LABEL_DEFAULT_I18N_PROPERTY);

    if (defaultLabelText == null) {
      defaultLabelText = messages.someOfAObject(listBuilder.getOptions().getClassToReturn().getName());
    }

    String dropdownValue = listBuilder.getOptions().getListId();

    // create
    searchPanel = new SearchPanel(filter, allFilter, messages.searchPlaceholder(), true, false);
    searchPanel.setList(list);
    searchPanel.setDefaultFilterIncremental(incremental);
    searchPanel.setSearchPanelSelectionDropdownWrapperVisible(true);
    if (hasMultipleSearchPanels) {
      initSearchPanelSelectionDropdown();
      searchPanelSelectionDropdown.addItem(defaultLabelText, dropdownValue);
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

  private <T extends IsIndexed> void attachComponents(String objectClassSimpleName) {
    SearchPanel searchPanel = components.getSearchPanel(objectClassSimpleName);
    AsyncTableCell<T, Void> list = components.getList(objectClassSimpleName);

    rootPanel.clear();
    rootPanel.add(searchPanel);
    if (scrollPanelCssClasses != null) {
      ScrollPanel scrollPanel = new ScrollPanel(list);
      scrollPanel.addStyleName(scrollPanelCssClasses);
      rootPanel.add(scrollPanel);
    } else {
      rootPanel.add(list);
    }

    if (hasMultipleSearchPanels) {
      searchPanelSelectionDropdown.setSelectedValue(objectClassSimpleName, false);
      searchPanel.attachSearchPanelSelectionDropdown(searchPanelSelectionDropdown);
    }
  }

  public Components getComponents() {
    return components;
  }

  /**
   * Auxiliary manager for inner components (groups of one searchWrapper and one
   * BasicAsyncTableCell, at least for now) that is used to enforce type coherence
   */
  @SuppressWarnings("unchecked")
  public class Components {
    private final Map<String, SearchPanel> searchPanels = new LinkedHashMap<>();
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
    public <T extends IsIndexed> void put(String listId, SearchPanel searchPanel, AsyncTableCell<T, Void> list) {
      searchPanels.put(listId, searchPanel);
      lists.put(listId, list);
    }

    public SearchPanel getSearchPanel(String listId) {
      return searchPanels.get(listId);
    }

    <T extends IsIndexed> AsyncTableCell<T, Void> getList(String listId) {
      return (AsyncTableCell<T, Void>) lists.get(listId);
    }
  }
}
