package com.databasepreservation.main.desktop.client.dbptk.wizard.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.sidebar.ConnectionSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.connection.JDBCPanel;
import com.databasepreservation.main.desktop.client.dbptk.wizard.common.connection.SSHTunnelPanel;
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
public class DBMSConnection extends WizardPanel<ConnectionParameters> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface DBMSConnectionUiBinder extends UiBinder<Widget, DBMSConnection> {
  }

  private static DBMSConnectionUiBinder binder = GWT.create(DBMSConnectionUiBinder.class);

  @UiField
  FlowPanel JDBCListConnections, leftSideContainer, connectionInputPanel;

  private DBPTKModule dbmsModule;
  private ConnectionSidebar connectionSidebar;
  private SSHTunnelPanel sshTunnelPanel;
  private String selectedConnection;
  private JDBCPanel selected;
  private Set<JDBCPanel> JDBCPanels = new HashSet<>();
  private String databaseUUID;
  private static Map<String, DBMSConnection> instances = new HashMap<>();

  public static DBMSConnection getInstance(final String databaseUUID) {
    if (instances.get(databaseUUID) == null) {
      DBMSConnection instance = new DBMSConnection(databaseUUID);
      instances.put(databaseUUID, instance);
    }

    DBMSWizardManager wizardManager = DBMSWizardManager.getInstance(databaseUUID);
    wizardManager.enableNext(false);

    return instances.get(databaseUUID);
  }

  private DBMSConnection(final String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;
    sshTunnelPanel = SSHTunnelPanel.getInstance(databaseUUID);

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    JDBCListConnections.add(spinner);

    BrowserService.Util.getInstance().getDatabaseExportModules(new DefaultAsyncCallback<DBPTKModule>() {
      @Override
      public void onSuccess(DBPTKModule result) {
        connectionSidebar = ConnectionSidebar.getInstance(databaseUUID, messages.menuSidebarDatabases(),
          FontAwesomeIconManager.DATABASE, result.getDBMSConnections(), HistoryManager.ROUTE_SEND_TO_LIVE_DBMS);

        JDBCListConnections.add(connectionSidebar);
        leftSideContainer.removeStyleName("loading-sidebar");
        JDBCListConnections.remove(spinner);

        dbmsModule = result.getDBMSConnections();
      }
    });
  }

  public void sideBarHighlighter(String connection) {
    GWT.log(connection);
    connectionSidebar.select(connection);
    JDBCListConnections.clear();
    JDBCListConnections.add(connectionSidebar);

    connectionInputPanel.clear();

    selectedConnection = connection;

    ArrayList<PreservationParameter> preservationParametersSelected = dbmsModule.getParameters(connection);

    TabPanel tabPanel = new TabPanel();
    tabPanel.addStyleName("browseItemMetadata connection-panel");
    selected = JDBCPanel.getInstance(connection, preservationParametersSelected, databaseUUID);
    JDBCPanels.add(selected);
    tabPanel.add(selected, messages.tabGeneral());
    tabPanel.add(sshTunnelPanel, messages.tabSSHTunnel());

    tabPanel.selectTab(0);

    DBMSWizardManager wizardManager = DBMSWizardManager.getInstance(databaseUUID);
    wizardManager.enableNext(false);

    selected.validate();
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

  @Override
  public boolean validate() {
    if (selected != null) {
      GWT.log("" + sshTunnelPanel.isSSHTunnelEnabled());
      if (sshTunnelPanel.isSSHTunnelEnabled()) {
        return selected.validate() && sshTunnelPanel.validate();
      }
      return selected.validate();
    }

    return false;
  }

  @Override
  public void error() {
    Toast.showError(messages.errorMessagesConnectionTitle(), messages.errorMessagesConnection(1));
  }
}