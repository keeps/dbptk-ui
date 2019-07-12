package com.databasepreservation.main.desktop.client.dbptk.wizard;

import java.util.HashMap;

import com.google.gwt.user.client.ui.Composite;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class WizardPanel<T> extends Composite {

  public abstract void clear();

  public abstract boolean validate();

  public abstract T getValues();

  public abstract void error();
}
