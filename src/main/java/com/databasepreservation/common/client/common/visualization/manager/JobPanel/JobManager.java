package com.databasepreservation.common.client.common.visualization.manager.JobPanel;

import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.JobList;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobManager extends ContentPanel {
  private ClientMessages messages = GWT.create(ClientMessages.class);

  interface JobManagerBinder extends UiBinder<Widget, JobManager> {
  }

  private static JobManagerBinder binder = GWT.create(JobManagerBinder.class);

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  FlowPanel header;

  @UiField
  SimplePanel description;

  @UiField(provided = true)
  JobList jobList;

  private static JobManager instance = null;

  public static JobManager getInstance() {
    if (instance == null) {
      instance = new JobManager();
    }
    return instance;
  }

  private JobManager() {
    jobList = new JobList();
    initWidget(binder.createAndBindUi(this));
    jobList.getSelectionModel().addSelectionChangeHandler(selectionChangeEvent -> {
      ViewerJob selected = jobList.getSelectionModel().getSelectedObject();
      if (selected != null) {
        String databaseUuid = selected.getDatabaseUuid();
        String tableId = selected.getTableId();
        HistoryManager.gotoTable(databaseUuid, tableId);
      }
    });

    header.add(CommonClientUtils.getHeaderHTML(
      FontAwesomeIconManager.getTag(FontAwesomeIconManager.NETWORK_WIRED), messages.batchJobsTextForPageTitle(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.batchJobsTextForPageDescription());
    instance.setCSS("table-row-description", "font-size-description");

    description.setWidget(instance);

    searchInputBox.getElement().setPropertyString("placeholder", messages.searchPlaceholder());

    searchInputBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(event -> doSearch());
  }

  private void doSearch() {
    // start searching
    Filter filter;
    String searchText = searchInputBox.getText();
    if (ViewerStringUtils.isBlank(searchText)) {
      filter = ViewerConstants.DEFAULT_FILTER;
    } else {
      filter = new Filter(new BasicSearchFilterParameter(ViewerConstants.INDEX_SEARCH, searchText));
    }

    jobList.setFilter(filter);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    jobList.getSelectionModel().clear();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forJobManager();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }
}
