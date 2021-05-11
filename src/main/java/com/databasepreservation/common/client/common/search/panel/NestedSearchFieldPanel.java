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
import com.databasepreservation.common.client.index.filter.BlockJoinParentFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NestedSearchFieldPanel extends SearchFieldPanel {
  // Text
  private final TextBox inputText;

  public NestedSearchFieldPanel(SearchField searchField) {
    super(searchField);
    inputText = new TextBox();
    inputText.addStyleName("form-textbox");
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
      if (!inputText.getValue().isEmpty()) {
        filterParameter = new BlockJoinParentFilterParameter(field, inputText.getValue(), searchFields.get(1),
          searchFields.get(2));
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    inputText.setText("");
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof BlockJoinParentFilterParameter) {
      BlockJoinParentFilterParameter blockJoinParentFilterParameter = (BlockJoinParentFilterParameter) filterParam;
      inputText.setValue(blockJoinParentFilterParameter.getValue());
    }
  }
}
