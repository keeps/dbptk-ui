package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomViews extends WizardPanel {
  interface CustomViewsUiBinder extends UiBinder<Widget, CustomViews> {
  }

  private static CustomViewsUiBinder binder = GWT.create(CustomViewsUiBinder.class);

  @UiField
  FlowPanel content;

  private static CustomViews instance = null;

  public static CustomViews getInstance() {
    if (instance == null) {
      instance = new CustomViews();
    }
    return instance;
  }

  private CustomViews() {
    initWidget(binder.createAndBindUi(this));

    content.add(new HTML("Custom Views"));
  }

  @Override
  public void clear() {

  }

  @Override
  public boolean validate() {
    return false;
  }

  @Override
  public HashMap<String, String> getValues() {
    return null;
  }

  @Override
  public void error() {

  }

  public void sideBarHighlighter(String value) {}
}
