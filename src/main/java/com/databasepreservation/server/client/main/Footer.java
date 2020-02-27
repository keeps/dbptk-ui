package com.databasepreservation.server.client.main;

import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Footer extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface FooterUiBinder extends UiBinder<Widget, Footer> {
  }

  private static FooterUiBinder binder = GWT.create(FooterUiBinder.class);

  @UiField
  Hyperlink contributors;

  public Footer() {
    initWidget(binder.createAndBindUi(this));

    contributors.setText(messages.breadcrumbTextForSponsors());
    contributors.setTargetHistoryToken(HistoryManager.ROUTE_SPONSORS);
  }
}
