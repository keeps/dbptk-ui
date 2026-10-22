/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.fields;

import com.databasepreservation.common.client.ViewerConstants;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A sentence ending with a highlighted link that opens in a new tab.
 */
public class TextWithLinkField extends Composite {

  public static TextWithLinkField createInstance(String text, String linkText, String href) {
    return new TextWithLinkField(text, linkText, href);
  }

  private TextWithLinkField(String text, String linkText, String href) {
    FlowPanel panel = new FlowPanel();
    panel.add(new InlineLabel(text + " "));

    Anchor anchor = new Anchor(linkText, href, ViewerConstants.BLANK_LINK);
    anchor.addStyleName("link-info");
    panel.add(anchor);

    initWidget(panel);
  }
}
