package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableAndColumns extends WizardPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface TableAndColumnsUiBinder extends UiBinder<Widget, TableAndColumns> {
  }

  private static TableAndColumnsUiBinder binder = GWT.create(TableAndColumnsUiBinder.class);

  @UiField
  FlowPanel content;

  private static TableAndColumns instance = null;

  public static TableAndColumns getInstance() {
    if (instance == null) {
      instance = new TableAndColumns();
    }
    return instance;
  }

  private TableAndColumns() {
    initWidget(binder.createAndBindUi(this));

    content.add(new HTML("TABLE AND COLUMNS"));
  }

  @Override
  public void clear() {

  }

  @Override
  public void validate() {

  }

  @Override
  public void getValues() {

  }
}