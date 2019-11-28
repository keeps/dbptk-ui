package com.databasepreservation.desktop.client.dbptk.wizard.common.connection;

import com.databasepreservation.common.client.ViewerConstants;
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

  public static CreateConnectionHomePanel getInstance(String type) {
    if (instance == null) {
      instance = new CreateConnectionHomePanel(type);
    }

    return instance;
  }

  private CreateConnectionHomePanel(String type) {
    initWidget(binder.createAndBindUi(this));

    Label header = new Label();
    header.setText(messages.connectionPageTitle());
    header.addStyleName("h2");

    content.add(header);
    if(type.equals(ViewerConstants.EXPORT_FORMAT_SIARD)){
      createSiardHomePanel();
    } else {
      createSendToDBMSHomePanel();
    }
  }

  private void createSiardHomePanel(){
    Label welcome = new Label();
    welcome.setText(messages.connectionPageTextForWelcome());
    welcome.addStyleName("h6");

    Label connection = new Label();
    connection.setText(messages.connectionPageTextForConnectionHelper());
    connection.addStyleName("h6");

    Label tableAndColumns = new Label();
    tableAndColumns.setText(messages.connectionPageTextForTableAndColumnsHelper());
    tableAndColumns.addStyleName("h6");

    Label customViews = new Label();
    customViews.setText(messages.connectionPageTextForCustomViewsHelper());
    customViews.addStyleName("h6");

    Label SIARDExportOptions = new Label();
    SIARDExportOptions.setText(messages.connectionPageTextForExportOptionsHelper());
    SIARDExportOptions.addStyleName("h6");

    Label MetadataExportOptions = new Label();
    MetadataExportOptions.setText(messages.connectionPageTextForMetadataExportOptionsHelper());
    MetadataExportOptions.addStyleName("h6");

    content.add(welcome);
    content.add(connection);
    content.add(tableAndColumns);
    content.add(SIARDExportOptions);
    content.add(MetadataExportOptions);

  }

  private void createSendToDBMSHomePanel(){
    Label welcome = new Label();
    welcome.setText(messages.connectionPageTextForWelcomeDBMSHelper());
    welcome.addStyleName("h6");

    Label connection = new Label();
    connection.setText(messages.connectionPageTextForDBMSHelper());
    connection.addStyleName("h6");

    Label sshConnection = new Label();
    sshConnection.setText(messages.connectionPageTextForSSHelper());
    sshConnection.addStyleName("h6");

    content.add(welcome);
    content.add(connection);
    content.add(sshConnection);
  }
}