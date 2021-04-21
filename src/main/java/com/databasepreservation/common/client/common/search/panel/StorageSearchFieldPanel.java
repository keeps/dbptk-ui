package com.databasepreservation.common.client.common.search.panel;

import java.util.List;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.LongRangeFilterParameter;
import com.databasepreservation.common.client.tools.Humanize;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class StorageSearchFieldPanel extends SearchFieldPanel {
  private static final String NUMBER = "number";

  private final TextBox inputStorageSizeFrom;
  private final TextBox inputStorageSizeTo;
  private final ListBox inputStorageSizeList;

  public StorageSearchFieldPanel(SearchField searchField) {
    super(searchField);
    inputStorageSizeFrom = new TextBox();
    inputStorageSizeTo = new TextBox();
    inputStorageSizeList = new ListBox();
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter longRangeFilterParameter = (LongRangeFilterParameter) filterParam;

      Long begin = longRangeFilterParameter.getFromValue();
      Long end = longRangeFilterParameter.getToValue();

      if (begin != null) {
        inputStorageSizeFrom.setValue(String.valueOf(begin));
      }

      if (end != null) {
        inputStorageSizeTo.setValue(String.valueOf(end));
      }
    }
  }

  @Override
  public void setup() {
    inputStorageSizeFrom.getElement().setAttribute("type", NUMBER);
    inputStorageSizeTo.getElement().setAttribute("type", NUMBER);
    for (String unit : Humanize.UNITS) {
      inputStorageSizeList.addItem(unit, unit);
    }

    inputPanel.add(inputStorageSizeFrom);
    inputPanel.add(inputStorageSizeTo);
    inputPanel.add(inputStorageSizeList);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);
      if (intervalValid(inputStorageSizeFrom, inputStorageSizeTo)) {
        filterParameter = new LongRangeFilterParameter(field,
          Humanize.parseFileSize(inputStorageSizeFrom.getValue(), inputStorageSizeList.getSelectedValue()),
          Humanize.parseFileSize(inputStorageSizeTo.getValue(), inputStorageSizeList.getSelectedValue()));
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    inputStorageSizeFrom.setText("");
    inputStorageSizeTo.setText("");
  }
}
