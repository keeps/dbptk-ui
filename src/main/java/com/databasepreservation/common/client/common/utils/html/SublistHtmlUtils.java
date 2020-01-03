package com.databasepreservation.common.client.common.utils.html;

import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.common.fields.RowField;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SublistHtmlUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static RowField getSublistHTML(Sublist sublist, long totalCount) {
    SafeHtmlBuilder sublistHTML = new SafeHtmlBuilder();

    int firstElementIndex = sublist.getFirstElementIndex();
    int maximumElementCount = sublist.getMaximumElementCount();

    int page = firstElementIndex + maximumElementCount;

    if (totalCount == 0) {
      return RowField.createInstance("Sublist",
        new HTML(sublistHTML.append(SafeHtmlUtils.fromSafeConstant(messages.sublistNoElements())).toSafeHtml()));
      // return
      // sublistHTML.append(SafeHtmlUtils.fromSafeConstant(messages.sublistNoElements())).toSafeHtml();
    }

    if (totalCount == 1) {
      return RowField.createInstance("Sublist",
        new HTML(sublistHTML.append(SafeHtmlUtils.fromSafeConstant(messages.sublistSingleElement())).toSafeHtml()));
      // return
      // sublistHTML.append(SafeHtmlUtils.fromSafeConstant(messages.sublistSingleElement())).toSafeHtml();
    }

    if (totalCount < page) {
      return RowField.createInstance("Sublist", new HTML(sublistHTML
        .append(SafeHtmlUtils.fromSafeConstant(messages.sublist(firstElementIndex + 1, totalCount))).toSafeHtml()));
      // return
      // sublistHTML.append(SafeHtmlUtils.fromSafeConstant(messages.sublist(firstElementIndex+1,
      // totalCount))).toSafeHtml();
    }

    return RowField.createInstance("Sublist", new HTML(
      sublistHTML.append(SafeHtmlUtils.fromSafeConstant(messages.sublist(firstElementIndex + 1, page))).toSafeHtml()));
    // sublistHTML.append(SafeHtmlUtils.fromSafeConstant(messages.sublist(firstElementIndex+1,
    // page)));
    // return sublistHTML.toSafeHtml();
  }
}
