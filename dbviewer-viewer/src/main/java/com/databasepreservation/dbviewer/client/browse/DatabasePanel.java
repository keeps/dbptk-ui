package com.databasepreservation.dbviewer.client.browse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabasePanel extends Composite {
  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabasePanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private String databaseID;

  @UiField
  public Label idElem;

  public DatabasePanel(String databaseID) {
    this.databaseID = databaseID;
    initWidget(uiBinder.createAndBindUi(this));

    idElem.setText(databaseID);
  }
}
