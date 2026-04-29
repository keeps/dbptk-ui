/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search.panel;

import java.util.List;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class BaseEnumSearchFieldPanel<E extends Enum<E>> extends SearchFieldPanel {
  protected final ListBox inputListBox;
  private final Class<E> enumClass;

  public BaseEnumSearchFieldPanel(SearchField searchField, Class<E> enumClass) {
    super(searchField);
    this.inputListBox = new ListBox();
    this.enumClass = enumClass;
  }

  @Override
  public void setup() {
    this.inputListBox.addItem(messages.advancedSearchBooleanValueDefault(), "");

    if (enumClass != null) {
      for (E enumValue : enumClass.getEnumConstants()) {
        String displayLabel = getDisplayLabel(enumValue);
        this.inputListBox.addItem(displayLabel, enumValue.name());
      }
    }

    this.inputListBox.addStyleName("form-textbox");
    inputPanel.add(inputListBox);
  }

  protected abstract String getDisplayLabel(E value);

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && !searchFields.isEmpty()) {
      String field = searchFields.get(0);
      String selectedValue = inputListBox.getSelectedValue();

      if (selectedValue != null && !selectedValue.isEmpty()) {
        filterParameter = new SimpleFilterParameter(field, selectedValue);
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    this.inputListBox.setSelectedIndex(0);
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof SimpleFilterParameter) {
      SimpleFilterParameter simpleFilterParameter = (SimpleFilterParameter) filterParam;
      String valueToSelect = simpleFilterParameter.getValue();

      for (int i = 0; i < inputListBox.getItemCount(); i++) {
        if (inputListBox.getValue(i).equals(valueToSelect)) {
          inputListBox.setSelectedIndex(i);
          break;
        }
      }
    }
  }
}
