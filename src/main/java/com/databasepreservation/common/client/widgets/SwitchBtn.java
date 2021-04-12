/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleCheckBox;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SwitchBtn extends FlowPanel {
  private Label switchLabel, labelForSwitch;
  private SimpleCheckBox button;

  public SwitchBtn(String title, Boolean enable) {
    switchLabel = new Label();
    switchLabel.addStyleName("switch-label");
    switchLabel.setText(title);

    labelForSwitch = new Label(); // workaround for ie11
    labelForSwitch.setStyleName("label-for-switch");

    button = new SimpleCheckBox();
    button.setStyleName("switch");
    button.setValue(enable);

    setStyleName("switch-widget");
    add(switchLabel);
    add(button);
    add(labelForSwitch);
  }

  public void setClickHandler(ClickHandler clickHandler){
    labelForSwitch.addClickHandler(clickHandler);
  }

  public SimpleCheckBox getButton() {
    return button;
  }
}
