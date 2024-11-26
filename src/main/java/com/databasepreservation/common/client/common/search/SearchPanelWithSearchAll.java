/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.common.lists.utils.AsyncTableCell;
import com.databasepreservation.common.client.common.search.panel.SearchFieldPanel;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchPanelWithSearchAll extends SearchPanelAbstract {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SearchPanelWithSearchAll> {
  }

  @UiField
  SearchAllButtonWrapper selectedDatabasesButtonWrapper;

  public void initSearchAll() {
    this.selectedDatabasesButtonWrapper.init(defaultFilter, allFilter, messages, this);
  }

  public SearchPanelWithSearchAll(Filter defaultFilter, String allFilter, String placeholder, boolean showSearchInputListBox,
                                  boolean showSearchAdvancedDisclosureButton) {
    super(defaultFilter, allFilter, placeholder, showSearchInputListBox, showSearchAdvancedDisclosureButton);
  }

  public SearchPanelWithSearchAll(Filter defaultFilter, String allFilter, String placeholder, String context,
                                  boolean showSearchAdvancedDisclosureButton, final AsyncCallback<Void> saveQueryCallback) {
    super(defaultFilter, allFilter, placeholder, context, showSearchAdvancedDisclosureButton, saveQueryCallback);
  }

  public SearchPanelWithSearchAll(Filter defaultFilter, String allFilter, String placeholder, boolean showSearchInputListBox,
                                  boolean showSearchAdvancedDisclosureButton, final AsyncCallback<Void> saveQueryCallback) {
    super(defaultFilter, allFilter, placeholder, showSearchInputListBox, showSearchAdvancedDisclosureButton, saveQueryCallback);
  }

  @Override
  public void bindUIAndInitWidget() {
    initWidget(binder.createAndBindUi(this));
  }

  public void setSearchAllTotalDatabases(String searchAllTotalDatabases) {
    this.selectedDatabasesButtonWrapper.setTotalSelectedDatabases(searchAllTotalDatabases);
  }

  public SearchAllButtonWrapper getSearchAllButtonWrapper() {
    return this.selectedDatabasesButtonWrapper;
  }

  public String getTotalSelected() {
    return this.selectedDatabasesButtonWrapper.getTotalSelectedDatabases();
  }
}
