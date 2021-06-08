/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.CustomizeProperties;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomizeColumnOptionsPanel extends ColumnOptionsPanel {
  interface ColumnsOptionsPanelUiBinder extends UiBinder<Widget, CustomizeColumnOptionsPanel> {
  }

  private static ColumnsOptionsPanelUiBinder binder = GWT.create(ColumnsOptionsPanelUiBinder.class);

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  HTML widthDescription;

  @UiField
  IntegerBox widthContent;

  @UiField
  ListBox unitListBox;

  private final List<Style.Unit> units = new ArrayList<>();

  public static CustomizeColumnOptionsPanel createInstance(ColumnStatus columnConfiguration) {
    return new CustomizeColumnOptionsPanel(columnConfiguration);
  }

  public boolean validate() {
    try {
      Double.parseDouble(widthContent.getText());
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public CustomizeProperties getProperties() {
    CustomizeProperties properties = new CustomizeProperties();

    properties.setWidth(widthContent.getText());
    properties.setUnit(Style.Unit.valueOf(unitListBox.getSelectedValue()));

    return properties;
  }

  private CustomizeColumnOptionsPanel(ColumnStatus columnConfiguration) {
    initWidget(binder.createAndBindUi(this));

    units.add(Style.Unit.PX);
    units.add(Style.Unit.EM);
    units.add(Style.Unit.PCT);
    units.add(Style.Unit.MM);

    widthContent.setText(columnConfiguration.getSearchStatus().getList().getCustomizeProperties().getWidth());
    widthDescription.setHTML(messages.columnManagementCustomizeColumnTextForWidthDescription("https://www.w3schools.com/cssref/css_units.asp"));

    setupNumericTextBox(widthContent);
    setupUnitListBox(columnConfiguration);
  }

  private void setupNumericTextBox(Widget... widgets) {
    for (Widget widget : widgets) {
      widget.getElement().setAttribute("type", "number");
      widget.getElement().setAttribute("min", "0");
    }
  }

  private void setupUnitListBox(ColumnStatus columnConfig) {
    int index = 0;
    for (Style.Unit unit : units) {
      unitListBox.addItem(unit.getType(), unit.toString());
      if (unit.equals(columnConfig.getSearchStatus().getList().getCustomizeProperties().getUnit())) {
        unitListBox.setSelectedIndex(index);
      }
      index++;
    }
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getExportTemplate() {
    return null;
  }
}
