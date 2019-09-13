package com.databasepreservation.main.desktop.client.common.helper;

import java.util.Collections;
import java.util.Date;

import com.databasepreservation.Constants;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.common.shared.client.tools.PathUtils;
import com.databasepreservation.main.desktop.shared.models.Filter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class HelperValidator {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private String reporterPathFile;
  private String udtPathFile;
  private String SIARDPath;
  private Label pathLabelReporter;
  private Label pathLabelUDT;

  public HelperValidator(String SIARDPath) {
    this.SIARDPath = SIARDPath;
  }

  public String getReporterPathFile() {
    return reporterPathFile;
  }

  public String getUdtPathFile() {
    return udtPathFile;
  }

  public FlowPanel reporterValidatorPanel() {
    if (reporterPathFile == null) {
      resetReporterPathFile();
    }
    pathLabelReporter = new Label(reporterPathFile);

    Button reporterButton = new Button();
    reporterButton.addClickHandler((ClickEvent event) -> {
      if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
        JavaScriptObject.createArray();
        Filter filter = new Filter();
        filter.setName(ViewerConstants.REPORT_FILES);
        filter.setExtensions(Collections.singletonList("txt"));
        JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.emptyList(),
          Collections.singletonList(filter));

        String path = JavascriptUtils.saveFileDialog(options);
        if (path != null && !path.isEmpty()) {
          reporterPathFile = path;
          pathLabelReporter.setText(reporterPathFile);
        }
      }
    });

    return buildPanel(messages.ReporterDestinationFolder(), pathLabelReporter, messages.ReporterTip(), reporterButton);
  }

  public FlowPanel udtValidatorPanel() {
    pathLabelUDT = new Label(udtPathFile);
    Button udtButton = new Button();

    udtButton.addClickHandler((ClickEvent event) -> {
      if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
        Filter txt = new Filter(Constants.UDT, Collections.singletonList("txt"));

        JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openFile"),
          Collections.singletonList(txt));

        String path = JavascriptUtils.openFileDialog(options);
        if (path != null && !path.isEmpty()) {
          udtPathFile = path;
          pathLabelUDT.setText(udtPathFile);
        }
      }
    });

    return buildPanel(messages.AllowedTypes(), pathLabelUDT, messages.AllowedTypesTip(), udtButton);
  }

  private FlowPanel buildPanel(String labelTxt, Label pathLabel, String tipTxt, Button btn) {
    FlowPanel panel = new FlowPanel();
    FlowPanel labelPanel = new FlowPanel();
    FlowPanel btnPanel = new FlowPanel();
    Label label = new Label(labelTxt);
    HTML tip = new HTML(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.QUESTION, tipTxt)));

    btn.setText(messages.siardExportBrowseButton());
    btn.addStyleName("btn btn-link-info btn-browser-validator");
    tip.addStyleName("tip-validator");
    btnPanel.add(btn);
    btnPanel.add(tip);
    btnPanel.addStyleName("validator-dialog-button");

    label.addStyleName("form-label-spaced");
    labelPanel.add(label);
    labelPanel.add(pathLabel);
    panel.add(labelPanel);
    panel.add(btnPanel);
    panel.addStyleName("form-row validator-dialog-information");

    return panel;
  }

  public void clear() {
    resetReporterPathFile();
    udtPathFile = "";
    pathLabelUDT.setText(udtPathFile);
    pathLabelReporter.setText(reporterPathFile);
  }

  private void resetReporterPathFile() {
    reporterPathFile = SIARDPath.replace(PathUtils.getFileName(SIARDPath), "")
      + Constants.DBPTK_VALIDATION_REPORTER_PREFIX + "-" + DateTimeFormat.getFormat("yyyyMMddHHmmssSSS").format(new Date())
      + ViewerConstants.TXT_SUFFIX;
  }
}
