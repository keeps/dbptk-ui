package com.databasepreservation.common.client.common.visualization.browse.table;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerTrigger;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableTriggersPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, TableTriggersPanel> instances = new HashMap<>();

  public static TableTriggersPanel getInstance(ViewerTable table) {
    TableTriggersPanel instance = instances.get(table.getUuid());
    if (instance == null) {
      instance = new TableTriggersPanel(table);
      instances.put(table.getUuid(), instance);
    }
    return instance;
  }

  interface SchemaTriggersPanelUiBinder extends UiBinder<Widget, TableTriggersPanel> {
  }

  private static SchemaTriggersPanelUiBinder uiBinder = GWT.create(SchemaTriggersPanelUiBinder.class);

  private ViewerTable table;

  @UiField
  FlowPanel contentItems;

  private TableTriggersPanel(ViewerTable table) {
    this.table = table;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  private void init() {
    contentItems.add(getBasicTablePanelForTableTriggers(table));
  }

  private BasicTablePanel<ViewerTrigger> getBasicTablePanelForTableTriggers(final ViewerTable table) {
    Label header = new Label(messages.menusidebar_triggers());
    header.addStyleName("h5");

    return new BasicTablePanel<ViewerTrigger>(header, SafeHtmlUtils.EMPTY_SAFE_HTML, table.getTriggers().iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          return viewerTrigger.getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          if (ViewerStringUtils.isNotBlank(viewerTrigger.getDescription())) {
            return viewerTrigger.getDescription();
          } else {
            return "";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.triggeredAction(), 25,
        new Column<ViewerTrigger, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ViewerTrigger viewerTrigger) {
            return SafeHtmlUtils.fromSafeConstant(SafeHtmlUtils
              .htmlEscape(viewerTrigger.getTriggeredAction().replace("\\u0020", " ")).replace("\\n", "<br/>"));
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.actionTime(), 8, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          return viewerTrigger.getActionTime();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.triggerEvent(), 8, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          return viewerTrigger.getTriggerEvent();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.aliasList(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger viewerTrigger) {
          if (ViewerStringUtils.isNotBlank(viewerTrigger.getAliasList())) {
            return viewerTrigger.getAliasList();
          } else {
            return "";
          }
        }
      })
    );
  }
}
