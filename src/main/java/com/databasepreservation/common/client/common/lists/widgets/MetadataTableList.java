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
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataTableList<C> extends Composite {
  interface MetadataEditTableUiBinder extends UiBinder<Widget, MetadataTableList> {
  }

  private static MetadataEditTableUiBinder uiBinder = GWT.create(MetadataEditTableUiBinder.class);

  CellTable<C> cellTable;
  ListDataProvider<C> dataProvider;
  CellTable<C> display;

  private final SingleSelectionModel<C> selectionModel;
  private ScrollPanel displayScroll;
  private SimplePanel displayScrollWrapper;

  @UiField
  SimplePanel info;

  @UiField
  SimplePanel table;

  @SafeVarargs
  public MetadataTableList(Iterator<C> rowItems, ColumnInfo<C>... columns) {
    initWidget(uiBinder.createAndBindUi(this));

    display = createTable(rowItems, columns);
    selectionModel = new SingleSelectionModel<>();
    display.setSelectionModel(selectionModel);

    displayScroll = new ScrollPanel(display);
    displayScroll.setSize("100%", "100%");
    displayScrollWrapper = new SimplePanel(displayScroll);
    displayScrollWrapper.addStyleName("metadata-edit-scroll-wrapper metadata-table-list");
    table.setWidget(displayScrollWrapper);

  }

  public MetadataTableList(String infoContent) {
    initWidget(uiBinder.createAndBindUi(this));

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

  public List getData() {
    return this.dataProvider.getList();
  }



  private final CellTable<C> createTable(Iterator<C> rowItems, ColumnInfo<C>... columns) {

    cellTable = new CellTable<>(Integer.MAX_VALUE, (MyCellTableResources) GWT.create(MyCellTableResources.class));

    cellTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    cellTable.setLoadingIndicator(new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>")));

    cellTable.addStyleName("table-info my-asyncdatagrid-display");

    // add columns
    for (ColumnInfo<C> column : columns) {
      cellTable.addColumn(column.column, column.header);
      cellTable.setColumnWidth(column.column, column.widthEM, Style.Unit.EM);
    }

    // fetch rows
    dataProvider = new ListDataProvider<C>();
    dataProvider.addDataDisplay(cellTable);
    List<C> list = dataProvider.getList();
    if (rowItems != null) {
      while (rowItems.hasNext()) {
        C rowItem = rowItems.next();
        list.add(rowItem);
      }
    }

    return cellTable;
  }

  public static class ColumnInfo<C> {
    private Column<C, ?> column;
    private double widthEM;
    private SafeHtml header;

    public ColumnInfo(SafeHtml header, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this.header = header;
      this.widthEM = widthEM;
      this.column = column;
      for (String addCellStyleName : addCellStyleNames) {
        this.column.setCellStyleNames(addCellStyleName);
      }
    }

    public ColumnInfo(String header, double widthEM, Column<C, ?> column, String... addCellStyleNames) {
      this(SafeHtmlUtils.fromString(header), widthEM, column, addCellStyleNames);
    }
  }

  public SingleSelectionModel<C> getSelectionModel() {
    return selectionModel;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
  }
}
