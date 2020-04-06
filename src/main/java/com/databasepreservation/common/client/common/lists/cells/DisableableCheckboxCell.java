package com.databasepreservation.common.client.common.lists.cells;

import com.databasepreservation.common.client.common.lists.cells.helper.CheckboxData;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisableableCheckboxCell extends AbstractEditableCell<CheckboxData, CheckboxData> {
  private static final SafeHtml INPUT_CHECKED = SafeHtmlUtils
    .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked/>");
  private static final SafeHtml INPUT_UNCHECKED = SafeHtmlUtils
    .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\"/>");
  private static final SafeHtml INPUT_UNCHECKED_DISABLED = SafeHtmlUtils
    .fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>");

  private final boolean dependsOnSelection;
  private final boolean handlesSelection;

  public DisableableCheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
    super("change", "keydown");
    this.dependsOnSelection = dependsOnSelection;
    this.handlesSelection = handlesSelection;
  }

  public boolean dependsOnSelection() {
    return this.dependsOnSelection;
  }

  public boolean handlesSelection() {
    return this.handlesSelection;
  }

  @Override
  public boolean isEditing(Context context, Element element, CheckboxData value) {
    return false;
  }

  public void onBrowserEvent(Context context, Element parent, CheckboxData value, NativeEvent event,
    ValueUpdater<CheckboxData> valueUpdater) {
    String type = event.getType();
    boolean enterPressed = "keydown".equals(type) && event.getKeyCode() == 13;
    if ("change".equals(type) || enterPressed) {
      InputElement input = parent.getFirstChild().cast();
      boolean isChecked = input.isChecked();
      if (enterPressed && (this.handlesSelection() || !this.dependsOnSelection())) {
        isChecked = !isChecked;
        input.setChecked(isChecked);
      }

      if (value.isChecked() != isChecked && !this.dependsOnSelection()) {
        this.setViewData(context.getKey(), value);
      } else {
        this.clearViewData(context.getKey());
      }

      if (valueUpdater != null) {
        value.setChecked(isChecked);
        valueUpdater.update(value);
      }
    }
  }

  @Override
  public void render(Context context, CheckboxData value, SafeHtmlBuilder safeHtmlBuilder) {
    // Get the view data.
    Object key = context.getKey();
    CheckboxData viewData = getViewData(key);
    if (viewData != null && viewData.equals(value)) {
      clearViewData(key);
      viewData = null;
    }

    CheckboxData data = viewData != null ? viewData : value;

    if (value != null && data.isChecked()) {
      safeHtmlBuilder.append(INPUT_CHECKED);
    } else if (value != null && value.isDisable()) {
      safeHtmlBuilder.append(INPUT_UNCHECKED_DISABLED);
    } else {
      safeHtmlBuilder.append(INPUT_UNCHECKED);
    }
  }
}
