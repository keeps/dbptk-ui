package com.databasepreservation.server.client.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Footer extends Composite {
  interface FooterUiBinder extends UiBinder<Widget, Footer> {
  }

  private static FooterUiBinder binder = GWT.create(FooterUiBinder.class);

  public Footer() {
    initWidget(binder.createAndBindUi(this));
  }
}
