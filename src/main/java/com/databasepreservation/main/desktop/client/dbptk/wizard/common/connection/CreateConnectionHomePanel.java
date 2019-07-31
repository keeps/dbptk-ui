package com.databasepreservation.main.desktop.client.dbptk.wizard.common.connection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateConnectionHomePanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface CreateConnectionHomePanelUiBinder extends UiBinder<Widget, CreateConnectionHomePanel> {
  }

  private static CreateConnectionHomePanelUiBinder binder = GWT.create(CreateConnectionHomePanelUiBinder.class);

  @UiField
  FlowPanel content;

  private static CreateConnectionHomePanel instance = null;

  public static CreateConnectionHomePanel getInstance() {
    if (instance == null) {
      instance = new CreateConnectionHomePanel();
    }

    return instance;
  }

  private CreateConnectionHomePanel() {
    initWidget(binder.createAndBindUi(this));

    Label header = new Label();
    header.setText(messages.connectionHomePanelTitle());
    header.addStyleName("h2");

    Label welcome = new Label();
    welcome.setText(messages.connectionHomePanelWelcomeText());
    welcome.addStyleName("h6");

    Label connection = new Label();
    connection.setText(messages.connectionHomePanelConnectionText());
    connection.addStyleName("h6");

    Label tableAndColumns = new Label();
    tableAndColumns.setText(messages.connectionHomePanelTableAndColumnsText());
    tableAndColumns.addStyleName("h6");

    Label customViews = new Label();
    customViews.setText(messages.connectionHomePanelCustomViewsText());
    customViews.addStyleName("h6");

    Label SIARDExportOptions = new Label();
    SIARDExportOptions.setText(messages.connectionHomePanelExportOptionsText());
    SIARDExportOptions.addStyleName("h6");

    Label MetadataExportOptions = new Label();
    MetadataExportOptions.setText(messages.connectionHomePanelMetadataExportOptionsText());
    MetadataExportOptions.addStyleName("h6");

    content.add(header);
    content.add(welcome);
    content.add(connection);
    content.add(tableAndColumns);
    content.add(SIARDExportOptions);
    content.add(MetadataExportOptions);
  }
}