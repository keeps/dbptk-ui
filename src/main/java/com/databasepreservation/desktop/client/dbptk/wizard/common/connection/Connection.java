package com.databasepreservation.desktop.client.dbptk.wizard.common.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.widgets.Toast;
import com.databasepreservation.common.shared.models.DBPTKModule;
import com.databasepreservation.common.shared.models.PreservationParameter;
import com.databasepreservation.common.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.desktop.client.common.sidebar.ConnectionSidebar;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

  interface DBPTKModuleMapper extends ObjectMapper<DBPTKModule> {}
  interface ConnectionMapper extends ObjectMapper<ConnectionParameters> {}

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
  private DBPTKModule dbmsModule;
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

        Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
            SPINNER_DIV));
        mainPanel.add(spinner);

        final ConnectionParameters connectionParameters = getValues();
        ConnectionMapper mapper = GWT.create(ConnectionMapper.class);
        String connectionParametersJSON = mapper.write(connectionParameters);
        GWT.log(connectionParametersJSON);
        BrowserService.Util.getInstance().testConnection(databaseUUID, connectionParametersJSON,
          new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
              mainPanel.remove(spinner);
              Dialogs.showErrors(messages.errorMessagesConnectionTitle(), caught.getMessage(),
                messages.basicActionClose());
            }

            @Override
            public void onSuccess(Boolean result) {
              Dialogs.showInformationDialog(messages.errorMessagesConnectionTitle(),
                messages.connectionPageTextForConnectionSuccess(
                  connectionParameters.getJDBCConnectionParameters().getConnection().get("database")),
                messages.basicActionClose(), "btn btn-link");
              mainPanel.remove(spinner);
            }
          });
      } else {
        Toast.showError(messages.errorMessagesConnectionTitle(), messages.connectionPageErrorMessageFor(1));
      }
    });
  }

  public void initImportDBMS(final String type, final String targetToken) {
    this.type = type;
    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
        SPINNER_DIV));

    jdbcListConnections.add(spinner);

    CreateConnectionHomePanel connectionHomePanel = CreateConnectionHomePanel
      .getInstance(ViewerConstants.EXPORT_FORMAT_SIARD);
    connectionInputPanel.clear();
    connectionInputPanel.add(connectionHomePanel);

    BrowserService.Util.getInstance().getDatabaseImportModules(new DefaultAsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        DBPTKModuleMapper mapper = GWT.create( DBPTKModuleMapper.class );
        DBPTKModule module = mapper.read(result);
        leftSideContainer.removeStyleName("loading-sidebar");
        jdbcListConnections.remove(spinner);
        connectionSidebar = ConnectionSidebar.getInstance(databaseUUID, messages.sidebarMenuTextForDatabases(),
          FontAwesomeIconManager.DATABASE, module, targetToken);
        jdbcListConnections.add(connectionSidebar);
        leftSideContainer.removeStyleName("loading-sidebar");
        jdbcListConnections.remove(spinner);
        countRows = true;
        dbmsModule = module;
      }
    });
  }

  public void initExportDBMS(final String type, final String targetToken) {
    this.type = type;
    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
        SPINNER_DIV));

    jdbcListConnections.add(spinner);

    CreateConnectionHomePanel connectionHomePanel = CreateConnectionHomePanel
      .getInstance(ViewerConstants.EXPORT_FORMAT_DBMS);
    connectionInputPanel.clear();
    connectionInputPanel.add(connectionHomePanel);

    BrowserService.Util.getInstance().getDatabaseExportModules(new DefaultAsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        DBPTKModuleMapper mapper = GWT.create( DBPTKModuleMapper.class );
        DBPTKModule module = mapper.read(result);
        connectionSidebar = ConnectionSidebar.getInstance(databaseUUID, messages.sidebarMenuTextForDatabases(),
          FontAwesomeIconManager.DATABASE, module, targetToken);

        jdbcListConnections.add(connectionSidebar);
        leftSideContainer.removeStyleName("loading-sidebar");
        jdbcListConnections.remove(spinner);
        countRows = false;
        dbmsModule = module;
      }
    });
  }

  public void sideBarHighlighter(String connection) {
    this.clickedOnSidebar = true;
    connectionSidebar.select(connection);
    jdbcListConnections.clear();
    jdbcListConnections.add(connectionSidebar);

    connectionInputPanel.clear();

    selectedConnection = connection;

    List<PreservationParameter> preservationParametersSelected = dbmsModule.getParameters(connection);

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

    parameters.setJDBCConnectionParameters(selected.getValues());
    parameters.setModuleName(selectedConnection);

    if (sshTunnelPanel.isSSHTunnelEnabled()) {
      parameters.setSSHConfiguration(sshTunnelPanel.getSSHConfiguration());
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