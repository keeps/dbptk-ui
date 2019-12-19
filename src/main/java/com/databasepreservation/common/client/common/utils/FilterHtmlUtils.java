package com.databasepreservation.common.client.common.utils;

import java.util.List;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FilterHtmlUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static SafeHtml getFilterHTML(Filter filter, String classToFilter) {
    List<FilterParameter> parameterValues = filter.getParameters();
    SafeHtmlBuilder preFilterTranslations = new SafeHtmlBuilder();
    preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant(classToFilter))
      .append(SafeHtmlUtils.fromSafeConstant("&nbsp;where")).append(SafeHtmlUtils.fromSafeConstant(":<ul><li>"));

    for (int i = 0; i < parameterValues.size(); i++) {
      final SafeHtml filterParameterHTML = getFilterParameterHTML(parameterValues.get(i));
      if (filterParameterHTML != null) {
        preFilterTranslations.append(filterParameterHTML);
      }

      preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("</li>"));
      if (i != parameterValues.size() - 1) {
        preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("<li>"));
      }
    }

    preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("</ul>"));
    return preFilterTranslations.toSafeHtml();
  }

  public static SafeHtml getFilterParameterHTML(FilterParameter parameter) {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter p = (SimpleFilterParameter) parameter;
      return SafeHtmlUtils.fromSafeConstant("<span class=\"search-prefilter-field\">" + p.getName()
        + "</span> is <span class=\"search-prefilter-value\">" + p.getValue() + "</span>");
      // return
      // messages.searchPreFilterSimpleFilterParameter(messages.searchPreFilterName(p.getName()),
      // p.getValue());
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter p = (BasicSearchFilterParameter) parameter;
      // TODO put '*' in some constant, see Search
      if (!"*".equals(p.getValue())) {
        return SafeHtmlUtils.fromSafeConstant("<span class=\"search-prefilter-field\">" + p.getName()
          + "</span> is <span class=\"search-prefilter-value\">" + p.getValue() + "</span>");
      }
    } else if (parameter instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter p = (LongRangeFilterParameter) parameter;
      if (p.getFromValue() == null) {
        return SafeHtmlUtils.fromSafeConstant("<span class=\"search-prefilter-field\">" + p.getName()
          + "</span> until <span class=\"search-prefilter-value\">" + p.getToValue());
      } else if (p.getToValue() == null) {
        return SafeHtmlUtils.fromSafeConstant("<span class=\"search-prefilter-field\">" + p.getName()
          + "</span> from <span class=\"search-prefilter-value\">" + p.getFromValue());
      } else {
        if (p.getToValue().equals(p.getFromValue())) {
          return SafeHtmlUtils.fromSafeConstant("<span class=\"search-prefilter-field\">" + p.getName()
            + "</span> is <span class=\"search-prefilter-value\">" + p.getFromValue());
        }

        return SafeHtmlUtils.fromSafeConstant("<span class=\"search-prefilter-field\">" + p.getName()
          + "</span> is between <span class=\"search-prefilter-value\">" + p.getFromValue()
          + "</span> and <span class=\"search-prefilter-value\">" + p.getToValue() + "</span>");
      }
    } else {
      return SafeHtmlUtils.fromString(parameter.getClass().getSimpleName());
    }

    return null;
  }
}
