package com.databasepreservation.common.client.common.search.panel;

import java.util.List;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NumericSearchFieldPanel extends SearchFieldPanel {
  private static final String NUMBER = "number";

  private final TextBox inputNumeric;

  public NumericSearchFieldPanel(SearchField searchField) {
    super(searchField);
    this.inputNumeric = new TextBox();
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter basicSearchFilterParameter = (BasicSearchFilterParameter) filterParam;
      inputNumeric.setValue(basicSearchFilterParameter.getValue());
    }
  }

  @Override
  public void setup() {
    this.inputNumeric.getElement().setAttribute("type", NUMBER);

    inputPanel.add(inputNumeric);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);
      if (valid(inputNumeric)) {
        filterParameter = new BasicSearchFilterParameter(field, inputNumeric.getValue());
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    inputNumeric.setText("");
  }
}
