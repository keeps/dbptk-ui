/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search;

import com.databasepreservation.common.client.index.filter.Filter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchPanel extends SearchPanelAbstract {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SearchPanel> {
  }
  public SearchPanel(Filter defaultFilter, String allFilter, String placeholder, boolean showSearchInputListBox,
    boolean showSearchAdvancedDisclosureButton) {
    super(defaultFilter, allFilter, placeholder, showSearchInputListBox, showSearchAdvancedDisclosureButton);
  }

  public SearchPanel(Filter defaultFilter, String allFilter, String placeholder, String context,
    boolean showSearchAdvancedDisclosureButton, final AsyncCallback<Void> saveQueryCallback) {
    super(defaultFilter, allFilter, placeholder, context, showSearchAdvancedDisclosureButton, saveQueryCallback);
  }

  public SearchPanel(Filter defaultFilter, String allFilter, String placeholder, boolean showSearchInputListBox,
    boolean showSearchAdvancedDisclosureButton, final AsyncCallback<Void> saveQueryCallback) {
    super(defaultFilter, allFilter, placeholder, showSearchInputListBox, showSearchAdvancedDisclosureButton,
      saveQueryCallback);
  }

  public void bindUIAndInitWidget() {
    initWidget(binder.createAndBindUi(this));
  }
}
