/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.widgets.sponsors;

import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SponsorWidget extends FlowPanel {

  public SponsorWidget() {
    super();
  }

  public SponsorWidget(String url, String linkToContributorPage, String height) {
    super();

    Image image = new Image(url);
    if (ViewerStringUtils.isNotBlank(height)) {
      image.setHeight(height);
    }
    add(image);

    if (ViewerStringUtils.isNotBlank(linkToContributorPage)) {
      image.addClickHandler(e -> {
        Window.open(linkToContributorPage, "_blank", "");
      });
    }
  }
}
