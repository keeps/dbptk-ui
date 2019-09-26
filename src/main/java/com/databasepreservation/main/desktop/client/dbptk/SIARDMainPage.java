package com.databasepreservation.main.desktop.client.dbptk;

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
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.Humanize;
import com.databasepreservation.main.common.shared.client.tools.PathUtils;
import com.databasepreservation.main.common.shared.client.tools.SolrHumanizer;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.desktop.client.common.MetadataField;
import com.databasepreservation.main.desktop.client.common.NavigationPanel;
import com.databasepreservation.main.desktop.client.common.dialogs.Dialogs;
import com.databasepreservation.main.desktop.client.common.helper.HelperValidator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDMainPage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDInfoUiBinder extends UiBinder<Widget, SIARDMainPage> {
  }

  private static SIARDInfoUiBinder binder = GWT.create(SIARDInfoUiBinder.class);
  private static Map<String, SIARDMainPage> instances = new HashMap<>();
  private ViewerDatabase database = null;
  private String validateAtHumanized = null;
  private String archivalDateHumanized = null;
  private MetadataField validatedAt = null;
  private MetadataField version = null;
  private MetadataField validationStatus = null;
  private FlowPanel validationIndicators = new FlowPanel();
  private MetadataField browsingStatus = null;
  private Button btnSeeReport, btnBrowse, btnDelete, btnIngest, btnOpenValidator, btnRunValidator;

  public static SIARDMainPage getInstance(String databaseUUID) {

    if (instances.get(databaseUUID) == null) {
      SIARDMainPage instance = new SIARDMainPage(databaseUUID);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  @UiField
  FlowPanel container, panel, metadataInformation, navigationPanels;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel description;

  private SIARDMainPage(final String databaseUUID) {

    initWidget(binder.createAndBindUi(this));

    final Widget loading = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div id='loading' class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    container.add(loading);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          BrowserService.Util.getInstance().getDateTimeHumanized(database.getValidatedAt(),
            new DefaultAsyncCallback<String>() {
              @Override
              public void onSuccess(String result) {
                validateAtHumanized = result;
                BrowserService.Util.getInstance().getDateTimeHumanized(database.getMetadata().getArchivalDate(),
                  new DefaultAsyncCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                      archivalDateHumanized = result;
                      populateMetadataInfo();
                      populateDescription();
                      populateNavigationPanels();

                      List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDMainPage(databaseUUID,
                        database.getMetadata().getName());
                      BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

                      container.remove(loading);
                    }
                  });
              }
            });
        }
      });
  }

  private NavigationPanel populateNavigationPanelSIARD() {
    /* SIARD */
    Button btnEditMetadata = new Button();
    btnEditMetadata.setText(messages.SIARDHomePageButtonTextEditMetadata());
    btnEditMetadata.addStyleName("btn btn-link-info");
    btnEditMetadata.addClickHandler(clickEvent -> {
      HistoryManager.gotoSIARDEditMetadata(database.getUUID());
    });

    Button btnMigrateToSIARD = new Button();
    btnMigrateToSIARD.setText(messages.SIARDHomePageButtonTextMigrateToSIARD());
    btnMigrateToSIARD.addStyleName("btn btn-link-info");

    btnMigrateToSIARD.addClickHandler(event -> {
      HistoryManager.gotoMigrateSIARD(database.getUUID(), database.getMetadata().getName());
    });

    Button btnSendToLiveDBMS = new Button();
    btnSendToLiveDBMS.setText(messages.SIARDHomePageButtonTextSendToLiveDBMS());
    btnSendToLiveDBMS.addStyleName("btn btn-link-info");

    btnSendToLiveDBMS.addClickHandler(event -> {
      HistoryManager.gotoSendToLiveDBMSExportFormat(database.getUUID(), database.getMetadata().getName());
    });

    MetadataField path = MetadataField.createInstance(PathUtils.getFileName(database.getSIARDPath()));
    MetadataField size = MetadataField.createInstance(Humanize.readableFileSize(database.getSIARDSize()));

    Button btnShowFiles = new Button(messages.SIARDHomePageButtonTextShowFile());
    btnShowFiles.addStyleName("btn btn-link-info");

    if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
      btnShowFiles.addClickHandler(clickEvent -> {
        JavascriptUtils.showItemInFolder(database.getSIARDPath());
      });
    }

    NavigationPanel siard = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForSIARD());

    siard.addButton(btnEditMetadata);
    siard.addButton(btnMigrateToSIARD);
    siard.addButton(btnSendToLiveDBMS);

    siard.addToInfoPanel(path);
    siard.addToInfoPanel(btnShowFiles);
    siard.addToInfoPanel(size);

    return siard;
  }

  private NavigationPanel populateNavigationPanelValidation() {
    /* Validation */
    NavigationPanel validation = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForValidation());

    HelperValidator validator = new HelperValidator(database.getSIARDPath());
    btnRunValidator = new Button();
    btnRunValidator.setText(messages.SIARDHomePageButtonTextValidateNow());
    btnRunValidator.addStyleName("btn btn-link-info");
    btnRunValidator.addClickHandler(event -> {
      Dialogs.showValidatorSettings(messages.SIARDValidatorSettings(), messages.basicActionCancel(),
        messages.basicActionConfirm(), validator, new DefaultAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            if (result && validator.getReporterPathFile() != null) {
              if (validator.getUdtPathFile() == null) {
                HistoryManager.gotoSIARDValidator(database.getUUID(), validator.getReporterPathFile());
              } else {
                HistoryManager.gotoSIARDValidator(database.getUUID(), validator.getReporterPathFile(),
                  validator.getUdtPathFile());
              }
            }
          }
        });
    });

    validation.addButton(btnRunValidator);

    btnOpenValidator = new Button();
    btnOpenValidator.setVisible(false);
    btnOpenValidator.setText(messages.SIARDHomePageButtonTextOpenValidate());
    btnOpenValidator.addStyleName("btn btn-link-info");
    btnOpenValidator.addClickHandler(event -> {
      HistoryManager.gotoSIARDValidator(database.getUUID(), validator.getReporterPathFile());
    });

    validation.addButton(btnOpenValidator);

    btnSeeReport = new Button();
    btnSeeReport.setText(messages.SIARDHomePageButtonTextSeeReport());
    btnSeeReport.addStyleName("btn btn-link-info");
    if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
      btnSeeReport.addClickHandler(clickEvent -> {
        JavascriptUtils.showItem(database.getValidatorReportPath());
      });
    }
    validation.addButton(btnSeeReport);

    if (database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.NOT_VALIDATED)) {
      btnSeeReport.setVisible(false);
      btnRunValidator.setText(messages.SIARDHomePageButtonTextValidateNow());
    } else {
      btnSeeReport.setVisible(true);
      btnRunValidator.setText(messages.SIARDHomePageButtonTextRunValidationAgain());
    }

    if (!database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.NOT_VALIDATED)) {
      validatedAt = MetadataField.createInstance(messages.SIARDHomePageLabelForValidatedAt(), validateAtHumanized);
      version = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationVersion(),
        database.getValidatedVersion());
      validatedAt.setVisible(true);
      version.setVisible(true);
      validationStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationStatus(),
        SolrHumanizer.humanize(database.getValidationStatus()));
      if (database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_SUCCESS)) {
        validationStatus.getMetadataValue().addStyleName("label-success");
        validationIndicators.setVisible(true);
      } else if (database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_FAILED)) {
        validationStatus.getMetadataValue().addStyleName("label-danger");
        validationIndicators.setVisible(true);
      } else {
        validationStatus.getMetadataValue().addStyleName("label-info");
      }
    } else {
      validatedAt = MetadataField.createInstance(messages.SIARDHomePageLabelForValidatedAt(),
        messages.humanizedTextForSIARDNotValidated());
      version = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationVersion(),
        messages.humanizedTextForSIARDNotValidated());
      validationStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationStatus(),
        messages.humanizedTextForSIARDNotValidated());
      validationStatus.getMetadataValue().addStyleName("label-info");
      validationIndicators.setVisible(false);
      validatedAt.setVisible(false);
      version.setVisible(false);
    }

    validatedAt.setCSSMetadata(null, "label-field", "value-field");
    version.setCSSMetadata(null, "label-field", "value-field");
    validationStatus.setCSSMetadata(null, "label-field", "value-field");

    // indicators
    updateValidationIndicators();

    validation.addToInfoPanel(validationStatus);
    validation.addToInfoPanel(validationIndicators);
    validation.addToInfoPanel(validatedAt);
    validation.addToInfoPanel(version);

    return validation;
  }

  private NavigationPanel populateNavigationPanelBrowse() {
    NavigationPanel browse = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForBrowsing());

    btnBrowse = new Button();
    btnBrowse.setText(messages.SIARDHomePageButtonTextForBrowseNow());
    btnBrowse.addStyleName("btn btn-link-info");
    btnBrowse.setVisible(false);

    btnBrowse.addClickHandler(event -> {
      HistoryManager.gotoDesktopDatabase(database.getUUID());
    });

    btnDelete = new Button();
    btnDelete.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
    btnDelete.addStyleName("btn btn-link-info");
    btnDelete.setVisible(false);

    btnIngest = new Button();
    btnIngest.setText(messages.SIARDHomePageButtonTextForIngest());
    btnIngest.addStyleName("btn btn-link-info");
    btnIngest.setVisible(false);

    btnIngest.addClickHandler(event -> {
      HistoryManager.gotoIngestSIARDData(database.getUUID(), database.getMetadata().getName());
      BrowserService.Util.getInstance().uploadSIARD(database.getSIARDPath(), database.getUUID(),
        new DefaultAsyncCallback<String>() {
          @Override
          public void onFailure(Throwable caught) {
            instances.clear();
            HistoryManager.gotoSIARDInfo(database.getUUID());
          }

          @Override
          public void onSuccess(String databaseUUID) {
            Dialogs.showInformationDialog("test", "sdfsd", messages.basicActionBrowse(), "btn btn-link", new DefaultAsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                GWT.log("OK");
              }
            });
            //HistoryManager.gotoDesktopDatabase(databaseUUID);
          }
        });
    });

    browse.addButton(btnIngest);
    browse.addButton(btnBrowse);
    browse.addButton(btnDelete);

    if (database.getStatus() == ViewerDatabase.Status.AVAILABLE) {
      btnBrowse.setVisible(true);
      // btnDelete.setVisible(true);
    } else if (database.getStatus() == ViewerDatabase.Status.METADATA_ONLY) {
      btnIngest.setVisible(true);
    }

    browsingStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForBrowseStatus(),
      SolrHumanizer.humanize(database.getStatus()));
    browsingStatus.setCSSMetadata(null, "label-field", "value-field");

    browse.addToInfoPanel(browsingStatus);

    return browse;
  }

  private void populateNavigationPanels() {
    navigationPanels.add(populateNavigationPanelSIARD());
    navigationPanels.add(populateNavigationPanelValidation());
    navigationPanels.add(populateNavigationPanelBrowse());
  }

  private void populateDescription() {
    Label label = new Label();

    String descriptionTxt = database.getMetadata().getDescription();

    if (ViewerStringUtils.isBlank(descriptionTxt) || descriptionTxt.contentEquals("unspecified")) {
      label.setText(messages.SIARDHomePageTextForMissingDescription());
    } else {
      label.setText(descriptionTxt);
    }

    description.add(label);
  }

  private void populateMetadataInfo() {

    FlowPanel left = new FlowPanel();
    left.addStyleName("metadata-information");
    FlowPanel right = new FlowPanel();
    right.addStyleName("metadata-information");

    MetadataField dbname = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataName(),
      database.getMetadata().getName());
    dbname.setCSSMetadata("metadata-field", "metadata-information-element-label", "metadata-information-element-value");
    MetadataField archivalDate = MetadataField
      .createInstance(messages.SIARDHomePageLabelForViewerMetadataArchivalDate(), archivalDateHumanized);
    archivalDate.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    MetadataField archiver = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataArchiver(),
      database.getMetadata().getArchiver());
    archiver.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    MetadataField archiverContact = MetadataField.createInstance(
      messages.SIARDHomePageLabelForViewerMetadataArchiverContact(), database.getMetadata().getArchiverContact());
    archiverContact.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    MetadataField clientMachine = MetadataField.createInstance(
      messages.SIARDHomePageLabelForViewerMetadataClientMachine(), database.getMetadata().getClientMachine());
    clientMachine.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");

    left.add(dbname);
    left.add(archivalDate);
    left.add(archiver);
    left.add(archiverContact);
    left.add(clientMachine);

    MetadataField databaseProduct = MetadataField.createInstance(
      messages.SIARDHomePageLabelForViewerMetadataDatabaseProduct(), database.getMetadata().getDatabaseProduct());
    databaseProduct.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    MetadataField dataOriginTimespan = MetadataField.createInstance(
      messages.SIARDHomePageLabelForViewerMetadataDataOriginTimespan(), database.getMetadata().getDataOriginTimespan());
    dataOriginTimespan.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    MetadataField dataOwner = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDataOwner(),
      database.getMetadata().getDataOwner());
    dataOwner.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    MetadataField producerApplication = MetadataField.createInstance(
      messages.SIARDHomePageLabelForViewerMetadataProducerApplication(),
      database.getMetadata().getProducerApplication());
    producerApplication.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");

    right.add(databaseProduct);
    right.add(dataOriginTimespan);
    right.add(dataOwner);
    right.add(producerApplication);

    metadataInformation.add(left);
    metadataInformation.add(right);
  }

  private void refreshInstance(String databaseUUID) {
    final Widget loading = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div id='loading' class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    container.add(loading);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          updateValidationStatus();
          updateBrowsingStatus();

          container.remove(loading);
        }
      });
  }

  private void updateValidationIndicators() {
    validationIndicators.clear();
    validationIndicators.addStyleName("validation-indicators");
    Label label = new Label(messages.SIARDHomePageTextForValidationIndicators());
    label.addStyleName("label-field");
    validationIndicators.add(label);
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("validation-indicators");
    panel.add(buildIndicators(database.getValidationPassed(), FontAwesomeIconManager.CHECK, "passed",
      messages.SIARDHomePageTextForValidationIndicatorsSuccess(Integer.parseInt(database.getValidationPassed()))));
    panel.add(buildIndicators(database.getValidationErrors(), FontAwesomeIconManager.TIMES, "errors",
      messages.SIARDHomePageTextForValidationIndicatorsFailed(Integer.parseInt(database.getValidationErrors()))));
    panel.add(buildIndicators(database.getValidationWarnings(), FontAwesomeIconManager.WARNING, "warnings",
      messages.SIARDHomePageTextForValidationIndicatorsWarnings(Integer.parseInt(database.getValidationWarnings()))));
    panel.add(buildIndicators(database.getValidationSkipped(), FontAwesomeIconManager.SKIPPED, "skipped",
      messages.SIARDHomePageTextForValidationIndicatorsSkipped(Integer.parseInt(database.getValidationSkipped()))));
    validationIndicators.add(panel);
  }

  private FlowPanel buildIndicators(String indicator, String icon, String style, String title) {
    FlowPanel panel = new FlowPanel();
    panel.setStyleName("indicator");
    panel.setTitle(title);
    Label label = new Label(indicator);
    HTML iconHTML = new HTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(icon)));
    iconHTML.addStyleName(style);
    panel.add(iconHTML);
    panel.add(label);

    return panel;
  }

  private void updateValidationStatus() {
    BrowserService.Util.getInstance().getDateTimeHumanized(database.getValidatedAt(),
      new DefaultAsyncCallback<String>() {
        @Override
        public void onSuccess(String result) {
          validatedAt.updateText(result);
          version.updateText(database.getValidatedVersion());
          validationStatus.updateText(SolrHumanizer.humanize(database.getValidationStatus()));
          updateValidationIndicators();
          switch (database.getValidationStatus()) {
            case VALIDATION_SUCCESS:
              updateValidationButtons(true);
              btnOpenValidator.setVisible(false);
              validationStatus.getMetadataValue().setStyleName("label-success");
              break;
            case VALIDATION_FAILED:
              updateValidationButtons(true);
              btnOpenValidator.setVisible(false);
              validationStatus.getMetadataValue().setStyleName("label-danger");
              break;
            case VALIDATION_RUNNING:
              updateValidationButtons(false);
              validationStatus.getMetadataValue().setStyleName("label-info");
              btnOpenValidator.setVisible(true);
              break;
            default:
              validationStatus.getMetadataValue().setStyleName("label-info");
              btnRunValidator.setVisible(true);
              btnOpenValidator.setVisible(false);
              validationIndicators.setVisible(false);
              btnSeeReport.setVisible(false);
              validationIndicators.setVisible(false);
              validatedAt.setVisible(false);
              version.setVisible(false);
          }
        }
      });
  }

  private void updateValidationButtons(Boolean enable) {
    btnRunValidator.setVisible(enable);
    btnSeeReport.setVisible(enable);
    validatedAt.setVisible(enable);
    version.setVisible(enable);
    validationIndicators.setVisible(enable);
  }

  private void updateBrowsingStatus() {
    browsingStatus.updateText(SolrHumanizer.humanize(database.getStatus()));

    if (database.getStatus() == ViewerDatabase.Status.AVAILABLE) {
      btnIngest.setVisible(false);
      btnBrowse.setVisible(true);
      // btnDelete.setVisible(true);
    } else if (database.getStatus().equals(ViewerDatabase.Status.INGESTING)) {
      btnIngest.setVisible(true);
      btnBrowse.setVisible(false);
      btnIngest.addClickHandler(event -> HistoryManager.gotoIngestSIARDData(database.getUUID(), database.getMetadata().getName()));
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (database != null) {
      refreshInstance(database.getUUID());
    }
  }
}