package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.NoAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.models.authorization.AuthorizationRules;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PermissionsNavigationPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static Map<String, PermissionsNavigationPanel> instances = new HashMap<>();
  private Set<AuthorizationRules> rules;
  private ViewerDatabase database;
  private MetadataField permissionsList;
  private Button btnEdit;

  private boolean hasPermissionsOrRules = true;

  public static PermissionsNavigationPanel getInstance(ViewerDatabase database,
    Set<AuthorizationRules> authorizationRules) {
    return instances.computeIfAbsent(database.getUuid(),
      k -> new PermissionsNavigationPanel(database, authorizationRules));
  }

  public PermissionsNavigationPanel(ViewerDatabase database, Set<AuthorizationRules> authorizationRules) {
    this.database = database;
    this.rules = authorizationRules;
    if (database.getPermissions().isEmpty() && rules.isEmpty()) {
      this.hasPermissionsOrRules = false;
    }
  }

  public boolean hasPermissionsOrRules() {
    return hasPermissionsOrRules;
  }

  public NavigationPanel build() {
    NavigationPanel panel = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForPermissions());
    panel.addToDescriptionPanel(messages.SIARDHomePageOptionsDescriptionForPermissions());

    btnEdit = new Button();
    btnEdit.setText(messages.basicActionEditPermissions());
    btnEdit.addStyleName("btn btn-outline-primary btn-edit");

    btnEdit.addClickHandler(clickEvent -> {
      Dialogs.showCustomConfirmationDialog(messages.SIARDHomePageDialogTitleForPermissionsList(),
        messages.SIARDHomePageDialogDescriptionForPermissionsList(), "500px", getPermissionListTable(),
        messages.basicActionCancel(), messages.basicActionConfirm(), new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean confirmation) {
            if (confirmation) {
              DatabaseService.Util.call((Set<String> result) -> {
                SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid());
                Toast.showInfo(messages.SIARDHomePageDialogTitleForPermissionsList(),
                  messages.SIARDHomePageDialogMessageForPermissionsList());
              }).updatePermissions(database.getUuid(), database.getPermissions());
            }
          }
        });
    });

    // Permission list
    permissionsList = MetadataField.createInstance(messages.SIARDHomePageLabelForPermissionsRoles(),
      buildPermissionList());
    permissionsList.setCSS(null, "label-field", "value-field");

    panel.addToInfoPanel(permissionsList);
    if (rules.isEmpty()) {
      panel.addButton(new Alert(Alert.MessageAlertType.WARNING,
        "To edit the permissions it is necessary to configure the application with a list of authorization rules."));
    } else {
      panel.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnEdit));
    }

    return panel;
  }

  public void update(ViewerDatabase database) {
    this.database = database;
    permissionsList.getMetadataValue().setText(buildPermissionList());
  }

  private String buildPermissionList() {
    Set<String> permissionList = new HashSet<>();
    for (String databasePermission : database.getPermissions()) {
      boolean foundRule = false;
      for (AuthorizationRules rule : rules) {
        if(databasePermission.equals(rule.getAttributeValue())){
          permissionList.add(rule.getLabel());
          foundRule = true;
          break;
        }
      }
      if(!foundRule){
        permissionList.add(databasePermission);
      }
    }

    return String.join(", ", permissionList);
  }

  private FlowPanel getPermissionListTable() {
    FlowPanel permissionListPanel = new FlowPanel();

    Column<AuthorizationRules, Boolean> checkbox = new Column<AuthorizationRules, Boolean>(
      new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(AuthorizationRules rule) {
        return database.getPermissions().contains(rule.getId());
      }
    };

    checkbox.setFieldUpdater((index, rule, value) -> {
      if (value) {
        if (!database.getPermissions().contains(rule.getId())) {
          database.getPermissions().add(rule.getId());
        }
      } else {
        if (database.getPermissions().contains(rule.getId())) {
          database.getPermissions().remove(rule.getId());
        }
      }
    });

    BasicTablePanel<AuthorizationRules> cellTable = new BasicTablePanel<>(new FlowPanel(),
      SafeHtmlUtils.EMPTY_SAFE_HTML, rules.iterator(),
      new BasicTablePanel.ColumnInfo<AuthorizationRules>("", 3, checkbox),
      new BasicTablePanel.ColumnInfo<AuthorizationRules>(messages.SIARDHomePageLabelForPermissionsTableRuleLabel(), 0,
        new TextColumn<AuthorizationRules>() {
          @Override
          public String getValue(AuthorizationRules rule) {
            return rule.getLabel();
          }
        }),
      new BasicTablePanel.ColumnInfo<AuthorizationRules>(
        messages.SIARDHomePageLabelForPermissionsTableRuleAttributeOperator(), 0, new TextColumn<AuthorizationRules>() {
          @Override
          public String getValue(AuthorizationRules rule) {
            return rule.getAttributeOperator();
          }
        }),
      new BasicTablePanel.ColumnInfo<AuthorizationRules>(
        messages.SIARDHomePageLabelForPermissionsTableRuleAttributeValue(), 0, new TextColumn<AuthorizationRules>() {
          @Override
          public String getValue(AuthorizationRules rule) {
            return rule.getAttributeValue();
          }
        }));

    permissionListPanel.add(cellTable);
    permissionListPanel
      .add(new Alert(Alert.MessageAlertType.INFO, messages.SIARDHomePageDialogDetailsForPermissionsList()));
    return permissionListPanel;
  }
}
