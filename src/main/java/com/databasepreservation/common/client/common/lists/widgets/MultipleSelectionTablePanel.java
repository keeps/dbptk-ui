/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists.widgets;

import java.util.Iterator;
import java.util.List;

import com.databasepreservation.common.client.widgets.MyCellTableResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 * @param <C>
 *          Type for the column, used by inner class ColumnInfo as Column<C,
 *          SafeHtml>
 */
public class MultipleSelectionTablePanel<C> extends Composite {
  private static final String MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_LEFT = "my-asyncdatagrid-display-scroll-wrapper-left";
  private static final String MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_RIGHT = "my-asyncdatagrid-display-scroll-wrapper-right";

  interface MultipleSelectionTablePanelUiBinder extends UiBinder<Widget, MultipleSelectionTablePanel> {
  }

  private static MultipleSelectionTablePanelUiBinder uiBinder = GWT.create(MultipleSelectionTablePanelUiBinder.class);

  private final MultiSelectionModel<C> selectionModel;
  private ListDataProvider<C> dataProvider = new ListDataProvider<>();
  private CellTable<C> display;
  private ScrollPanel displayScroll;
  private SimplePanel displayScrollWrapper;
  private String height;
  private CellTable.Resources resources = null;

  public static class ColumnInfo<C> {
    private Column<C, ?> column;
    private double widthEM;
    private boolean hide;
    private SafeHtml header;

    public ColumnInfo(SafeHtml header, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this.header = header;
      this.widthEM = widthEM;
      this.column = column;
      this.hide = false;
      for (String addCellStyleName : addCellStyleNames) {
        this.column.setCellStyleNames(addCellStyleName);
      }
    }

    public ColumnInfo(SafeHtml header, boolean hide, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this.header = header;
      this.widthEM = widthEM;
      this.column = column;
      this.hide = hide;
      for (String addCellStyleName : addCellStyleNames) {
        this.column.setCellStyleNames(addCellStyleName);
      }
    }

    public ColumnInfo(String header, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this(SafeHtmlUtils.fromString(header), widthEM, column, addCellStyleNames);
    }

    public ColumnInfo(String header, boolean hide, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this(SafeHtmlUtils.fromString(header), hide, widthEM, column, addCellStyleNames);
    }
  }

  @UiField
  SimplePanel info;
  @UiField
  SimplePanel table;

  public MultipleSelectionTablePanel() {
    initWidget(uiBinder.createAndBindUi(this));

    table.setVisible(false);
    selectionModel = new MultiSelectionModel<>();
    this.height = "";

  }

  public MultipleSelectionTablePanel(CellTable.Resources resources) {
    initWidget(uiBinder.createAndBindUi(this));

    table.setVisible(false);
    selectionModel = new MultiSelectionModel<>();
    this.height = "";
    this.resources = resources;

  }

  @Override
  public void setHeight(String height) {
    this.height = height;
  }

  public CellTable<C> getDisplay() {
    return display;
  }

  @SafeVarargs
  public final void createTable(Widget infoContent, List<Integer> whitelistedColumns, Iterator<C> rowItems,
    ColumnInfo<C>... columns) {
    createTable(null, infoContent, whitelistedColumns, rowItems, columns);
  }

  @SafeVarargs
  public final void createTable(Widget headerContent, SafeHtml infoContent, List<Integer> whitelistedColumns,
    Iterator<C> rowItems, ColumnInfo<C>... columns) {
    createTable(headerContent, new HTMLPanel(infoContent), whitelistedColumns, rowItems, columns);
  }

  @SafeVarargs
  public final void createTable(Widget headerContent, Widget infoContent, List<Integer> whitelistedColumns,
    Iterator<C> rowItems, ColumnInfo<C>... columns) {
    // set widgets
    // if (headerContent != null)
    // header.setWidget(headerContent);
    info.setWidget(infoContent);

    display = internalCreateTable(rowItems, columns);

    display.addCellPreviewHandler(event -> {
      if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())
        && (whitelistedColumns.isEmpty() || whitelistedColumns.contains(event.getColumn() + 1))) {
        handleRowSelection(event);
      }
    });

    final CellPreviewEvent.Handler<C> selectionEventManager = DefaultSelectionEventManager.createCheckboxManager(0);
    display.setSelectionModel(getSelectionModel(), selectionEventManager);

    displayScroll = new ScrollPanel(display);
    displayScrollWrapper = new SimplePanel(displayScroll);
    displayScrollWrapper.addStyleName("my-asyncdatagrid-display-scroll-wrapper");
    displayScroll.setSize("100%", height);
    table.setWidget(displayScrollWrapper);

    displayScroll.addScrollHandler(event -> handleScrollChanges());
    handleScrollChanges();
    table.setVisible(true);
  }

  private void handleRowSelection(CellPreviewEvent<C> event) {
    final C value = event.getValue();
    final boolean state = !event.getDisplay().getSelectionModel().isSelected(value);
    event.getDisplay().getSelectionModel().setSelected(value, state);
    event.setCanceled(true);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    handleScrollChanges();
  }

  public void handleScrollChanges() {
    if (displayScroll.getMaximumHorizontalScrollPosition() > 0) {
      double percent = displayScroll.getHorizontalScrollPosition() * 100F
        / displayScroll.getMaximumHorizontalScrollPosition();

      com.google.gwt.core.shared.GWT.log(String.valueOf(percent));

      if (percent > 0) {
        // show left shadow
        displayScrollWrapper.addStyleName(MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_LEFT);
      } else {
        // hide left shadow
        displayScrollWrapper.removeStyleName(MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_LEFT);
      }

      if (percent < 100) {
        // show right shadow
        displayScrollWrapper.addStyleName(MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_RIGHT);
      } else {
        // hide right shadow
        displayScrollWrapper.removeStyleName(MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_RIGHT);
      }
    } else {
      // hide both shadows
      displayScrollWrapper.removeStyleName(MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_LEFT);
      displayScrollWrapper.removeStyleName(MY_ASYNCDATAGRID_DISPLAY_SCROLL_WRAPPER_RIGHT);
    }
  }

  @SafeVarargs
  private final CellTable<C> internalCreateTable(Iterator<C> rowItems, ColumnInfo<C>... columns) {
    // create table
    CellTable<C> cellTable;
    if (resources != null) {
      cellTable = new CellTable<>(Integer.MAX_VALUE, resources);
    } else {
      cellTable = new CellTable<>(Integer.MAX_VALUE, (MyCellTableResources) GWT.create(MyCellTableResources.class));
    }
    cellTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    cellTable.setLoadingIndicator(new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>")));
    cellTable.addStyleName("table-info my-asyncdatagrid-display");

    // add columns
    for (ColumnInfo<C> column : columns) {
      if (!column.hide) {
        cellTable.addColumn(column.column, column.header);
        if (column.widthEM > 0) {
          cellTable.setColumnWidth(column.column, column.widthEM, Style.Unit.EM);
        }
      }
    }

    // fetch rows
    dataProvider.addDataDisplay(cellTable);
    List<C> list = dataProvider.getList();
    while (rowItems.hasNext()) {
      C rowItem = rowItems.next();
      list.add(rowItem);
    }

    return cellTable;
  }

  public MultiSelectionModel<C> getSelectionModel() {
    return selectionModel;
  }
}
