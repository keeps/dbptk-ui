package com.databasepreservation.desktop.client.dbptk.wizard.common.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.parameters.PreservationParameter;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionResponse;
import com.databasepreservation.common.client.services.MigrationService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.widgets.Toast;
import com.databasepreservation.desktop.client.common.sidebar.ConnectionSidebar;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
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
  private static final String SPINNER_DIV = "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>";

  interface ConnectionUiBinder extends UiBinder<Widget, Connection> {
  }

  private static ConnectionUiBinder binder = GWT.create(ConnectionUiBinder.class);

  @UiField
  FlowPanel mainPanel;

  @UiField
  FlowPanel jdbcListConnections;

  @UiField
  FlowPanel leftSideContainer;

  @UiField
  FlowPanel connectionInputPanel;

  private final String databaseUUID;
  private List<Module> dbmsModules;
  private ConnectionSidebar connectionSidebar;
  private SSHTunnelPanel sshTunnelPanel;
  private String selectedConnection;
  private JDBCPanel selected;
  private Set<JDBCPanel> jdbcPanels = new HashSet<>();
  private String type;
  private boolean clickedOnSidebar = false;
  private boolean countRows = false;
  private Button btnTestConnection;

  private static Map<String, Connection> instances = new HashMap<>();

  public static Connection getInstance(final String databaseUUID) {
    instances.computeIfAbsent(databaseUUID, k -> new Connection(databaseUUID));
    return instances.get(databaseUUID);
  }

  private Connection(final String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    this.databaseUUID = databaseUUID;
    sshTunnelPanel = SSHTunnelPanel.getInstance(databaseUUID);
    btnTestConnection = new Button();
    btnTestConnection.setVisible(false);
    btnTestConnection.addStyleName("btn btn-primary btn-test");
    btnTestConnection.setText(messages.connectionPageButtonTextForTestConnection());
    btnTestConnection.addClickHandler(event -> {
      if (validate()) {

        Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(SPINNER_DIV));
        mainPanel.add(spinner);

        final ConnectionParameters connectionParameters = getValues();

        MigrationService.Util.call((ConnectionResponse result) -> {
          if (result.isConnected()) {
            Dialogs.showInformationDialog(messages.errorMessagesConnectionTitle(),
              messages.connectionPageTextForConnectionSuccess(
                connectionParameters.getJdbcParameters().getConnection().get("database")),
              messages.basicActionClose(), "btn btn-link");
            mainPanel.remove(spinner);
          } else {
            mainPanel.remove(spinner);
            Dialogs.showErrors(messages.errorMessagesConnectionTitle(), result.getMessage(),
              messages.basicActionClose());
          }
        }).testConnection(connectionParameters);
      } else {
        Toast.showError(messages.errorMessagesConnectionTitle(), messages.connectionPageErrorMessageFor(1));
      }
    });
  }

  public void initImportDBMS(final String type, final String targetToken) {
    this.type = type;
    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(SPINNER_DIV));

    jdbcListConnections.add(spinner);

    CreateConnectionHomePanel connectionHomePanel = CreateConnectionHomePanel
      .getInstance(ViewerConstants.EXPORT_FORMAT_SIARD);
    connectionInputPanel.clear();
    connectionInputPanel.add(connectionHomePanel);

    MigrationService.Util.call((List<Module> modules) -> {
      leftSideContainer.removeStyleName("loading-sidebar");
      jdbcListConnections.remove(spinner);
      connectionSidebar = ConnectionSidebar.getInstance(databaseUUID, messages.sidebarMenuTextForDatabases(), modules,
        targetToken);
      jdbcListConnections.add(connectionSidebar);
      leftSideContainer.removeStyleName("loading-sidebar");
      jdbcListConnections.remove(spinner);
      countRows = true;
      dbmsModules = modules;
    }).getDBMSModules("import", null);
  }

  public void initExportDBMS(final String type, final String targetToken) {
    this.type = type;
    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(SPINNER_DIV));

    jdbcListConnections.add(spinner);

    CreateConnectionHomePanel connectionHomePanel = CreateConnectionHomePanel
      .getInstance(ViewerConstants.EXPORT_FORMAT_DBMS);
    connectionInputPanel.clear();
    connectionInputPanel.add(connectionHomePanel);

    MigrationService.Util.call((List<Module> modules) -> {
      connectionSidebar = ConnectionSidebar.getInstance(databaseUUID, messages.sidebarMenuTextForDatabases(), modules, targetToken);

      jdbcListConnections.add(connectionSidebar);
      leftSideContainer.removeStyleName("loading-sidebar");
      jdbcListConnections.remove(spinner);
      countRows = false;
      dbmsModules = modules;
    }).getDBMSModules("export", null);
  }

  public void sideBarHighlighter(String connection) {
    this.clickedOnSidebar = true;
    connectionSidebar.select(connection);
    jdbcListConnections.clear();
    jdbcListConnections.add(connectionSidebar);

    connectionInputPanel.clear();

    selectedConnection = connection;

    final Module module = dbmsModules.stream().filter(c -> c.getModuleName().equals(connection)).findFirst()
      .orElse(new Module());

    List<PreservationParameter> preservationParametersSelected = module.getParameters();

    TabPanel tabPanel = new TabPanel();
    tabPanel.addStyleName("browseItemMetadata connection-panel");
    selected = JDBCPanel.getInstance(connection, preservationParametersSelected, databaseUUID, type, countRows);
    jdbcPanels.add(selected);
    tabPanel.add(selected, messages.connectionPageTextForTabGeneral());
    tabPanel.add(sshTunnelPanel, messages.connectionPageTextForTabSSHTunnel());

    tabPanel.selectTab(0);

    selected.validate(type);
    connectionInputPanel.add(tabPanel);

    if (connectionInputPanel.getWidgetIndex(btnTestConnection) == -1) {
      btnTestConnection.setVisible(true);
      connectionInputPanel.add(btnTestConnection);
    }
  }

  @Override
  public ConnectionParameters getValues() {
    ConnectionParameters parameters = new ConnectionParameters();

    parameters.setJdbcParameters(selected.getValues());
    parameters.setModuleName(selectedConnection);

    if (sshTunnelPanel.isSSHTunnelEnabled()) {
      parameters.setSshConfiguration(sshTunnelPanel.getSSHConfiguration());
    }

    return parameters;
  }

  @Override
  public void clear() {
    for (JDBCPanel jdbc : jdbcPanels) {
      jdbc.clear();
    }

    sshTunnelPanel.clear();
    connectionInputPanel.clear();
    if (connectionSidebar != null) {
      connectionSidebar.selectNone();
    }
    instances.clear();
  }

  public void clearPasswords() {
    for (JDBCPanel jdbc : jdbcPanels) {
      jdbc.clearPasswords();
    }
    sshTunnelPanel.clearPassword();
  }

  public boolean sidebarWasClicked() {
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