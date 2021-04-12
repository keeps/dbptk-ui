/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.desktop.client.dbptk.wizard;

import com.google.gwt.user.client.ui.Composite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class WizardManager extends Composite {

    protected List<WizardPanel> wizardInstances = new ArrayList<>();

    public abstract void enableNext(boolean value);

    protected abstract void updateButtons();

    protected abstract void enableButtons(boolean value);

    protected abstract void handleWizard();

    protected void clear() {
        for (WizardPanel panel : wizardInstances) {
            panel.clear();
        }

        wizardInstances.clear();
    }

    protected abstract void updateBreadcrumb();

}
