package config.i18n.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public interface ClientMessages extends Messages {
  String browserOfflineError();

  String cannotReachServerError();

  String alertErrorTitle();

  String noItemsToDisplay();

  String databaseDoesNotContainUsers();

  String databaseDoesNotContainRoles();

  String databaseDoesNotContainPrivileges();

  String titleUsers();

  String titleReport();

  String titleDescription();

  String titleAdmin();

  String titleRoles();

  String titleType();

  String titlePrivileges();

  String titleGrantor();

  String titleGrantee();

  String titleObject();

  String titleOption();

  String searchFieldDatePlaceHolder();

  String searchFieldDateFromPlaceHolder();

  String searchFieldDateToPlaceHolder();

  String searchFieldNumericPlaceHolder();

  String searchFieldNumericFromPlaceHolder();

  String searchFieldNumericToPlaceHolder();

  String searchFieldTimeFromPlaceHolder();

  String searchFieldTimeToPlaceHolder();

  String fillUsernameAndPasswordMessage();

  String couldNotLoginWithTheProvidedCredentials();

  String loginProfile();

  String loginLogout();

  String loginLogin();

  String dialogNotFoundGoToHome();

  String dialogResourceNotFound();

  String dialogPermissionDenied();

  String dialogMustLogin();

  String dialogLogin();

  String menusidebar_database();

  String menusidebar_information();

  String menusidebar_usersRoles();

  String menusidebar_savedSearches();

  String menusidebar_searchAllRecords();

  String menusidebar_structure();

  String menusidebar_routines();

  String menusidebar_triggers();

  String menusidebar_checkConstraints();

  String menusidebar_views();

  String menusidebar_data();

  String menusidebar_filterSidebar();

  String siardMetadata_databaseName();

  String siardMetadata_archivalDate();

  String siardMetadata_archivist();

  String siardMetadata_archivistContact();

  String siardMetadata_clientMachine();

  String siardMetadata_databaseProduct();

  String siardMetadata_databaseUser();

  String siardMetadata_dataOriginTimeSpan();

  String siardMetadata_dataOwner();

  String description();

  String siardMetadata_producerApplication();

  String siardMetadata_DescriptionUnavailable();

  String menusidebar_manageDatabases();

  String searchPlaceholder();

  String diagram();

  String diagram_usingTheDiagram();

  String diagram_Explanation();

  String diagram_rows(@PluralCount int itemCount);

  String diagram_columns(@PluralCount int itemCount);

  String diagram_relations(@PluralCount int itemCount);

  String references_relation();

  String references_relatedTable();

  String references_foreignKeyName();

  String references_foreignKeyDescription();

  String references_thisRecordIsRelatedTo();

  String references_thisRecordIsReferencedBy();

  String row_downloadLOB();

  String references_isRelatedTo();

  String references_isReferencedBy();

  String constraints_thisSchemaDoesNotHaveAnyCheckConstraints();

  String constraints_constraintsInTable(String tableName);

  String references_referencesForValue(String value);

  String name();

  String constraints_condition();

  String schema_tableName();

  String schema_numberOfRows();

  String schema_numberOfColumns();

  String schema_relationsOut();

  String schema_relationsIn();

  String routines_thisSchemaDoesNotHaveAnyRoutines();

  String routine_sourceCode();

  String routine_body();

  String routine_characteristic();

  String routine_returnType();

  String routineParameter_mode();

  String foreignKeys();

  String foreignKeys_referencedSchema();

  String foreignKeys_referencedTable();

  String mappingSourceToReferenced(String arrowHTML);

  String foreignKeys_matchType();

  String foreignKeys_updateAction();

  String foreignKeys_deleteAction();

  String primaryKey();

  String foreignKeys_usedByAForeignKeyRelation();

  String columnName();

  String typeName();

  String originalTypeName();

  String nullable();

  String thisSchemaDoesNotHaveAnyTriggers();

  String triggersInTable(String tableName);

  String triggeredAction();

  String actionTime();

  String triggerEvent();

  String aliasList();

  String originalQuery();

  String query();

  String loginVisitorMessage();

  String loginDialogTitle();

  String loginDialogUsername();

  String loginDialogPassword();

  String loginDialogLogin();

  String loginDialogCancel();

  String editingSavedSearch();

  String noRecordsMatchTheSearchTerms();

  String homePageDescriptionTop();

  String homePageDescriptionBottom();

  String homePageDescriptionHere();

  String loading();

  String inSchema();

  String inTable();

  String inColumn();

  String loadingTableInfo();

  String searchAllRecords();

  String references();

  String savedSearch_Save();

  String savedSearch_Cancel();

  String exportVisible();

  String exportAll();

  String goHome();

  String goToDatabaseInformation();

  String aboutDBVTK();

  String whatIsDBVTK();

  String license();

  String download();

  String binary();

  String source();

  String development();

  String bugReporting();

  String contactUs();

  String infoAndSupport();

  String databaseName();

  String databaseStatus();

  String uniqueID();

  String edit();

  String delete();

  String table();

  String created();

  String actions();

  String thereAreNoSavedSearches();

  String largeBinaryObject();

  String searchingRange_to();

  String searchingTime_at();

  String saveSearch();

  String saving();

  String searchOnTable(String tableName);

  String addSearchField();

  String search();

  String menusidebar_home();

  String menusidebar_databases();

  String menusidebar_savedSearch();

  String menusidebar_record();

  String menusidebar_referencesForColumn(String columnNameInTable);

  String databaseListing();

  String of();

  String ofOver();

  String showMore();

  String showLess();

  String includingStoredProceduresAndFunctions();

  String schemaName();

  String schemaDescription();

  String newUpload();

  String uploadedSIARD();

  String uploads();

  String newUploadLabel();

  String usersAndPermissions();

  String createCardHeader();

  String createCardText();

  String createCardButton();

  String openCardHeader();

  String openCardText();

  String openCardButton();

  String manageCardHeader();

  String manageCardText();

  String manageCardButton();

  String viewerMetadataName();

  String viewerMetadataArchivalDate();

  String viewerMetadataArchiver();

  String viewerMetadataArchiverContact();

  String viewerMetadataClientMachine();

  String viewerMetadataDatabaseProduct();

  String viewerMetadataDataOriginTimespan();

  String viewerMetadataDataOwner();

  String viewerMetadataProducerApplication();

  String navigationSIARD();

  String navigationValidation();

  String navigationBrowsing();

  String editMetadata();

  String sendToLiveDBMS();

  String validateNow();

  String seeReport();

  String browseNow();

  String deleteIngested();

  String showFile();

  String validatedAt();

  String validationVersionLabel();

  String SIARDNotValidated();

  String SIARD();

  String manageSIARD();

  String dialogReimportSIARDTitle();

  String dialogReimportSIARD();

  String dialogCancel();

  String dialogConfirm();

  String SIARDLocation();

  String SIARDValidated();

  String solrIngesting();

  String solrAvailable();

  String solrMetadataOnly();

  String solrRemoving();

  String solrError();

  String open();

  String createSIARD();

  String openSIARD();

  String createSIARDConnection();

  String cancel();

  String next();

  String back();

  String menuSidebarSIARD();

  String menuSidebarDatabases();

  String migrate();

  String createSIARDTableAndColumns();

  String createSIARDExportOptions();

  String createSIARDExternalLOBsOptions();

  String createSIARDMetadataOptions();

  String tabGeneral();

  String tabSSHTunnel();

  String useSSHTunnel();

  String proxyHostLabel();

  String proxyPortLabel();

  String proxyUserLabel();

  String proxyPasswordLabel();

  String createSIARDCustomViews();

  String siardversionLabel();

  String siardExportBrowseButton();

  String siardDestinationFolderLabel();

  String siardprettyXMLLabel();

  String siardValidateLabel();

  String siardcompressionLabel();

  String connectionURLLabel();

  String connectionLabels(@Select String fieldName);

  String chooseDriverLocation();

  String sidebarTables();

  String sidebarViews();

  String selectAll();

  String selectNone();

  String customViewsTitle();

  String customViewsDescription();

  String customViewsTestMessage();

  String customViewNameLabel();

  String customViewDescriptionLabel();

  String customViewQueryLabel();

  String update();

  String exportOptionsLabels(@Select String fieldName);

  String exportOptionsHelperText(@Select String fieldName);

  String SIARDEditMetadata();

  String databaseInformation();

  String metadataButtonSave();

  String metadataHasUpdates();

  String metadataMissingFields(String fieldList);

  String metadataSuccessUpdated();

  String metadataFailureUpdated();

  String metadataButtonRevert();

  String metadataDoesNotContainDescription();

  String tableDoesNotContainColumns();

  String tableDoesNotContainPrimaryKey();

  String tableDoesNotContainForeignKeys();

  String tableDoesNotContainCandidateKeys();

  String tableDoesNotContainConstraints();

  String tableDoesNotContainTriggers();

  String candidateKeys();

  String columns();

  String viewDoesNotContainDescription();

  String viewDoesNotContainQuery();

  String viewDoesNotContainQueryOriginal();

  String viewDoesNotContainColumns();

  String routineDoesNotContainDescription();

  String routines_thisRoutineFieldDoesNotHaveContent();

  String routines_parametersList();

  String dialogUpdateMetadata();

  String dialogConfirmUpdateMetadata();

  String retrievingTableStructure();

  String progressBarPanelTables();

  String progressBarPanelRows();

  String progressBarPanelCurrentTables();

  String progressBarPanelCurrentRows();

  String newText();

  String viewName();

  String SIARDError();
}
