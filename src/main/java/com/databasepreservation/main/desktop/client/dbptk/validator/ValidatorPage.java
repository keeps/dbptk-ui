package com.databasepreservation.main.desktop.client.dbptk.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ValidatorPage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, ValidatorPage> instances = new HashMap<>();
  private String databaseUUID = null;
  private String SIARDPath = null;
  private String reporterPath = null;
  private String udtPath = null;

  interface ValidatorUiBinder extends UiBinder<Widget, ValidatorPage> {
  }

  private static ValidatorUiBinder binder = GWT.create(ValidatorUiBinder.class);

  public static ValidatorPage getInstance(String databaseUUID, String reporterPath) {
    return ValidatorPage.getInstance(databaseUUID, reporterPath, null);
  }

  public static ValidatorPage getInstance(String databaseUUID, String reporterPath, String udtPath) {
    String codeId = databaseUUID + ViewerConstants.API_SEP + reporterPath + ViewerConstants.API_SEP + udtPath;
    if (instances.get(codeId) == null) {
      ValidatorPage instance = new ValidatorPage(databaseUUID, reporterPath, udtPath);
      instances.put(codeId, instance);
    }

    return instances.get(codeId);
  }

  @UiField
  FlowPanel container, validatorInformation;

  @UiField
  BreadcrumbPanel breadcrumb;

  private ValidatorPage(String databaseUUID, String reporterPath, String udtPath) {
    this.databaseUUID = databaseUUID;
    this.reporterPath = URL.decodePathSegment(reporterPath);
    if (udtPath != null) {
      this.udtPath = URL.decodePathSegment(udtPath);
    }
    initWidget(binder.createAndBindUi(this));

    final Widget loading = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div id='loading' class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    container.add(loading);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          ViewerDatabase database = (ViewerDatabase) result;

          List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDValidatorPage(databaseUUID,
            database.getMetadata().getName());
          BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
          container.remove(loading);

          SIARDPath = database.getSIARDPath();

          runValidator();
        }
      });
  }

  private void runValidator() {
    SimplePanel database = new SimplePanel();
    SimplePanel siard = new SimplePanel();
    SimplePanel reporter = new SimplePanel();
    SimplePanel udt = new SimplePanel();

    Label databaseLb = new Label("Database UUID:" + databaseUUID);
    Label siardLb = new Label("SIARD Path:" + SIARDPath);
    Label reporterLb = new Label("Reporter Path:" + reporterPath);
    Label udtLb = new Label("UDT Path:" + udtPath);

    database.add(databaseLb);
    siard.add(siardLb);
    reporter.add(reporterLb);
    udt.add(udtLb);

    validatorInformation.add(database);
    validatorInformation.add(siard);
    validatorInformation.add(reporter);
    if (udtPath != null) {
      validatorInformation.add(udt);
    }
  }

  public void clear(String uuid) {

  }

}
