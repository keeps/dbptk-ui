package com.databasepreservation.common.client.common.visualization.manager.JobPanel;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.DatabaseList;
import com.databasepreservation.common.client.common.lists.JobList;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobManager extends ContentPanel {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forManageDatabase();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  interface JobManagerBinder extends UiBinder<Widget, JobManager> {
  }

  private static JobManagerBinder binder = GWT.create(JobManagerBinder.class);

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  SimplePanel mainHeader;

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

    mainHeader
      .setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.COG), "Jobs", "h1"));

    MetadataField instance = MetadataField.createInstance(messages.manageDatabasePageDescription());
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

}
