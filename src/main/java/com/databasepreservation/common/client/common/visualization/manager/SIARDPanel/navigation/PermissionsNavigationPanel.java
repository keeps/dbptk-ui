package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.NoAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.columns.TooltipColumn;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroups;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PermissionsNavigationPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static Map<String, PermissionsNavigationPanel> instances = new HashMap<>();

  private ViewerDatabase database;
  private Set<String> databasePermissions;
  private Set<AuthorizationGroups> groups;
  private boolean hasPermissionsOrGroups = true;
  private FlowPanel body;
  private FlowPanel bottom;
  private Button btnEdit;

  private boolean overrideMissingGroups = false;

  public static PermissionsNavigationPanel getInstance(ViewerDatabase database, Set<String> databasePermissions,
    Set<AuthorizationGroups> authorizationGroups) {
    return instances.computeIfAbsent(database.getUuid(),
      k -> new PermissionsNavigationPanel(database, databasePermissions, authorizationGroups));
  }

  public PermissionsNavigationPanel(ViewerDatabase database, Set<String> databasePermissions,
    Set<AuthorizationGroups> authorizationGroups) {
    this.database = database;
    this.groups = authorizationGroups;
    this.databasePermissions = databasePermissions;
    if (databasePermissions.isEmpty() && groups.isEmpty()) {
      this.hasPermissionsOrGroups = false;
    }
  }

  public boolean hasPermissionsOrGroups() {
    return hasPermissionsOrGroups;
  }

  public NavigationPanel build() {
    NavigationPanel panel = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForPermissions());
    panel.addToDescriptionPanel(messages.SIARDHomePageOptionsDescriptionForPermissions());
    body = new FlowPanel();
    bottom = new FlowPanel();
    panel.addToInfoPanel(body);
    panel.addButton(bottom);

    update(databasePermissions);

    return panel;
  }

  public void update(Set<String> databasePermissions) {
    this.databasePermissions = databasePermissions;
    updateBody();
    updateBottom();
  }

  private void updateBody() {
    body.clear();
    if (databasePermissions.isEmpty()) {
      body.add(new Alert(Alert.MessageAlertType.WARNING, messages.SIARDHomePageTextForMissingDatabasePermissions()));
    } else {
      ArrayList<String> permissionOrGroupsList = new ArrayList<>();
      // Add the corresponding label to the permission
      for (AuthorizationGroups authorizationGroups : groups) {
        if (databasePermissions.contains(authorizationGroups.getAttributeValue())) {
          permissionOrGroupsList.add(authorizationGroups.getLabel());
        }
      }

      SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
      for (String permission : permissionOrGroupsList) {
        safeHtmlBuilder.append(LabelUtils.getDatabasePermission(permission, true));
      }

      // Add all permissions that do not contain groups
      for (String missingGroupForPermission : retrieveMissingGroups()) {
        safeHtmlBuilder.append(LabelUtils.getDatabasePermission(missingGroupForPermission, false));
      }

      MetadataField groupMetadataField = MetadataField.createInstance(messages.SIARDHomePageLabelForPermissionsRoles(),
        safeHtmlBuilder.toSafeHtml());
      groupMetadataField.setCSS(null, "label-field", "value-field metadata_field_flex");
      body.add(groupMetadataField);
    }
  }

  private void updateBottom() {
    bottom.clear();
    if (groups.isEmpty()) {
      bottom.add(
        new Alert(Alert.MessageAlertType.WARNING, messages.SIARDHomePageTextForMissingAuthorizationGroupsProperties()));
    } else {
      btnEdit = new Button();
      btnEdit.setText(messages.basicActionEditPermissions());
      btnEdit.addStyleName("btn btn-outline-primary btn-edit");

      btnEdit.addClickHandler(clickEvent -> {
        overrideMissingGroups = false;
        Dialogs.showCustomConfirmationDialog(messages.SIARDHomePageDialogTitleForPermissionsList(),
          messages.SIARDHomePageDialogDescriptionForPermissionsList(), "620px", getGroupsTable(),
          messages.basicActionCancel(), messages.basicActionConfirm(), new NoAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean confirmation) {
              if (confirmation) {
                if (overrideMissingGroups) {
                  databasePermissions.removeAll(retrieveMissingGroups());
                }
                DatabaseService.Util.call((Set<String> result) -> {
                  SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid());
                  Toast.showInfo(messages.SIARDHomePageDialogTitleForPermissionsList(),
                    messages.SIARDHomePageDialogMessageForPermissionsList());
                }).updateDatabasePermissions(database.getUuid(), databasePermissions);
              }
            }
          });
      });
      bottom.add(CommonClientUtils.wrapOnDiv("btn-item", btnEdit));
    }
  }

  private FlowPanel getGroupsTable() {
    FlowPanel permissionListPanel = new FlowPanel();
    permissionListPanel
      .add(new Alert(Alert.MessageAlertType.INFO, messages.SIARDHomePageDialogDetailsForPermissionsList()));

    Column<AuthorizationGroups, Boolean> checkbox = new Column<AuthorizationGroups, Boolean>(
      new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(AuthorizationGroups group) {
        return databasePermissions.contains(group.getAttributeValue());
      }
    };

    checkbox.setFieldUpdater((index, group, value) -> {
      if (value) {
        // Add
        if (!databasePermissions.contains(group.getAttributeValue())) {
          databasePermissions.add(group.getAttributeValue());
        }
      } else {
        // Remove
        if (databasePermissions.contains(group.getAttributeValue())) {
          databasePermissions.remove(group.getAttributeValue());
        }
      }
    });

    BasicTablePanel<AuthorizationGroups> cellTable = new BasicTablePanel<>(new FlowPanel(),
      SafeHtmlUtils.EMPTY_SAFE_HTML, groups.iterator(),
      new BasicTablePanel.ColumnInfo<AuthorizationGroups>("", 3, checkbox),
      new BasicTablePanel.ColumnInfo<AuthorizationGroups>(messages.SIARDHomePageLabelForPermissionsTableGroupLabel(), 7,
        new TooltipColumn<AuthorizationGroups>() {
          @Override
          public SafeHtml getValue(AuthorizationGroups group) {
            return SafeHtmlUtils.fromString(group.getLabel());
          }
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroups>(
        messages.SIARDHomePageLabelForPermissionsTableGroupAttributeName(), 7, new TooltipColumn<AuthorizationGroups>() {
          @Override
          public SafeHtml getValue(AuthorizationGroups group) {
            return SafeHtmlUtils.fromString(group.getAttributeName());
          }
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroups>(
        messages.SIARDHomePageLabelForPermissionsTableGroupAttributeOperator(), 7,
        new TooltipColumn<AuthorizationGroups>() {
          @Override
          public SafeHtml getValue(AuthorizationGroups group) {
            return SafeHtmlUtils.fromString(group.getAttributeOperator());
          }
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroups>(
        messages.SIARDHomePageLabelForPermissionsTableGroupAttributeValue(), 0, new TooltipColumn<AuthorizationGroups>() {
          @Override
          public SafeHtml getValue(AuthorizationGroups group) {
            return SafeHtmlUtils.fromString(group.getAttributeValue());
          }
        }, "force_column_ellipsis"));

    permissionListPanel.add(cellTable);
    Set<String> missingGroups = retrieveMissingGroups();
    if (!missingGroups.isEmpty()) {
      CheckBox checkBoxOverrideMissingGroups = new CheckBox();
      checkBoxOverrideMissingGroups.setValue(false);
      checkBoxOverrideMissingGroups.setText(messages.SIARDHomePageDialogActionForOverridePermissions());
      checkBoxOverrideMissingGroups.addValueChangeHandler(event -> {
        if (event.getValue()) {
          overrideMissingGroups = true;
        } else {
          overrideMissingGroups = false;
        }
      });
      Alert alert = new Alert(Alert.MessageAlertType.WARNING,
          messages.SIARDHomePageDialogDetailsForUnknownPermissions(String.join(", ", missingGroups)), checkBoxOverrideMissingGroups);

      permissionListPanel.add(alert);

    }
    return permissionListPanel;
  }

  private Set<String> retrieveMissingGroups() {
    // Checking for database permissions that doesn't have any registered groups
    Set<String> missingGroups = new HashSet<>();
    for (String permission : databasePermissions) {
      boolean foundGroup = false;
      for (AuthorizationGroups group : groups) {
        if (group.getAttributeValue().equals(permission)) {
          foundGroup = true;
          break;
        }
      }
      if (!foundGroup) {
        missingGroups.add(permission);
      }
    }
    return missingGroups;
  }
}
