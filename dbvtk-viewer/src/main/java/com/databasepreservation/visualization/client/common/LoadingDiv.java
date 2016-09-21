package com.databasepreservation.visualization.client.common;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class LoadingDiv extends HTMLPanel {
  public LoadingDiv() {
    super(
      SafeHtmlUtils
        .fromSafeConstant("<div class=\"spinner\" id=\"loading\"><div class=\"double-bounce1\"></div><div class=\"double-bounce2\"></div></div>"));
  }

  /**
   * do not use. needed by GWT
   */
  public LoadingDiv(String html) {
    this();
  }

  /**
   * do not use. needed by GWT
   */
  public LoadingDiv(SafeHtml safeHtml) {
    this();
  }

  /**
   * do not use. needed by GWT
   */
  public LoadingDiv(String tag, String html) {
    this();
  }
}
