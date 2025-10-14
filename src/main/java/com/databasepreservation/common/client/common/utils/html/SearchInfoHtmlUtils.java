/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.utils.html;

import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SearchInfoHtmlUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static SafeHtml getSearchInfoHtml(SearchInfo searchInfo) {

    final List<SearchField> fields = searchInfo.getFields();
    final Map<String, FilterParameter> fieldParameters = searchInfo.getMapFieldParameters();

    for (int i = 0; i < fields.size(); i++) {
      SearchField field = fields.get(i);
      final String label = field.getLabel();
      if (fieldParameters.get(field.getId()) != null && fieldParameters.get(field.getId()) instanceof BasicSearchFilterParameter) {
        BasicSearchFilterParameter parameter = (BasicSearchFilterParameter) fieldParameters.get(field.getId());
        final String value = parameter.getValue();
        return messages.basicFilterParameter(messages.activityLogFilterName(label), value);
      }
    }

    return SafeHtmlUtils.EMPTY_SAFE_HTML;
  }

}
