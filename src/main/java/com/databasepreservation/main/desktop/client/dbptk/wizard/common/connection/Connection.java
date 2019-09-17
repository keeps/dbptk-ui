package com.databasepreservation.main.desktop.client.dbptk.wizard.common.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.sidebar.ConnectionSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
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
public class Connection extends WizardPanel<ConnectionParameters> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ConnectionUiBinder extends UiBinder<Widget, Connection> {
  }

  private static ConnectionUiBinder binder = GWT.create(ConnectionUiBinder.class);

  @UiField
  FlowPanel JDBCListConnections, leftSideContainer, connectionInputPanel;

  private final String databaseUUID;
  private DBPTKModule dbmsModule;
  private ConnectionSidebar connectionSidebar;
  private SSHTunnelPanel sshTunnelPanel;
  private String selectedConnection;
  private JDBCPanel selected;
  private Set<JDBCPanel> JDBCPanels = new HashSet<>();
  private String type;
  private boolean clickedOnSidebar = false;

  private static HashMap<String, Connection> instances = new HashMap<>();

  public static Connection getInstance(final String databaseUUID) {
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new Connection(databaseUUID));
    }
    return instances.get(databaseUUID);
  }

  private Connection(final String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;

    sshTunnelPanel = SSHTunnelPanel.getInstance(databaseUUID);
  }

  public void initImportDBMS(final String type, final String targetToken) {
    this.type = type;
    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    JDBCListConnections.add(spinner);

    CreateConnectionHomePanel connectionHomePanel = CreateConnectionHomePanel
      .getInstance(ViewerConstants.EXPORT_FORMAT_SIARD);
    connectionInputPanel.clear();
    connectionInputPanel.add(connectionHomePanel);

    BrowserService.Util.getInstance().getDatabaseImportModules(new DefaultAsyncCallback<DBPTKModule>() {
      @Override
      public void onSuccess(DBPTKModule result) {
        connectionSidebar = ConnectionSidebar.getInstance(databaseUUID, messages.sidebarMenuTextForDatabases(),
          FontAwesomeIconManager.DATABASE, result.getDBMSConnections(), targetToken);

        JDBCListConnections.add(connectionSidebar);
        leftSideContainer.removeStyleName("loading-sidebar");
        JDBCListConnections.remove(spinner);

        dbmsModule = result.getDBMSConnections();
      }
    });
  }

  public void initExportDBMS(final String type, final String targetToken) {
    this.type = type;
    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    JDBCListConnections.add(spinner);

    CreateConnectionHomePanel connectionHomePanel = CreateConnectionHomePanel
      .getInstance(ViewerConstants.EXPORT_FORMAT_DBMS);
    connectionInputPanel.clear();
    connectionInputPanel.add(connectionHomePanel);

    BrowserService.Util.getInstance().getDatabaseExportModules(new DefaultAsyncCallback<DBPTKModule>() {
      @Override
      public void onSuccess(DBPTKModule result) {
        connectionSidebar = ConnectionSidebar.getInstance(databaseUUID, messages.sidebarMenuTextForDatabases(),
          FontAwesomeIconManager.DATABASE, result.getDBMSConnections(), targetToken);

        JDBCListConnections.add(connectionSidebar);
        leftSideContainer.removeStyleName("loading-sidebar");
        JDBCListConnections.remove(spinner);

        dbmsModule = result.getDBMSConnections();
      }
    });
  }

  public void sideBarHighlighter(String connection) {
    this.clickedOnSidebar = true;
    connectionSidebar.select(connection);
    JDBCListConnections.clear();
    JDBCListConnections.add(connectionSidebar);

    connectionInputPanel.clear();

    selectedConnection = connection;

    ArrayList<PreservationParameter> preservationParametersSelected = dbmsModule.getParameters(connection);

    TabPanel tabPanel = new TabPanel();
    tabPanel.addStyleName("browseItemMetadata connection-panel");
    selected = JDBCPanel.getInstance(connection, preservationParametersSelected, databaseUUID, type);
    JDBCPanels.add(selected);
    tabPanel.add(selected, messages.connectionPageTextForTabGeneral());
    tabPanel.add(sshTunnelPanel, messages.connectionPageTextForTabSSHTunnel());

    tabPanel.selectTab(0);

    selected.validate(type);
    connectionInputPanel.add(tabPanel);
  }

  @Override
  public ConnectionParameters getValues() {
    ConnectionParameters parameters = new ConnectionParameters();

    parameters.setJDBCConnectionParameters(selected.getValues());
    parameters.setModuleName(selectedConnection);

    if (sshTunnelPanel.isSSHTunnelEnabled()) {
      parameters.setSSHConfiguration(sshTunnelPanel.getSSHConfiguration());
      parameters.doSSH(true);
    }

    return parameters;
  }

  @Override
  public void clear() {
    for (JDBCPanel jdbc : JDBCPanels) {
      jdbc.clear();
    }
    sshTunnelPanel.clear();
    connectionInputPanel.clear();
    connectionSidebar.selectNone();
    instances.clear();
  }

  public void clearPasswords() {
    for (JDBCPanel jdbc : JDBCPanels) {
      jdbc.clearPasswords();
    }
    sshTunnelPanel.clearPassword();
  }

  public boolean isClickedOnSidebar() {
    return clickedOnSidebar;
  }

  @Override
  public boolean validate() {
    if (selected != null) {
      if (sshTunnelPanel.isSSHTunnelEnabled()) {
        return selected.validate(type) && sshTunnelPanel.validate();
      }
      return selected.validate(type);
    }

    return false;
  }

  @Override
  public void error() {
    Toast.showError(messages.errorMessagesConnectionTitle(), messages.connectionPageErrorMessageFor(1));
  }
}