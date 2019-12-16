package com.databasepreservation.common.client.common.visualization.browse.table;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.models.structure.ViewerCheckConstraint;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
public class TableCheckConstraintsPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, TableCheckConstraintsPanel> instances = new HashMap<>();

  public static TableCheckConstraintsPanel getInstance(ViewerTable table) {
    TableCheckConstraintsPanel instance = instances.get(table.getUUID());
    if (instance == null) {
      instance = new TableCheckConstraintsPanel(table);
      instances.put(table.getUUID(), instance);
    }
    return instance;
  }

  interface SchemaCheckConstraintsPanelUiBinder extends UiBinder<Widget, TableCheckConstraintsPanel> {
  }

  private static SchemaCheckConstraintsPanelUiBinder uiBinder = GWT.create(SchemaCheckConstraintsPanelUiBinder.class);

  private ViewerTable table;

  @UiField
  FlowPanel contentItems;

  private TableCheckConstraintsPanel(ViewerTable table) {
    this.table = table;

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    contentItems.add(getBasicTablePanelForTableCheckConstraints());
  }

  private BasicTablePanel<ViewerCheckConstraint> getBasicTablePanelForTableCheckConstraints() {
    Label header = new Label(messages.menusidebar_checkConstraints());
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");

    return new BasicTablePanel<ViewerCheckConstraint>(header, info, table.getCheckConstraints().iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerCheckConstraint>() {
        @Override
        public String getValue(ViewerCheckConstraint viewerRoutineParameter) {
          return viewerRoutineParameter.getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerCheckConstraint>() {
        @Override
        public String getValue(ViewerCheckConstraint viewerRoutineParameter) {
          if (ViewerStringUtils.isNotBlank(viewerRoutineParameter.getDescription())) {
            return viewerRoutineParameter.getDescription();
          } else {
            return "";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.constraints_condition(), 15, new TextColumn<ViewerCheckConstraint>() {
        @Override
        public String getValue(ViewerCheckConstraint viewerRoutineParameter) {
          if (ViewerStringUtils.isNotBlank(viewerRoutineParameter.getCondition())) {
            return viewerRoutineParameter.getCondition();
          } else {
            return "";
          }
        }
      }));
  }
}
