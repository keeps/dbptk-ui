package com.databasepreservation.common.client.common.search.panel;

import java.util.List;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BooleanSearchFieldPanel extends SearchFieldPanel {
  private final ListBox inputCheckBox;

  public BooleanSearchFieldPanel(SearchField searchField) {
    super(searchField);
    this.inputCheckBox = new ListBox();
  }

  @Override
  public void setup() {
    this.inputCheckBox.addItem(messages.advancedSearchBooleanValueDefault());
    this.inputCheckBox.addItem(messages.advancedSearchBooleanValueTrue(), TRUE);
    this.inputCheckBox.addItem(messages.advancedSearchBooleanValueFalse(), FALSE);
    this.inputCheckBox.addStyleName("form-listbox form-listbox-search");

    inputPanel.add(inputCheckBox);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);

      if (valid(inputCheckBox)) {
        filterParameter = new SimpleFilterParameter(field, inputCheckBox.getSelectedValue());
      }
    }

    return filterParameter;
  }

  @Override
  public void clear() {
    this.inputCheckBox.setSelectedIndex(0);
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof SimpleFilterParameter) {
      SimpleFilterParameter simpleFilterParameter = (SimpleFilterParameter) filterParam;
      final String value = simpleFilterParameter.getValue();
      if (value.equals(TRUE)) {
        inputCheckBox.setSelectedIndex(1);
      } else if (value.equals(FALSE)) {
        inputCheckBox.setSelectedIndex(2);
      } else {
        inputCheckBox.setSelectedIndex(0);
      }
    }
  }
}
