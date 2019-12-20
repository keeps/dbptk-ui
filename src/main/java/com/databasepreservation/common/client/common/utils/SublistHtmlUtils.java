package com.databasepreservation.common.client.common.utils;

import org.roda.core.data.v2.index.sublist.Sublist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SublistHtmlUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static SafeHtml getSublistHTML(Sublist sublist, long totalCount) {
    SafeHtmlBuilder sublistHTML = new SafeHtmlBuilder();

    int firstElementIndex = sublist.getFirstElementIndex();
    int maximumElementCount = sublist.getMaximumElementCount();

    int page = firstElementIndex + maximumElementCount;

    if (totalCount < page) {
      return sublistHTML.append(
          SafeHtmlUtils.fromSafeConstant(messages.sublist(firstElementIndex+1, totalCount))).toSafeHtml();
    }

    sublistHTML.append(
      SafeHtmlUtils.fromSafeConstant(messages.sublist(firstElementIndex+1, page)));

    return sublistHTML.toSafeHtml();
  }
}
