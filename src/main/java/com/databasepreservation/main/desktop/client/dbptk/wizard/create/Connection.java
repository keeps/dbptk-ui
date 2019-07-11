package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
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
  FlowPanel JDBCListConnections, leftSideContainer, connectionInputPanel;

  private ConnectionModule dbmsModule;
  private ConnectionSidebar connectionSidebar;
  private SSHTunnelPanel sshTunnelPanel;
  private String selectedConnection;
  private JDBCPanel selected;
  private Set<JDBCPanel> JDBCPanels = new HashSet<>();

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

    JDBCListConnections.add(spinner);

    BrowserService.Util.getInstance().getDatabaseImportModules(new DefaultAsyncCallback<ConnectionModule>() {
      @Override
      public void onSuccess(ConnectionModule result) {
        connectionSidebar = ConnectionSidebar.getInstance(messages.menuSidebarDatabases(),
          FontAwesomeIconManager.DATABASE, result.getDBMSConnections());

        JDBCListConnections.add(connectionSidebar);
        leftSideContainer.removeStyleName("loading-sidebar");
        JDBCListConnections.remove(spinner);

        dbmsModule = result.getDBMSConnections();
      }
    });
  }

  public void sideBarHighlighter(String connection) {

    connectionSidebar.select(connection);
    JDBCListConnections.clear();
    JDBCListConnections.add(connectionSidebar);

    connectionInputPanel.clear();

    selectedConnection = connection;

    ArrayList<PreservationParameter> preservationParametersSelected = dbmsModule.getParameters(connection);

    TabPanel tabPanel = new TabPanel();
    tabPanel.addStyleName("connection-panel");
    selected = JDBCPanel.getInstance(connection, preservationParametersSelected);
    JDBCPanels.add(selected);
    tabPanel.add(selected, messages.tabGeneral());
    tabPanel.add(sshTunnelPanel, messages.tabSSHTunnel());

    tabPanel.selectTab(0);

    connectionInputPanel.add(tabPanel);
  }

  @Override
  public HashMap<String, String> getValues() {

    if (sshTunnelPanel.isSSHTunnelEnabled()) {
      final SSHConfiguration parameters = sshTunnelPanel.getSSHConfiguration();
      // TODO: SSH connection
    }

    HashMap<String, String> values = selected.getValues();
    values.put("module_name", selectedConnection);
    return values;
  }

  @Override
  public void clear() {
    for (JDBCPanel jdbc : JDBCPanels) {
      jdbc.clear();
    }
    sshTunnelPanel.clear();
    connectionInputPanel.clear();
  }

  public void clearPasswords() {
    for (JDBCPanel jdbc : JDBCPanels) {
      jdbc.clearPasswords();
    }
    sshTunnelPanel.clearPassword();
  }

  @Override
  public boolean validate() {
    final ArrayList<PreservationParameter> validate = selected.validate();

   return validate.isEmpty();
  }

  @Override
  public void error() {

    Toast.showError("Mandatory arguments missing"); //TODO: Improve error message, add electron option to display notification
  }
}