package com.databasepreservation.common.client.common.lists;

import java.util.Iterator;
import java.util.List;

import com.databasepreservation.common.client.widgets.MyCellTableResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
  interface MultipleSelectionTablePanelUiBinder extends UiBinder<Widget, MultipleSelectionTablePanel> {
  }

  private static MultipleSelectionTablePanelUiBinder uiBinder = GWT.create(MultipleSelectionTablePanelUiBinder.class);

  private final MultiSelectionModel<C> selectionModel;
  private CellTable<C> display;
  private ScrollPanel displayScroll;
  private SimplePanel displayScrollWrapper;
  private String height;

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
  SimplePanel header;
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

  @Override
  public void setHeight(String height) {
    this.height = height;
  }

  public CellTable<C> getDisplay() {
    return display;
  }

  @SafeVarargs
  public final void createTable(Widget infoContent, Iterator<C> rowItems, ColumnInfo<C>... columns) {
    createTable(null, infoContent, rowItems, columns);
  }

  @SafeVarargs
  public final void createTable(Widget headerContent, SafeHtml infoContent, Iterator<C> rowItems, ColumnInfo<C>... columns) {
    createTable(headerContent, new HTMLPanel(infoContent), rowItems, columns);
  }

  @SafeVarargs
  public final void createTable(Widget headerContent, Widget infoContent, Iterator<C> rowItems, ColumnInfo<C>... columns) {
    // set widgets
    if (headerContent != null)
      header.setWidget(headerContent);
    info.setWidget(infoContent);

    display = internalCreateTable(rowItems, columns);

    display.addCellPreviewHandler(event -> {
      if (event.getColumn() != display.getColumnCount()-1) {
        if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
          final C value = event.getValue();
          final boolean state = !event.getDisplay().getSelectionModel().isSelected(value);
          event.getDisplay().getSelectionModel().setSelected(value, state);
          event.setCanceled(true);
        }
      }
    });

    final CellPreviewEvent.Handler<C> selectionEventManager = DefaultSelectionEventManager.createCheckboxManager();
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

  public MultipleSelectionTablePanel(Widget headerContent, String infoContent) {
    initWidget(uiBinder.createAndBindUi(this));

    // set widgets
    header.setWidget(headerContent);

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
    b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"label\">"));
    b.append(SafeHtmlUtils.fromString(infoContent));
    b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    info.setWidget(new HTMLPanel(b.toSafeHtml()));

    table.setVisible(false);
    selectionModel = null;
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    handleScrollChanges();
  }

  public void handleScrollChanges() {
    GWT.log("maximum: " + displayScroll.getMaximumHorizontalScrollPosition());
    if (displayScroll.getMaximumHorizontalScrollPosition() > 0) {
      double percent = displayScroll.getHorizontalScrollPosition() * 100F
        / displayScroll.getMaximumHorizontalScrollPosition();

      com.google.gwt.core.shared.GWT.log(String.valueOf(percent));

      if (percent > 0) {
        // show left shadow
        displayScrollWrapper.addStyleName("my-asyncdatagrid-display-scroll-wrapper-left");
      } else {
        // hide left shadow
        displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-left");
      }

      if (percent < 100) {
        // show right shadow
        displayScrollWrapper.addStyleName("my-asyncdatagrid-display-scroll-wrapper-right");
      } else {
        // hide right shadow
        displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-right");
      }
    } else {
      // hide both shadows
      displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-left");
      displayScrollWrapper.removeStyleName("my-asyncdatagrid-display-scroll-wrapper-right");
    }
  }

  @SafeVarargs
  private final CellTable<C> internalCreateTable(Iterator<C> rowItems, ColumnInfo<C>... columns) {
    // create table
    CellTable<C> cellTable = new CellTable<>(Integer.MAX_VALUE,
      (MyCellTableResources) GWT.create(MyCellTableResources.class));
    cellTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    cellTable.setLoadingIndicator(new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>")));
    cellTable.addStyleName("table-info my-asyncdatagrid-display");

    // add columns
    for (ColumnInfo<C> column : columns) {
      if (!column.hide) {
        cellTable.addColumn(column.column, column.header);
        cellTable.setColumnWidth(column.column, column.widthEM, Style.Unit.EM);
      }
    }

    // fetch rows
    ListDataProvider<C> dataProvider = new ListDataProvider<>();
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
