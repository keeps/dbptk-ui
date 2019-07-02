package com.databasepreservation.main.desktop.client.dbptk.wizard;

import com.google.gwt.user.client.ui.Composite;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class WizardPanel extends Composite {

  public abstract void clear();

  public abstract void validate();

  public abstract void getValues();
}
