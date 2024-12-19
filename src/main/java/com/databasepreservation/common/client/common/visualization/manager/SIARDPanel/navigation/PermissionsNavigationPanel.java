/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.ArrayList;
import java.util.Date;
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
import com.databasepreservation.common.client.common.lists.columns.ButtonColumn;
import com.databasepreservation.common.client.common.lists.columns.TooltipColumn;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroup;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.SwitchBtn;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PermissionsNavigationPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static Map<String, PermissionsNavigationPanel> instances = new HashMap<>();

  private ViewerDatabase database;
  private Set<String> databasePermissionGroups;
  private Set<AuthorizationGroup> groups;
  private HashMap<String, AuthorizationDetails> groupDetails;
  private boolean hasPermissionsOrGroups = true;
  private FlowPanel body;
  private FlowPanel bottom;
  private Button btnEdit;
  private SwitchBtn btnSwitch;
  private Column<AuthorizationGroup, Boolean> checkbox;
  private Column<AuthorizationGroup, String> expiry;
  private BasicTablePanel<AuthorizationGroup> cellTable;

  private AuthorizationGroup currentGroup;
  private DateTimeFormat htmlInputPresentedDateFormat = DateTimeFormat.getFormat("MM/dd/yyyy");
  private DateTimeFormat htmlAttributesDateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
  private DateTimeFormat htmlInputDateFormat = DateTimeFormat.getFormat("yyyy-MM-ddTHH:mm:ssZ");
  private Date lastDate;

  private boolean overrideMissingGroups = false;

  public static PermissionsNavigationPanel getInstance(ViewerDatabase database,
    Map<String, AuthorizationDetails> databasePermissions, Set<AuthorizationGroup> authorizationGroups) {
    return instances.computeIfAbsent(database.getUuid(),
      k -> new PermissionsNavigationPanel(database, databasePermissions, authorizationGroups));
  }

  public PermissionsNavigationPanel(ViewerDatabase database, Map<String, AuthorizationDetails> databasePermissionGroups,
    Set<AuthorizationGroup> authorizationGroups) {
    this.database = database;
    this.groups = authorizationGroups;
    this.groupDetails = new HashMap<>();
    this.groupDetails.putAll(databasePermissionGroups);
    this.databasePermissionGroups = new HashSet<>();
    this.databasePermissionGroups.addAll(databasePermissionGroups.keySet());
    if (databasePermissionGroups.isEmpty() && groups.isEmpty()) {
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
      btnSwitch = new SwitchBtn(messages.SIARDHomePageTitleForPermissionsSwitchButton(),
        database.isAvailableToSearchAll());
      handleSwitchBottom();
    }

    update();

    return panel;
  }

  public void update() {
    updateBody();
    updateBottom();
  }

  public void update(Map<String, AuthorizationDetails> newPermissions) {
    this.groupDetails.clear();
    this.groupDetails.putAll(newPermissions);
    this.databasePermissionGroups.clear();
    this.databasePermissionGroups.addAll(newPermissions.keySet());
    updateBody();
    updateBottom();
  }

  private void updateBody() {
    body.clear();
    if (databasePermissionGroups.isEmpty()) {
      body.add(new Alert(Alert.MessageAlertType.WARNING, messages.SIARDHomePageTextForMissingDatabasePermissions()));
    } else {
      ArrayList<String> permissionOrGroupsList = new ArrayList<>();
      // Add the corresponding label to the permission
      for (AuthorizationGroup authorizationGroup : groups) {
        if (databasePermissionGroups.contains(authorizationGroup.getAttributeValue())) {
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
          messages.SIARDHomePageDialogDescriptionForPermissionsList(), "1000px", getGroupsTables(),
          messages.basicActionCancel(), messages.basicActionConfirm(), new NoAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean confirmation) {
              if (confirmation) {
                if (overrideMissingGroups) {
                  databasePermissionGroups.removeAll(retrieveMissingGroups());
                }
                DatabaseService.Util.call((Map<String, AuthorizationDetails> result) -> {
                  SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid());
                  Toast.showInfo(messages.SIARDHomePageDialogTitleForPermissionsList(),
                    messages.SIARDHomePageDialogMessageForPermissionsList());
                }).updateDatabasePermissions(database.getUuid(), createDatabasePermissionsMap());
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

    checkbox = new Column<AuthorizationGroup, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(AuthorizationGroup group) {
        return databasePermissionGroups.contains(group.getAttributeValue());
      }
    };

    checkbox.setFieldUpdater((index, group, value) -> {
      if (value) {
        // Add
        if (!databasePermissionGroups.contains(group.getAttributeValue())) {
          databasePermissionGroups.add(group.getAttributeValue());
        }
      } else {
        // Remove
        databasePermissionGroups.remove(group.getAttributeValue());
      }
      cellTable.refresh();
    });

    expiry = new ButtonColumn<AuthorizationGroup>() {
      @Override
      public String getValue(AuthorizationGroup database) {
        String ret = "";
        if (databasePermissionGroups.contains(database.getAttributeValue())) {
          AuthorizationDetails authorizationDetails = groupDetails.getOrDefault(database.getAttributeValue(),
            new AuthorizationDetails());
          return authorizationDetails.hasExpiryDate()
            ? htmlInputPresentedDateFormat.format(authorizationDetails.getExpiry(), TimeZone.createTimeZone(0))
            : messages.SIARDHomePageLabelForPermissionsTableButtonNoExpiryDate();
        }
        return ret;
      }

      @Override
      public void render(Cell.Context context, AuthorizationGroup object, SafeHtmlBuilder sb) {
        String value = getValue(object);
        if (databasePermissionGroups.contains(object.getAttributeValue())) {
          sb.appendHtmlConstant("<button class=\"btn btn-link-info\" type=\"button\" tabindex=\"-1\">");
        } else {
          sb.appendHtmlConstant("<button class=\"btn btn-link-info\" type=\"button\" tabindex=\"-1\" disabled>");
        }
        if (value != null) {
          sb.append(SafeHtmlUtils.fromString(value));
        }
        sb.appendHtmlConstant("</button>");
      }
    };

    expiry.setFieldUpdater((index, authorizationGroup, value) -> {
      currentGroup = authorizationGroup;
      showDatePicker();
    });

    buildGroupsTable(groups, checkbox, expiry);

    permissionListPanel.add(cellTable);

    searchBox.addChangeHandler(event -> {
      doSearch(searchBox.getValue(), permissionListPanel);
    });

    searchBox.addKeyUpHandler(event -> {
      doSearch(searchBox.getValue(), permissionListPanel);
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

  private void showDatePicker() {
    String today = htmlAttributesDateFormat.format(new Date());
    String currentDateValueAttribute = "";
    if (groupDetails.getOrDefault(currentGroup.getAttributeValue(), new AuthorizationDetails()).hasExpiryDate()) {
      currentDateValueAttribute = "value=\""
        + htmlAttributesDateFormat.format(groupDetails.get(currentGroup.getAttributeValue()).getExpiry()) + "\"";
    }
    HTML htmlDatePicker = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<input type=date id=\"expiryDatePicker\" min=" + today + " " + currentDateValueAttribute + "></input>")) {
      @Override
      protected void onDetach() {
        super.onDetach();
        String datePickerValue = JavascriptUtils.getInputValue("expiryDatePicker");
        if (datePickerValue != null && !datePickerValue.isEmpty()) {
          lastDate = htmlInputDateFormat.parse(datePickerValue + "T23:59:59-0000");
        } else {
          lastDate = null;
        }
      }
    };
    htmlDatePicker.addStyleName("datepicker_dialog");
    RadioButton pickDateButton = new RadioButton("expiryDatePickerGroup",
      messages.SIARDHomePageLabelForPermissionsRadioButtonPickDate());
    pickDateButton.addValueChangeHandler(event -> {
      if (event.getValue()) {
        JavascriptUtils.removeAttribute("expiryDatePicker", "disabled");
      }
    });
    pickDateButton.setValue(true);
    FlowPanel pickDateButtonContainer = new FlowPanel();
    pickDateButtonContainer.addStyleName("datepicker_dialog_row");
    pickDateButtonContainer.add(pickDateButton);
    pickDateButtonContainer.add(htmlDatePicker);

    RadioButton neverButton = new RadioButton("expiryDatePickerGroup",
      messages.SIARDHomePageLabelForPermissionsRadioButtonNever());
    neverButton.addValueChangeHandler(event -> {
      if (event.getValue()) {
        JavascriptUtils.setAttribute("expiryDatePicker", "disabled", "true");
      }
    });

    FlowPanel confirmDialogHelper = new FlowPanel();
    confirmDialogHelper.add(pickDateButtonContainer);
    confirmDialogHelper.add(neverButton);

    Dialogs.showCustomConfirmationDialog(messages.SIARDHomePageTitleForDateEdit(), SafeHtmlUtils.EMPTY_SAFE_HTML,
      "360px", confirmDialogHelper, messages.basicActionCancel(), messages.basicActionConfirm(),
      new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmation) {
          if (confirmation) {
            AuthorizationDetails authorizationDetails = groupDetails.getOrDefault(currentGroup.getAttributeValue(),
              new AuthorizationDetails());
            if (pickDateButton.getValue()) {
              authorizationDetails.setExpiry(lastDate);
            } else if (neverButton.getValue()) {
              authorizationDetails.setExpiry(null);
            }
            groupDetails.put(currentGroup.getAttributeValue(), authorizationDetails);
            cellTable.refresh();
          }
        }
      });
  }

  private void buildGroupsTable(Set<AuthorizationGroup> groups, Column<AuthorizationGroup, Boolean> checkbox,
    Column<AuthorizationGroup, String> expiry) {
    cellTable = new BasicTablePanel<>(new FlowPanel(), SafeHtmlUtils.EMPTY_SAFE_HTML, groups.iterator(),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>("", 3, checkbox),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>(messages.SIARDHomePageLabelForPermissionsTableGroupLabel(), 15,
        new TooltipColumn<AuthorizationGroup>() {
          @Override
          public SafeHtml getValue(AuthorizationGroup group) {
            return SafeHtmlUtils.fromString(group.getLabel());
          }
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>(
        messages.SIARDHomePageLabelForPermissionsTableGroupAttributeName(), 12,
        new TooltipColumn<AuthorizationGroup>() {
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
        }, "force_column_ellipsis"),
      new BasicTablePanel.ColumnInfo<AuthorizationGroup>(
        messages.SIARDHomePageLabelForPermissionsTableGroupExpiryDate(), 12, expiry,
        "force_column_ellipsis expiry_column"));
  }

  private void doSearch(String searchValue, FlowPanel permissionListPanel) {
    permissionListPanel.remove(cellTable);

    if (ViewerStringUtils.isBlank(searchValue)) {
      showAll();
      permissionListPanel.add(cellTable);
    } else {
      showMatching(searchValue);
      permissionListPanel.add(cellTable);
    }
  }

  private void showAll() {
    // Show all the groups
    buildGroupsTable(groups, checkbox, expiry);
  }

  private void showMatching(final String searchValue) {
    // Filter the groups based on the search value
    List<AuthorizationGroup> matchingGroups = groups.stream()
      .filter(group -> group.getLabel().toLowerCase().contains(searchValue.toLowerCase())
        || group.getAttributeName().toLowerCase().contains(searchValue.toLowerCase())
        || group.getAttributeOperator().toLowerCase().contains(searchValue.toLowerCase())
        || group.getAttributeValue().toLowerCase().contains(searchValue.toLowerCase()))
      .collect(Collectors.toList());

    buildGroupsTable(new HashSet<>(matchingGroups), checkbox, expiry);
  }

  private Set<String> retrieveMissingGroups() {
    // Checking for database permissions that doesn't have any registered groups
    Set<String> missingGroups = new HashSet<>();
    for (String permission : databasePermissionGroups) {
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

  private Map<String, AuthorizationDetails> createDatabasePermissionsMap() {
    Map<String, AuthorizationDetails> permissions = new HashMap<>();
    for (String permission : databasePermissionGroups) {
      permissions.put(permission, groupDetails.getOrDefault(permission, new AuthorizationDetails()));
    }
    return permissions;
  }
}
