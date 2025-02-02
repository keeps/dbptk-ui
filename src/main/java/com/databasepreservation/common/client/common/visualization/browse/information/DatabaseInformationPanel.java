/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.information;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.HTMLWidgetWrapper;
import com.databasepreservation.common.client.widgets.SwitchBtn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseInformationPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DatabaseInformationPanel> instances = new HashMap<>();

  public static DatabaseInformationPanel getInstance(ViewerDatabase database, CollectionStatus status) {
    return instances.computeIfAbsent(database.getUuid(), k -> new DatabaseInformationPanel(database, status));
  }

  interface DatabaseInformationPanelUiBinder extends UiBinder<Widget, DatabaseInformationPanel> {
  }

  private static DatabaseInformationPanelUiBinder uiBinder = GWT.create(DatabaseInformationPanelUiBinder.class);

  private ViewerDatabase database;
  private boolean advancedMode = false; // True means advanced attributes are on, false means advanced view is off
  private CollectionStatus status;

  @UiField
  FlowPanel mainPanel;

  @UiField
  FlowPanel header;

  @UiField
  SimplePanel description;

  @UiField
  FlowPanel metadataContent;

  @UiField
  FlowPanel dataContent;

  @UiField
  FlowPanel dataContentCard;

  @UiField
  SimplePanel cardTitle;

  private DatabaseInformationPanel(ViewerDatabase database, CollectionStatus status) {
    this.database = database;
    this.status = status;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabaseInformation(database.getUuid(), database.getMetadata().getName()));
  }

  private void init() {
    final boolean loadOnAccess = ClientConfigurationManager.getBoolean(false,
      ViewerConstants.PROPERTY_PLUGIN_LOAD_ON_ACCESS);

    if (ViewerDatabaseStatus.METADATA_ONLY.equals(database.getStatus())
      || ViewerDatabaseStatus.INGESTING.equals(database.getStatus())) {
      if (loadOnAccess) {
        loadOnAccess();
      } else {
        mainPanel.clear();
        mainPanel.add(new HTMLWidgetWrapper("loadOnAccess.html"));
      }
    }

    initContent();
  }

  private void initContent() {
    cardTitle.setWidget(CommonClientUtils.getCardTitle(messages.menusidebar_database()));
    header
      .add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_INFORMATION),
        messages.databaseInformationTextForTitle(), "h1"));
    HTML html = new HTML();
    html.addStyleName("font-size-description");

    description.setWidget(html);

    SwitchBtn switchTechInformation = new SwitchBtn(messages.schemaStructurePanelTextForAdvancedOption(), false);
    switchTechInformation.setClickHandler(clickEvent -> {
      switchTechInformation.getButton().setValue(!switchTechInformation.getButton().getValue(), true); // workaround
      // for
      // ie11
      advancedMode = !advancedMode;
      metadataContent.clear();
      dataContent.clear();
      initMetadataContent();
      initDataContent();
    });
    header.add(switchTechInformation);

    initMetadataContent();
    initDataContent();
  }

  private void loadOnAccess() {
    HistoryManager.gotoIngestSIARDData(database.getUuid(), database.getMetadata().getName());
    if (ViewerDatabaseStatus.METADATA_ONLY.equals(database.getStatus())) {
      CollectionService.Util.call((StringResponse databaseUUID) -> {
        instances.remove(database.getUuid());
        HistoryManager.gotoDatabase(databaseUUID.getValue());
        Dialogs.showInformationDialog(messages.SIARDHomePageDialogTitleForBrowsing(),
          messages.SIARDHomePageTextForIngestSuccess(), messages.basicActionClose(), "btn btn-link");
      }, (String errorMessage) -> {
        instances.remove(database.getUuid());
        HistoryManager.gotoSIARDInfo(database.getUuid());
        Dialogs.showErrors(messages.SIARDHomePageDialogTitleForBrowsing(), errorMessage, messages.basicActionClose());
      }).createCollection(database.getUuid());
    }
  }

  private void initDataContent() {
    if (database.getMetadata().getSchemas().size() == 1) {
      if (dataContentCard.getWidgetCount() == 1) {
        FlowPanel cardTitlePanel = CommonClientUtils.getCardTitle(messages.schema());
        cardTitlePanel.addStyleName("card-header");
        dataContentCard.insert(cardTitlePanel, 0);
      }
      final DataPanel instance = DataPanel.getInstance(database, database.getMetadata().getSchemas().get(0).getUuid(),
        status);
      instance.reload(advancedMode);
      dataContent.add(instance);
    } else {
      dataContentCard.addStyleName("card-diagram");
      TabPanel tabPanel = new TabPanel();
      tabPanel.addStyleName("information-panel-multi-schemas-tab");
      for (ViewerSchema schema : database.getMetadata().getSchemas()) {
        final DataPanel instance = DataPanel.getInstance(database, schema.getUuid(), status);
        instance.reload(advancedMode);
        tabPanel.add(instance, schema.getName());

      }
      tabPanel.selectTab(0);
      dataContent.add(tabPanel);
    }
  }

  private void initMetadataContent() {
    // database metadata
    ViewerMetadata metadata = database.getMetadata();

    metadataContent.add(getMetadataField(messages.siardMetadata_databaseName(), metadata.getName()));
    if (ViewerStringUtils.isNotBlank(metadata.getDescription())) {
      metadataContent.add(getMetadataField(messages.description(), metadata.getDescription()));
    } else {
      metadataContent.add(getMetadataField(messages.description(), messages.siardMetadata_DescriptionUnavailable()));
    }
    metadataContent
      .add(getMetadataField(messages.siardMetadata_dataOriginTimeSpan(), metadata.getDataOriginTimespan()));
    metadataContent.add(getMetadataField(messages.siardMetadata_dataOwner(), metadata.getDataOwner()));
    metadataContent.add(getMetadataField(messages.siardMetadata_archivalDate(),
      metadata.getArchivalDate() != null ? metadata.getArchivalDate().substring(0, 10) : metadata.getArchivalDate()));

    if (advancedMode) {
      metadataContent.add(getMetadataField(messages.siardMetadata_archivist(), metadata.getArchiver()));
      metadataContent.add(getMetadataField(messages.siardMetadata_archivistContact(), metadata.getArchiverContact()));
      metadataContent.add(getMetadataField(messages.siardMetadata_clientMachine(), metadata.getClientMachine()));
      metadataContent.add(getMetadataField(messages.siardMetadata_databaseProduct(), metadata.getDatabaseProduct()));
      metadataContent
        .add(getMetadataField(messages.siardMetadata_producerApplication(), metadata.getProducerApplication()));
    }
  }

  private MetadataField getMetadataField(String label, String value) {
    if (value != null) {
      MetadataField metadataField = MetadataField.createInstance(label, value);
      metadataField.setCSS("metadata-field", "metadata-information-element-label",
        "metadata-information-element-value");

      return metadataField;
    }

    final MetadataField instance = MetadataField.createInstance(label,
      messages.managePageTableHeaderTextForDatabaseStatus());
    instance.setCSS("metadata-field", "metadata-information-element-label", "metadata-information-element-value");

    return instance;
  }

  @Override
  protected void onAttach() {
    super.onAttach();

    DatabaseService.Util.call((IsIndexed result) -> {
      database = (ViewerDatabase) result;
      if (ViewerDatabaseStatus.METADATA_ONLY.equals(database.getStatus())
        || ViewerDatabaseStatus.INGESTING.equals(database.getStatus())) {
        HistoryManager.gotoIngestSIARDData(database.getUuid(), database.getMetadata().getName());
      }
    }).retrieve(database.getUuid());
  }
}
