/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ValidationUiUtils {
  private ValidationUiUtils() {
  }

  public static void showError(Widget input, Label errorLabel, String message) {
    input.addStyleName("dialog-input-error");
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }

  public static void clearError(Widget input, Label errorLabel) {
    input.removeStyleName("dialog-input-error");
    errorLabel.setText("");
    errorLabel.setVisible(false);
  }
}
