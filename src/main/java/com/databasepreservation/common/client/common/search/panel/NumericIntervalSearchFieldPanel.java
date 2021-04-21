package com.databasepreservation.common.client.common.search.panel;

import java.util.List;

import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.LongRangeFilterParameter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NumericIntervalSearchFieldPanel extends SearchFieldPanel {
  private static final String NUMBER = "number";

  private final TextBox inputNumericFrom;
  private final TextBox inputNumericTo;
  private final Label labelTo;

  public NumericIntervalSearchFieldPanel(SearchField searchField) {
    super(searchField);
    this.inputNumericFrom = new TextBox();
    this.inputNumericTo = new TextBox();
    this.labelTo = new Label(messages.searchingRange_to());
  }

  @Override
  public void setup() {
    this.inputNumericFrom.getElement().setAttribute("type", NUMBER);
    this.inputNumericTo.getElement().setAttribute("type", NUMBER);

    inputNumericFrom.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    inputNumericTo.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    labelTo.addStyleName(LABEL);

    inputPanel.add(inputNumericFrom);
    inputPanel.add(labelTo);
    inputPanel.add(inputNumericTo);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);

      if (!inputNumericFrom.getValue().isEmpty() || !inputNumericTo.getValue().isEmpty()) {
        if (intervalValid(inputNumericFrom, inputNumericTo)) {
          if (inputNumericTo.getValue().isEmpty()) {
            filterParameter = new LongRangeFilterParameter(field, Long.valueOf(inputNumericFrom.getValue()), null);
          } else if (inputNumericFrom.getValue().isEmpty()) {
            filterParameter = new LongRangeFilterParameter(field, null, Long.valueOf(inputNumericTo.getValue()));
          } else {
            filterParameter = new LongRangeFilterParameter(field, Long.valueOf(inputNumericFrom.getValue()),
              Long.valueOf(inputNumericTo.getValue()));
          }
        } else {
          Dialogs.showErrors(messages.advancedSearchDialogTitle(),
            messages.advancedSearchErrorMessageForNumericInvalid(), messages.basicActionClose());
        }
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    inputNumericFrom.setText("");
    inputNumericTo.setText("");
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter longRangeFilterParameter = (LongRangeFilterParameter) filterParam;

      Long begin = longRangeFilterParameter.getFromValue();
      Long end = longRangeFilterParameter.getToValue();

      if (begin != null) {
        inputNumericFrom.setValue(String.valueOf(begin));
      }

      if (end != null) {
        inputNumericTo.setValue(String.valueOf(end));
      }
    }
  }
}
