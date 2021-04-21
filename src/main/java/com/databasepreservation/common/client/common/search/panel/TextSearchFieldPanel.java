package com.databasepreservation.common.client.common.search.panel;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.google.gwt.user.client.ui.TextBox;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TextSearchFieldPanel extends SearchFieldPanel {
  // Text
  private final TextBox inputText;

  public TextSearchFieldPanel(SearchField searchField) {
    super(searchField);
    inputText = new TextBox();
    inputText.addStyleName("form-textbox");
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter basicSearchFilterParameter = (BasicSearchFilterParameter) filterParam;
      inputText.setValue(basicSearchFilterParameter.getValue());
    }
  }

  @Override
  public void setup() {
    inputPanel.add(inputText);
    inputPanel.addStyleName(FULL_WIDTH);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);
      if (valid(inputText)) {
        filterParameter = new BasicSearchFilterParameter(field, inputText.getValue());
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    inputText.setText("");
  }
}
