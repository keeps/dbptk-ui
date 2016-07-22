package com.databasepreservation.visualization.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.lists.TableRowList;
import com.databasepreservation.visualization.client.common.search.TableSearchPanel;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.databasepreservation.visualization.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.IndexResult;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseSearchPanel extends RightPanel {
  private static Map<String, DatabaseSearchPanel> instances = new HashMap<>();

  public static DatabaseSearchPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseSearchPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseSearchPanel(database);
      instances.put(code, instance);
    }

    return instance;
  }

  interface TablePanelUiBinder extends UiBinder<Widget, DatabaseSearchPanel> {
  }

  @UiField
  HTML mainHeader;

  @UiField
  FlowPanel content;

  @UiField
  HTML description;

  @UiField
  TextBox searchInputBox;

  @UiField
  FlowPanel searchPanel;

  @UiField
  AccessibleFocusPanel searchInputButton;

  List<TableSearchPanelContainer> tableSearchPanelContainers;

  private ViewerDatabase database;

  private static TablePanelUiBinder uiBinder = GWT.create(TablePanelUiBinder.class);

  private DatabaseSearchPanel(ViewerDatabase database) {
    tableSearchPanelContainers = new ArrayList<>();

    initWidget(uiBinder.createAndBindUi(this));

    for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
      for (ViewerTable viewerTable : viewerSchema.getTables()) {
        TableSearchPanelContainer tableSearchPanelContainer = new TableSearchPanelContainer(database, viewerTable);
        tableSearchPanelContainers.add(tableSearchPanelContainer);
        content.add(tableSearchPanelContainer);
      }
    }

    this.database = database;

    searchInputBox.getElement().setPropertyString("placeholder", "Search...");

    searchInputBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          doSearch();
        }
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));
  }

  private void doSearch() {
    // hide everything
    for (TableSearchPanelContainer tableSearchPanelContainer : tableSearchPanelContainers) {
      tableSearchPanelContainer.setVisible(false);
    }

    // start searching
    Filter filter;
    String searchText = searchInputBox.getText();
    if(ViewerStringUtils.isBlank(searchText)){
      filter = ViewerSafeConstants.DEFAULT_FILTER;
    }else{
      filter = new Filter(new BasicSearchFilterParameter(ViewerSafeConstants.SOLR_ROW_SEARCH, searchText));
    }

    for (TableSearchPanelContainer tableSearchPanelContainer : tableSearchPanelContainers) {
      tableSearchPanelContainer.doSearch(filter);
    }
  }

  private static class TableSearchPanelContainer extends FlowPanel {
    private TableRowList tableRowList;
    private final ViewerDatabase database;
    private final ViewerTable table;

    public TableSearchPanelContainer(ViewerDatabase database, ViewerTable table){
      super();
      this.setVisible(false);
      this.database = database;
      this.table = table;
    }

    public void init(Filter filter){
      if(filter == null){
        filter = ViewerSafeConstants.DEFAULT_FILTER;
      }

      tableRowList = new TableRowList(database, table, filter, null, null, false, false);

      HTML header = new HTML(new SafeHtmlBuilder().append(
        FontAwesomeIconManager.loaded(FontAwesomeIconManager.SCHEMA, table.getSchemaName())).appendHtmlConstant("<br/>").append(
        FontAwesomeIconManager.loaded(FontAwesomeIconManager.TABLE, table.getName())).toSafeHtml());
      header.addStyleName("h3");

      add(header);
      add(tableRowList);

      tableRowList.addValueChangeHandler(new ValueChangeHandler<IndexResult<ViewerRow>>() {
        @Override public void onValueChange(ValueChangeEvent<IndexResult<ViewerRow>> event) {
          searchCompletedEventHandler(event);
        }
      });
    }

    private void searchCompletedEventHandler(ValueChangeEvent<IndexResult<ViewerRow>> event){
      long resultCount = event.getValue().getTotalCount();
      GWT.log(table.getName() + " got " + resultCount + " results");
      setVisible(resultCount > 0);
    }

    public void doSearch(Filter filter) {
      if(tableRowList == null){
        init(filter);
      }else{
        tableRowList.setFilter(filter);
      }
    }
  }
}
