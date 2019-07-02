package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.HashMap;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.desktop.client.common.sidebar.ConnectionSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.connection.JDBCPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.create.connection.SSHTunnelPanel;
import com.databasepreservation.main.desktop.shared.models.ConnectionModule;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.databasepreservation.main.desktop.shared.models.SSHConfiguration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Connection extends WizardPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ConnectionUiBinder extends UiBinder<Widget, Connection> {
  }

  private static ConnectionUiBinder binder = GWT.create(ConnectionUiBinder.class);

  @UiField
  FlowPanel JDBCListConnections, SIARDListConnections, leftSideContainer, connectionInputPanel;

  private ConnectionModule siardModule;
  private ConnectionModule dbmsModule;
  private ConnectionSidebar connectionSidebar;
  private SSHTunnelPanel sshTunnelPanel;
  private String selectedConnection;
  private JDBCPanel selected;
  private ArrayList<PreservationParameter> preservationParametersSelected;

  private static Connection instance = null;

  public static Connection getInstance() {
    if (instance == null) {
      instance = new Connection();
    }
    return instance;
  }

  private Connection() {
    initWidget(binder.createAndBindUi(this));

    sshTunnelPanel = SSHTunnelPanel.getInstance();

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    SIARDListConnections.add(spinner);

    BrowserService.Util.getInstance().getDatabaseModuleFactories(new DefaultAsyncCallback<ConnectionModule>() {
      @Override
      public void onSuccess(ConnectionModule result) {
        connectionSidebar = ConnectionSidebar.getInstance(messages.menuSidebarDatabases(),
          FontAwesomeIconManager.DATABASE, result.getDBMSConnections());

        JDBCListConnections.add(connectionSidebar);
        leftSideContainer.removeStyleName("loading-sidebar");
        SIARDListConnections.remove(spinner);

        siardModule = result.getSIARDConnections();
        dbmsModule = result.getDBMSConnections();
      }
    });
  }

  public void change(String connection) {

    connectionSidebar.select(connection);
    JDBCListConnections.clear();
    JDBCListConnections.add(connectionSidebar);

    connectionInputPanel.clear();

    selectedConnection = connection;

    preservationParametersSelected = dbmsModule.getParameters(connection);

    TabPanel tabPanel = new TabPanel();
    tabPanel.addStyleName("connection-panel");
    selected = JDBCPanel.getInstance(connection, preservationParametersSelected);
    tabPanel.add(selected, messages.tabGeneral());
    tabPanel.add(sshTunnelPanel, messages.tabSSHTunnel());

    tabPanel.selectTab(0);

    connectionInputPanel.add(tabPanel);
  }

  @Override
  public void getValues() {
    if (sshTunnelPanel.isSSHTunnelEnabled()) {
      final SSHConfiguration parameters = sshTunnelPanel.getSSHConfiguration();
      JavascriptUtils.log(parameters.getHostname());
      JavascriptUtils.log(parameters.getPort());
      JavascriptUtils.log(parameters.getUsername());
      JavascriptUtils.log(parameters.getPassword());
    }

    final HashMap<String, String> values = selected.getValues();

  }

  @Override
  public void clear() {
    connectionInputPanel.clear();
  }

  @Override
  public void validate() {

  }
}