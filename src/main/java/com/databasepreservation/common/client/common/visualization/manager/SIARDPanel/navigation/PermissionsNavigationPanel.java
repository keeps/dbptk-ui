/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.NoAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.columns.TooltipColumn;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroup;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.SwitchBtn;
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

import com.google.gwt.user.client.ui.TextBox;
import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PermissionsNavigationPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static Map<String, PermissionsNavigationPanel> instances = new HashMap<>();

  private ViewerDatabase database;
  private Set<String> databasePermissions;
  private Set<AuthorizationGroup> groups;
  private boolean hasPermissionsOrGroups = true;
  private FlowPanel body;
  private FlowPanel bottom;
  TextBox searchInputBox;
  private Button btnEdit;
  private SwitchBtn btnSwitch;

  private boolean overrideMissingGroups = false;

  public static PermissionsNavigationPanel getInstance(ViewerDatabase database, Set<String> databasePermissions,
    Set<AuthorizationGroup> authorizationGroups) {
    return instances.computeIfAbsent(database.getUuid(),
      k -> new PermissionsNavigationPanel(database, databasePermissions, authorizationGroups));
  }

  public PermissionsNavigationPanel(ViewerDatabase database, Set<String> databasePermissions,
    Set<AuthorizationGroup> authorizationGroups) {
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

    if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
      btnSwitch = new SwitchBtn(messages.SIARDHomePageTitleForPermissionsSwitchButton(), database.isAvailableToSearchAll());
      handleSwitchBottom();
    }

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
      for (AuthorizationGroup authorizationGroup : groups) {
        if (databasePermissions.contains(authorizationGroup.getAttributeValue())) {
          permissionOrGroupsList.add(authorizationGroup.getLabel());
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
        Dialogs.showPermissionsDialog(messages.SIARDHomePageDialogTitleForPermissionsList(),
          messages.SIARDHomePageDialogDescriptionForPermissionsList(), "900px", getGroupsTables(),
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
      if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
        bottom.add(CommonClientUtils.wrapOnDiv("btn-item", btnSwitch));
      }
    }
  }

  private void handleSwitchBottom() {
    btnSwitch.setClickHandler(clickEvent -> {
      btnSwitch.getButton().setValue(!btnSwitch.getButton().getValue(), true);
      DatabaseService.Util.call((Boolean result) -> {
        SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid());
        Toast.showInfo(messages.SIARDHomePageDialogTitleForChangeAvailabilityToSearchAll(),
          messages.SIARDHomePageDialogMessageForChangeAvailabilityToSearchAll());
      }).updateDatabaseSearchAllAvailability(database.getUuid());
    });
  }

  private FlowPanel getGroupsTables() {
    FlowPanel permissionListPanel = new FlowPanel();
    TextBox searchBox = new TextBox();
    searchBox.getElement().setPropertyString("placeholder", messages.searchPlaceholder());
    searchBox.addStyleName("searchBox-permissions");

    permissionListPanel.addStyleName("scrollable-panel");
    permissionListPanel.add(searchBox);

    permissionListPanel
      .add(new Alert(Alert.MessageAlertType.INFO, messages.SIARDHomePageDialogDetailsForPermissionsList()));

    Column<AuthorizationGroup, Boolean> checkbox = new Column<AuthorizationGroup, Boolean>(
      new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(AuthorizationGroup group) {
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

    BasicTablePanel<AuthorizationGroup> cellTable = buildGroupsTable(groups, checkbox);
    permissionListPanel.add(cellTable);

    searchBox.addChangeHandler(event -> {
      doSearch(searchBox.getValue(), checkbox, permissionListPanel, cellTable);
    });

    searchBox.addKeyUpHandler(event -> {
      doSearch(searchBox.getValue(), checkbox, permissionListPanel, cellTable);
    });

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
        messages.SIARDHomePageDialogDetailsForUnknownPermissions(String.join(", ", missingGroups)),
        checkBoxOverrideMissingGroups);

      permissionListPanel.add(alert);

    }
    return permissionListPanel;
  }

  private BasicTablePanel<AuthorizationGroup> buildGroupsTable(Set<AuthorizationGroup> groups,
    Column<AuthorizationGroup, Boolean> checkbox) {
    return new BasicTablePanel<>(new FlowPanel(), SafeHtmlUtils.EMPTY_SAFE_HTML, groups.iterator(),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>("", 3, checkbox),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>(messages.SIARDHomePageLabelForPermissionsTableGroupLabel(), 7,
        new TooltipColumn<AuthorizationGroup>() {
          @Override
          public SafeHtml getValue(AuthorizationGroup group) {
            return SafeHtmlUtils.fromString(group.getLabel());
          }
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>(
        messages.SIARDHomePageLabelForPermissionsTableGroupAttributeName(), 7, new TooltipColumn<AuthorizationGroup>() {
          @Override
          public SafeHtml getValue(AuthorizationGroup group) {
            return SafeHtmlUtils.fromString(group.getAttributeName());
          }
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>(
        messages.SIARDHomePageLabelForPermissionsTableGroupAttributeOperator(), 7,
        new TooltipColumn<AuthorizationGroup>() {
          @Override
          public SafeHtml getValue(AuthorizationGroup group) {
            return SafeHtmlUtils.fromString(group.getAttributeOperator());
          }
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>(
        messages.SIARDHomePageLabelForPermissionsTableGroupAttributeValue(), 0,
        new TooltipColumn<AuthorizationGroup>() {
          @Override
          public SafeHtml getValue(AuthorizationGroup group) {
            return SafeHtmlUtils.fromString(group.getAttributeValue());
          }
        }, "force_column_ellipsis"));
  }

  private void doSearch(String searchValue, Column<AuthorizationGroup, Boolean> checkbox, FlowPanel permissionListPanel,
    BasicTablePanel<AuthorizationGroup> cellTable) {
    permissionListPanel.remove(cellTable);

    if (ViewerStringUtils.isBlank(searchValue)) {
      permissionListPanel.remove(1);
      permissionListPanel.add(showAll(checkbox));
    } else {
      permissionListPanel.remove(1);
      permissionListPanel.add(showMatching(searchValue, checkbox));
    }
  }

  private BasicTablePanel<AuthorizationGroup> showAll(Column<AuthorizationGroup, Boolean> checkBox) {
    // Clear the table and add all groups
    return buildGroupsTable(groups, checkBox);
  }

  private BasicTablePanel<AuthorizationGroup> showMatching(final String searchValue,
    Column<AuthorizationGroup, Boolean> checkbox) {
    // Filter the groups based on the search value
    List<AuthorizationGroup> matchingGroups = groups.stream()
      .filter(group -> group.getLabel().toLowerCase().contains(searchValue.toLowerCase())
        || group.getAttributeName().toLowerCase().contains(searchValue.toLowerCase())
        || group.getAttributeOperator().toLowerCase().contains(searchValue.toLowerCase())
        || group.getAttributeValue().toLowerCase().contains(searchValue.toLowerCase()))
      .collect(Collectors.toList());

    return buildGroupsTable(new HashSet<>(matchingGroups), checkbox);
  }

  private Set<String> retrieveMissingGroups() {
    // Checking for database permissions that doesn't have any registered groups
    Set<String> missingGroups = new HashSet<>();
    for (String permission : databasePermissions) {
      boolean foundGroup = false;
      for (AuthorizationGroup group : groups) {
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
