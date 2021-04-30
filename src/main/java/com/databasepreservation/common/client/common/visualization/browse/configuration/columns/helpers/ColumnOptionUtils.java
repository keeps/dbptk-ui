/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ColumnOptionUtils {

  private ColumnOptionUtils() {
    super();
  }

  public static FlowPanel buildHintForLabel(TextBox target, String label) {
    FlowPanel hintPanel = new FlowPanel();
    hintPanel.setStyleName("data-transformation-title");
    hintPanel.add(new Label(label));

    Button btnDownloadLink = new Button(ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK);
    btnDownloadLink.setStyleName("btn btn-primary btn-small");
    btnDownloadLink.addClickHandler(event -> {
      target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE
        + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
    });
    Button btnDownloadLabel = new Button(ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL);
    btnDownloadLabel.setStyleName("btn btn-primary btn-small");
    btnDownloadLabel.addClickHandler(event -> {
      target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE
        + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
    });

    hintPanel.add(btnDownloadLink);
    hintPanel.add(btnDownloadLabel);

    return hintPanel;
  }

  public static String getDefaultTextOrValue(TemplateStatus templateConfig) {
    String template = templateConfig.getTemplate();

    if (ViewerStringUtils.isBlank(template)) {
      template = DEFAULT_DOWNLOAD_LABEL_TEMPLATE;
    }

    return template;
  }

  public static FlowPanel buildHintWithButtons(TableStatus tableConfiguration, TextBox target, String label,
    Boolean isBlob) {
    FlowPanel hintPanel = new FlowPanel();
    hintPanel.setStyleName("data-transformation-title");
    hintPanel.add(new Label(label));

    for (ColumnStatus column : tableConfiguration.getColumns()) {
      Button btnField = new Button(column.getCustomName());
      btnField.setStyleName("btn btn-primary btn-small");
      btnField.addClickHandler(handler -> {
        target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE
          + ViewerStringUtils.replaceAllFor(btnField.getText(), "\\s", "_") + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
      });

      hintPanel.add(btnField);
    }

    /* BLOB Options */
    if (isBlob) {
      Button btnRowIndex = new Button(ViewerConstants.TEMPLATE_LOB_ROW_INDEX);
      btnRowIndex.setStyleName("btn btn-primary btn-small");
      btnRowIndex.addClickHandler(handler -> {
        target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE + btnRowIndex.getText()
          + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
      });
      hintPanel.add(btnRowIndex);

      Button btnColIndex = new Button(ViewerConstants.TEMPLATE_LOB_COLUMN_INDEX);
      btnColIndex.setStyleName("btn btn-primary btn-small");
      btnColIndex.addClickHandler(handler -> {
        target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE + btnColIndex.getText()
          + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
      });
      hintPanel.add(btnColIndex);

      Button btnAutoDetectedFileExtension = new Button(ViewerConstants.TEMPLATE_LOB_AUTO_DETECTED_EXTENSION);
      btnAutoDetectedFileExtension.setStyleName("btn btn-primary btn-small");
      btnAutoDetectedFileExtension.addClickHandler(handler -> {
        target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE + btnAutoDetectedFileExtension.getText()
          + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
      });
      hintPanel.add(btnAutoDetectedFileExtension);
    }

    return hintPanel;
  }
}
