package com.databasepreservation.common.shared.client.common.helper;

import java.util.Collections;
import java.util.Date;

import com.databasepreservation.Constants;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.JSOUtils;
import com.databasepreservation.common.shared.client.tools.PathUtils;
import com.databasepreservation.common.shared.models.Filter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
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
    Button clearButton = new Button();

    reporterButton.addClickHandler((ClickEvent event) -> {
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
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

    clearButton.addClickHandler((ClickEvent event) -> {
      resetReporterPathFile();
      pathLabelReporter.setText(reporterPathFile);
    });

    return buildPanel(messages.reporterDestinationFolder(), pathLabelReporter, messages.reporterTip(), reporterButton, clearButton);
  }

  public FlowPanel udtValidatorPanel() {
    pathLabelUDT = new Label(udtPathFile);
    Button udtButton = new Button();
    Button clearButton = new Button();

    udtButton.addClickHandler((ClickEvent event) -> {
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
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

    clearButton.addClickHandler((ClickEvent event) -> {
      udtPathFile = "";
      pathLabelUDT.setText(udtPathFile);
    });


    return buildPanel(messages.allowedTypes(), pathLabelUDT, messages.allowedTypesTip(), udtButton, clearButton);
  }

  private FlowPanel buildPanel(String labelTxt, Label pathLabel, String tipTxt, Button btn, Button clear) {
    FlowPanel panel = new FlowPanel();
    FlowPanel inputPanel = new FlowPanel();
    FlowPanel browsePanel = new FlowPanel();
    Label label = new Label(labelTxt);
    Label tip = new Label(tipTxt);
    tip.addStyleName("form-text-helper text-muted");

    btn.setText(messages.basicActionBrowse());
    btn.addStyleName("btn btn-link-info btn-validator");

    clear.setHTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.ACTION_DELETE)));
    clear.addStyleName("btn btn-link-info btn-validator");

    pathLabel.addStyleName("text-muted");

    browsePanel.add(btn);
    browsePanel.add(pathLabel);
    browsePanel.addStyleName("validator-dialog-button");
    inputPanel.add(browsePanel);
    inputPanel.add(clear);

    inputPanel.addStyleName("validator-dialog-information");

    label.addStyleName("form-label-spaced");
    panel.add(label);
    panel.add(inputPanel);
    panel.add(tip);

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
      + Constants.DBPTK_VALIDATION_REPORTER_PREFIX + "-"
      + PathUtils.getFileName(SIARDPath).replaceFirst("[.][^.]+$", "") + "-"
      + DateTimeFormat.getFormat("yyyyMMddHHmmssSSS").format(new Date()) + ViewerConstants.TXT_SUFFIX;
  }
}
