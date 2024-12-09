package com.databasepreservation.common.client.common.search;

import com.databasepreservation.common.client.common.dialogs.DatabaseSelectDialog;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class SearchAllButtonWrapper extends SimplePanel implements HasValueChangeHandlers<String> {
  private ClientMessages messages;
  public String totalSelectedDatabases;

  private Button button;
  private DatabaseSelectDialog databaseSelectDialog;

  public void init(Filter defaultFilter, String allFilter, ClientMessages messages,
    SearchPanelWithSearchAll parentSearchPanel) {
    this.messages = messages;
    this.button = new Button();
    this.button.addStyleName("btn-link-info btn btn-searchall-dialog");
    setTotalSelectedDatabases("0");
    this.databaseSelectDialog = new DatabaseSelectDialog(defaultFilter, allFilter, messages, parentSearchPanel);
    this.button.addClickHandler(event -> {
      this.databaseSelectDialog.center();
      this.databaseSelectDialog.show();
    });
    setWidget(this.button);
  }

  public String getTotalSelectedDatabases() {
    return this.totalSelectedDatabases;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public void setTotalSelectedDatabases(String totalSelectedDatabases) {
    this.totalSelectedDatabases = totalSelectedDatabases;
    this.button
      .setHTML(SafeHtmlUtils.fromSafeConstant(messages.manageDatabaseSearchAllSearchingOn(this.totalSelectedDatabases)
        + FontAwesomeIconManager.getTag("edit")));
    ValueChangeEvent.fire(this, totalSelectedDatabases);
  }

  public SearchAllButtonWrapper() {
    super();
  }
}
