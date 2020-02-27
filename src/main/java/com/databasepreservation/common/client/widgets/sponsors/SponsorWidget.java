package com.databasepreservation.common.client.widgets.sponsors;

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

  public SponsorWidget(String url, String linkToContributorPage) {
    super();

    Image image = new Image(url);
    image.setHeight("150px");
    add(image);

    image.addClickHandler(e -> {
      Window.open(linkToContributorPage, "_blank", "");
    });
  }
}
