package com.databasepreservation.main.desktop.client.dbptk.wizard;

import com.google.gwt.user.client.ui.Composite;

import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class WizardPanel extends Composite {

  public abstract void clear();

  public abstract boolean validate();

  public abstract HashMap<String, String> getValues();

  public abstract void error();
}
