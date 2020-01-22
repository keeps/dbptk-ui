package config.i18n.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public interface ClientMessages extends Messages {
  String browserOfflineError();

  String cannotReachServerError();

  String alertErrorTitle();

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

  String schemaDescriptionLabel();

  String siardMetadata_producerApplication();

  String siardMetadata_DescriptionUnavailable();

  String menusidebar_manageDatabases();

  String searchPlaceholder();

  String schema();

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

  String casForwardWarning();

  String loginMissingPermissions();

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

  String uniqueID();

  String edit();

  String delete();

  String table();

  String created();

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

  String usersAndPermissions();

  String SIARD();

  String dialogReimportSIARDTitle();

  String dialogReimportSIARD();

  String update();

  String databaseInformation();

  String databaseInformationDescription();

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

  String dialogLargeFileConfirmUpdateMetadata();

  String newText();

  String SIARDError();

  /********************************************
   * Information Strings
   ********************************************/
  String noItemsToDisplay();

  /********************************************
   * Humanized Strings
   ********************************************/
  String humanizedTextForSIARDValidationSuccess();

  String humanizedTextForSIARDValidationFailed();

  String humanizedTextForSIARDValidationRunning();

  String humanizedTextForSIARDNotValidated();

  String humanizedTextForSolrIngesting();

  String humanizedTextForSolrAvailable();

  String humanizedTextForSolrMetadataOnly();

  String humanizedTextForSolrRemoving();

  String humanizedTextForSolrError();

  String humanizedTextForJobStatusFailed();

  String humanizedTextForJobStatusCompleted();

  String humanizedTextForJobStatusStarting();

  String humanizedTextForJobStatusStarted();

  String humanizedTextForJobStatusStopping();

  String humanizedTextForJobStatusStopped();

  String humanizedTextForJobStatusAbandoned();

  String humanizedTextForJobStatusUnknown();

  String durationDHMSShortDays(int days, int hours, int minutes, int seconds);

  String durationDHMSShortHours(int hours, int minutes, int seconds);

  String durationDHMSShortMinutes(int minutes, int seconds);

  String durationDHMSShortSeconds(int seconds);

  String durationDHMSLongDays(int days, int hours, int minutes, int seconds);

  String durationDHMSLongHours(int hours, int minutes, int seconds);

  String durationDHMSLongMinutes(int minutes, int seconds);

  String durationDHMSLongSeconds(int seconds);

  String durationDHMSShortMillis(int millis);

  String yes();

  String no();

  String humanizedTextForViewerJobStatus(@Select String status);

  /********************************************
   * Basic Table Headers
   ********************************************/
  String basicTableHeaderShow();

  String basicTableHeaderDescription();

  String basicTableHeaderLabel();

  String basicTableHeaderTableOrColumn(@Select String value);

  String basicTableHeaderOrder();

  /********************************************
   * Basic Actions
   ********************************************/
  String basicActionClose();

  String basicActionCancel();

  String basicActionDiscard();

  String basicActionConfirm();

  String basicActionClear();

  String basicActionNext();

  String basicActionBack();

  String basicActionSkip();

  String basicActionSelectAll();

  String basicActionSelectNone();

  String basicActionOpen();

  String basicActionAdd();

  String basicActionMigrate();

  String basicActionBrowse();

  String basicActionUpdate();

  String basicActionNew();

  String basicActionSave();

  String basicActionTest();

  String basicActionImport();

  String basicActionUnderstood();

  String basicActionDelete();

  String basicActionOptions();

  String basicActionExport();

  /*********************************************
   * Sidebar Menus
   ********************************************/
  String sidebarMenuTextForDatabases();

  String sidebarMenuTextForTables();

  String sidebarMenuTextForViews();

  /*********************************************
   * Breadcrumbs Text
   ********************************************/
  String breadcrumbTextForManageDatabase();

  String breadcrumbTextForWizardCreateSIARDConnection();

  String breadcrumbTextForWizardCreateSIARDTableAndColumns();

  String breadcrumbTextForWizardCreateSIARDExportOptions();

  String breadcrumbTextForWizardCreateSIARDMetadataOptions();

  String breadcrumbTextForWizardCreateSIARDCustomViews();

  String breadcrumbTextForSIARDEditMetadata();

  String breadcrumbTextForCreateSIARD();

  String breadcrumbTextForWizardSendToTableAndColumns();

  String breadcrumbTextForWizardSendToDBMSConnection();

  String breadcrumbTextForWizardSendToSIARDExportOptions();

  String breadcrumbTextForWizardSendToMetadataExportOptions();

  String breadcrumbTextForWizardSendToProgressPanel();

  String breadcrumbTextForSIARDValidator();

  String breadcrumbTextForSIARDIngesting();

  String breadcrumbTextForActivityLog();

  String breadcrumbTextForActivityLogDetail();

  String breadcrumbTextForAdvancedConfiguration();

  String breadcrumbTextForDataTransformation();

  String breadcrumbTextForJobManager();

  String breadcrumbTextForPreferences();

  String breadcrumbTextForTableManagement();

  String breadcrumbTextForColumnManagement();

  /*********************************************
   * Home Page
   ********************************************/
  String homePageButtonTextForCreateSIARD();

  String homePageButtonTextForOpenSIARD();

  String homePageButtonTextForManageSIARD();

  String homePageHeaderTextForCreateSIARD();

  String homePageDescriptionTextForCreateSIARD();

  String homePageHeaderTextForOpenSIARD();

  String homePageDescriptionTextForOpenSIARD();

  String homePageHeaderTextForManageSIARD();

  String homePageDescriptionTextForManageSIARD();

  String homePageTextForFinancedBy();

  String homePageTextForDevelopedBy();

  /********************************************
   * Manage SIARD
   ********************************************/
  String managePageButtonTextForCreateSIARD();

  String managePageButtonTextForDownloadDBPTK();

  String managePageButtonTextForOpenSIARD();

  String managePageTableHeaderTextForDatabaseName();

  String managePageTableHeaderTextForDataOwner();

  String managePageTableHeaderTextForProductName();

  String managePageTableHeaderTextForArchivalDate();

  String managePageTableHeaderTextForSIARDLocation();

  String managePageTableHeaderTextForSIARDSize();

  String managePageTableHeaderTextForSIARDVersion();

  String managePageTableHeaderTextForSIARDValidationStatus();

  String managePageTableHeaderTextForDatabaseStatus();

  String managePageTableHeaderTextForActions();

  String manageDatabasePageDescription();

  /********************************************
   * SIARD Home Page
   ********************************************/
  String SIARDManagerPageInformationDialogTitle();

  String SIARDManagerPageTextForWaitForFinishing();

  String SIARDHomePageToastTitle(String method);

  String SIARDHomePageButtonTextEditMetadata();

  String SIARDHomePageButtonTextMigrateToSIARD();

  String SIARDHomePageButtonTextSendToLiveDBMS();

  String SIARDHomePageButtonTextShowFile();

  String SIARDHomePageButtonTextValidateNow();

  String SIARDHomePageButtonTextRunValidationAgain();

  String SIARDHomePageButtonTextOpenValidate();

  String SIARDHomePageButtonTextSeeReport();

  String SIARDHomePageButtonTextForBrowseNow();

  String SIARDHomePageButtonTextForIngest();

  String SIARDHomePageButtonTextForStartIngest();

  String SIARDHomePageButtonTextForDeleteIngested();

  String SIARDHomePageOptionsHeaderForSIARD();

  String SIARDHomePageOptionsHeaderForValidation();

  String SIARDHomePageOptionsHeaderForBrowsing();

  String SIARDHomePageLabelForViewerMetadataName();

  String SIARDHomePageLabelForViewerMetadataArchivalDate();

  String SIARDHomePageLabelForViewerMetadataArchiver();

  String SIARDHomePageLabelForViewerMetadataArchiverContact();

  String SIARDHomePageLabelForViewerMetadataClientMachine();

  String SIARDHomePageLabelForViewerMetadataDatabaseProduct();

  String SIARDHomePageLabelForViewerMetadataDataOriginTimespan();

  String SIARDHomePageLabelForViewerMetadataDataOwner();

  String SIARDHomePageLabelForViewerMetadataProducerApplication();

  String SIARDHomePageLabelForViewerMetadataDescription();

  String SIARDHomePageLabelForSIARDVersion();

  String SIARDHomePageLabelForSIARDSize();

  String SIARDHomePageLabelForSIARDPath();

  String SIARDHomePageLabelForValidatedAt();

  String SIARDHomePageLabelForValidationVersion();

  String SIARDHomePageTextForMissingDescription();

  String SIARDHomePageLabelForValidationStatus();

  String SIARDHomePageTextForValidationIndicators();

  String SIARDHomePageTextForValidationIndicatorsSuccess(@PluralCount int passed);

  String SIARDHomePageTextForValidationIndicatorsFailed(@PluralCount int errors);

  String SIARDHomePageTextForValidationIndicatorsWarnings(@PluralCount int warnings);

  String SIARDHomePageTextForValidationIndicatorsSkipped(@PluralCount int skipped);

  String SIARDHomePageLabelForBrowseStatus();

  String SIARDHomePageTextForSIARDValid(String name);

  String SIARDHomePageTextForSIARDInvalid(String name);

  String SIARDHomePageTextForIngestSIARDTitle();

  String SIARDHomePageTextForIngestSIARDSubtitle();

  String SIARDHomePageDialogTitleForBrowsing();

  String SIARDHomePageDialogTitleForDelete();

  String SIARDHomePageDialogTitleForDeleteBrowseContent();

  String SIARDHomePageDialogTitleForDeleteValidationReport();

  SafeHtml SIARDHomePageTextForDeleteAllFromServer();

  SafeHtml SIARDHomePageTextForDeleteAllFromDesktop();

  SafeHtml SIARDHomePageTextForDeleteFromSolr();

  SafeHtml SIARDHomePageTextForDeleteSIARD();

  SafeHtml SIARDHomePageTextForDeleteSIARDReportValidation();

  String SIARDHomePageTextForIngestNotSupported();

  String SIARDHomePageTextForIngestSuccess();

  String SIARDHomePageTextForRequiredSIARDFile();

  /********************************************
   * Edit Metadata
   ********************************************/
  String editMetadataNotificationTitle();

  String editMetadataInformationMessage();

  /********************************************
   * Create Wizard: Home Page
   ********************************************/
  String createSIARDWizardManagerErrorTitle();

  String createSIARDWizardManagerSelectDataSourceError();

  /********************************************
   * Create Wizard: Information Messages
   ********************************************/
  String createSIARDWizardManagerInformationMessagesTitle();

  String createSIARDWizardManagerInformationMessage();

  String createSIARDWizardManagerSIARDCreated();

  /********************************************
   * Connection Page
   ********************************************/
  String connectionPageTextForTabGeneral();

  String connectionPageTextForTabSSHTunnel();

  String connectionPageLabelForUseSSHTunnel();

  String connectionPageLabelForProxyHost();

  String connectionPageLabelForProxyPort();

  String connectionPageLabelForProxyUserLabel();

  String connectionPageLabelForProxyPasswordLabel();

  String connectionPageDescriptionForProxyUser();

  String connectionPageDescriptionForProxyHost();

  String connectionPageDescriptionForProxyPort();

  String connectionPageDescriptionForProxyPassword();

  String connectionPageLabelsFor(@Select String fieldName);

  String connectionPageDescriptionsFor(@Select String fieldName);

  String connectionPageLabelForChooseDriverLocation();

  String connectionPageLabelForChooseFileLocation();

  String errorMessagesConnectionTitle();

  String connectionPageErrorMessageFor(@Select int error);

  String connectionPageTitle();

  String connectionPageTextForWelcome();

  String connectionPageTextForConnectionHelper();

  String connectionPageTextForTableAndColumnsHelper();

  String connectionPageTextForCustomViewsHelper();

  String connectionPageTextForExportOptionsHelper();

  String connectionPageTextForMetadataExportOptionsHelper();

  String connectionPageTextForWelcomeDBMSHelper();

  String connectionPageTextForDBMSHelper();

  String connectionPageTextForSSHelper();

  String connectionPageButtonTextForTestConnection();

  String connectionPageTextForConnectionSuccess(String databaseName);

  /********************************************
   * Create Wizard: Table & Columns
   ********************************************/
  String tableAndColumnsPageTitle();

  String tableAndColumnsPageTextForExternalLOBConfigure();

  String tableAndColumnsPageLabelForReferenceType();

  String tableAndColumnsPageLabelForBasePath();

  String tableAndColumnsPageTableHeaderTextForColumnFilters();

  String tableAndColumnsPageTableHeaderTextForColumnName();

  String tableAndColumnsPageTableHeaderTextForOriginalTypeName();

  String tableAndColumnsPageTableHeaderTextForDescription();

  String tableAndColumnsPageTableHeaderTextForOptions();

  String tableAndColumnsPageTableHeaderTextForSelect();

  String tableAndColumnsPageTableHeaderTextForViewName();

  String tableAndColumnsPageTableHeaderTextForTableName();

  String tableAndColumnsPageTableHeaderTextForNumberOfRows();

  String tableAndColumnsPageDialogTitleForExternalLOBDialog();

  String tableAndColumnsPageErrorMessageFor(@Select int error);

  String tableAndColumnsPageDialogTitleForRetrievingInformation();

  String tableAndColumnsPageDialogMessageForRetrievingInformation();

  String tableAndColumnsPageDescriptionForExternalLOBReferenceType();

  String tableAndColumnsPageDescriptionForExternalLOBBasePath();

  /********************************************
   * Create Wizard: Custom Views
   ********************************************/
  String customViewsPageTitle();

  String customViewsUpdateMessage();

  String customViewsPageLabelForSchemaName();

  String customViewsPageLabelForName();

  String customViewsPageLabelForDescription();

  String customViewsPageLabelForQuery();

  String customViewsPageTextForDescription();

  String customViewsPageTextForDialogTitle();

  String customViewsPageTextForDialogMessage();

  String customViewsPageTextForDialogValidatingQuery();

  String customViewsPageTextForQueryResultsDialogTitle();

  String customViewsPageTextForDialogConfirmDelete();

  String customViewsPageErrorMessageForQueryError();

  String customViewsPageErrorMessagesFor(@Select int error);

  String customViewsPageTextForHelpSchemaName();

  String customViewsPageTextForHelpViewName();

  String customViewsPageTextForHelpViewDescription();

  String customViewsPageTextForHelpViewQuery();

  /********************************************
   * Wizard Export Options
   ********************************************/
  String wizardExportOptionsTitle();

  String wizardExportOptionsDescription();

  String wizardMetadataExportOptionsTitle();

  String wizardMetadataExportOptionsDescription();

  String wizardExportOptionsLabels(@Select String fieldName);

  String wizardExportOptionsHelperText(@Select String fieldName);

  String errorMessagesExportOptionsTitle();

  String errorMessagesExportOptions(@Select int error);

  /*********************************************
   * Send to: Table & Columns
   ********************************************/
  String wizardSendToDBMSExportButton();

  /*********************************************
   * Send to: Export Format
   ********************************************/
  String wizardSendToExportFormatTitle();

  String wizardSendToExportFormatSubTitle();

  /*********************************************
   * Send to: Information Messages
   ********************************************/
  String sendToWizardManagerInformationTitle();

  String sendToWizardManagerInformationMessageSIARD();

  String sendToWizardManagerInformationMessageDBMS(String name);


  /*********************************************
   * Open SIARD
   ********************************************/
  String dialogOpenSIARDMessage();

  String errorMessagesOpenFile(String filename);

  /*********************************************
   * SIARD Validator
   ********************************************/
  String SIARDValidatorSettings();

  String SIARDValidatorDialogInformationTitle();

  String SIARDValidatorTextForVersionCannotBeValidated();

  String allowedTypes();

  String reporterDestinationFolder();

  String reporterTip();

  String allowedTypesTip();

  String clear();

  String validatorPageTextForTitle();

  String reportFile();

  String validatorPageRequirementsThatFailed();

  String validatorPageRequirementsThatPassed();

  String numberOfValidationsPassed();

  String numberOfValidationsWarnings();

  String numberOfValidationsSkipped();

  String numberOfValidationsErrors();

  String validatorPageTextForSIARDVersion();

  String validatorPageTextForSIARDSpecification();

  String validatorPageTextForAdditionalChecksSpecification();

  String validatorPageTextForStick();

  String validatorPageTextForToast();

  String validatorPageTextForDialogSuccessInformation(String databaseUUID);

  String validatorPageTextForDialogFailureInformation(String databaseUUID, @PluralCount int errors);

  String validatorPageTextForErrorDetails();

  String validationRequirements(@Select String codeID);

  String skipAdditionalChecks();

  String skipAdditionalChecksHelpText();

  /*********************************************
   * Progress Bar
   ********************************************/
  String progressBarPanelTextForTables();

  String progressBarPanelTextForRows();

  String progressBarPanelTextForCurrentTable();

  String progressBarPanelTextForCurrentRows();

  String progressBarPanelTextForRetrievingTableStructure();

  String progressBarPanelTextForDBMSWizardTitle(String dbms);

  String progressBarPanelTextForDBMSWizardSubTitle();

  String progressBarPanelTextForCreateWizardProgressTitle();

  String progressBarPanelTextForCreateWizardProgressSubTitle();

  /*********************************************
   * Structure Panel
   ********************************************/
  String schemaStructurePanelTextForAdvancedOption();

  String schemaStructurePanelHeaderTextForForeignKeyName();

  String schemaStructurePanelTextForPageSubtitle();

  /*********************************************
   * Database Information Panel
   ********************************************/
  String databaseInformationTextForTitle();
  /********************************************
   * Upload SIARD
   ********************************************/
  String uploadPanelTextForTitle();

  String uploadPanelTextForDescription();

  String uploadPanelTextForLoading();

  String uploadPanelTextForLabelGoToSIARDInfo();

  String uploadPanelTextForLabelDropHere();

  String uploadPanelTextForLabelBrowseFiles();;

  String uploadSIARDTextForDoneUpload();

  /*********************************************
   *  CSV Export Dialog
   *********************************************/
  String csvExportDialogTitle();

  String csvExportDialogLabelForFilename();

  String csvExportDialogLabelForZipFilename();

  String csvExportDialogLabelForExportRows();

  String csvExportDialogLabelForExportAllRadioButton();

  String csvExportDialogLabelForExportVisibleRadioButton();

  String csvExportDialogLabelForExportHeaderWithDescriptions();

  String csvExportDialogLabelForExportLOBs();

  String csvExportDialogHelpTextForFilename();

  String csvExportDialogHelpTextForZipFilename();

  String csvExportDialogHelpTextForExportSize();

  String csvExportDialogHelpTextForDescription();

  String csvExportDialogHelpTextForLOBs();

  String showAdvancedSearch();

  /********************************************
   * View Panel
   *******************************************/
  String viewPanelViewerNotMaterialized();

  String viewPanelInformationLabel();

  String emptyBasePath();

  /********************************************
   * Row Panel
   *******************************************/
  String rowPanelTextForButtonExportSingleRow();

  String informationNotAvailable();

  /********************************************
   * Advanced Search
   *******************************************/
  String advancedSearchDialogTitle();

  String advancedSearchErrorMessageForDateInvalid();

  String advancedSearchErrorMessageForTimeInvalid();

  String advancedSearchErrorMessageForNumericInvalid();

  String advancedSearchBooleanValueTrue();

  String advancedSearchBooleanValueFalse();

  String advancedSearchBooleanValueDefault();

  /********************************************
   * Activity Log
   *******************************************/
  String activityLogMenuText();

  String activityLogDetailedHeaderText();

  String activityLogDescription();

  String activityLogTextForDate();

  String activityLogTextForComponent();

  String activityLogTextForMethod();

  String activityLogTextForUser();

  String activityLogTextForDuration();

  String activityLogTextForAddress();

  String activityLogTextForParameters();

  String activityLogTextForOutcome();

  String activityLogHumanizedTextForSuccess();

  String activityLogHumanizedTextForFailure();

  String activityLogHumanizedTextForUnauthorized();

  String activityLogHumanizedTextForUnknown();

  String activityLogComponent(@Select String component);

  String activityLogMethod(@Select String method);

  String activityLogFilterName(@Select String name);

  String filterParameterEmpty();

  String activityLogViewedLog();

  String activityLogDatabaseDeleted();

  String activityLogSavedSearchDeleted();

  String activityLogDatabaseRelated();

  String activityLogTableRelated();

  String activityLogRecordRelated();

  String activityLogSavedSearchRelated();

  String activityLogPathRelated();

  String activityLogFilenameRelated();

  String activityLogRelatedLog();

  SafeHtml activityLogFilenameParameter(String value);

  String activityLogSavedSearchName();

  String activityLogSavedSearchDescription();

  String activityLogSearchInfoRelated();

  String activityLogLabelForExportType();

  String activityLogTextForExportTypeTable();

  String activityLogTextForExportTypeRow();

  String activityLogUsernameRelated();
  /********************************************
   * HTML Utils
   *******************************************/
  String sublist(int firstElementIndex, long maximumElementCount);

  String sublistSingleElement();

  String sublistNoElements();

  SafeHtml simpleFilterParameter(String name, String value);

  SafeHtml basicFilterParameter(String name, String value);

  SafeHtml longRangeFilterParameter(String name, long fromValue, long toValue);

  SafeHtml longRangeFilterParameterOnlyFrom(String name, long fromValue);

  SafeHtml longRangeFilterParameterOnlyTo(String name, long toValue);

  SafeHtml longRangeFilterParameterEquals(String name, long value);

  SafeHtml dateRangeFilterParameterOnlyFrom(String date);

  SafeHtml dateRangeFilterParameterOnlyTo(String date);

  SafeHtml dateRangeFilterParameter(String fromDate, String fromTo);

  /********************************************
   * Menu
   *******************************************/
  String menuTextForPreferences();

  String menuTextForAdministration();

  String menuTextForJobs();

  String objectNotFound();

  /********************************************
   * Advanced Configuration
   *******************************************/
  String advancedConfigurationLabelForMainTitle();

  String advancedConfigurationLabelForTableManagement();

  String advancedConfigurationLabelForColumnsManagement();

  String advancedConfigurationLabelForDataTransformation();

  String advancedConfigurationTextForTableManagement();

  String advancedConfigurationTextForColumnsManagement();

  String advancedConfigurationTextForDataTransformation();

  /********************************************
   * Data Transformation
   *******************************************/
  String dataTransformationSidebarDialogTitle();

  String dataTransformationSidebarWaitDialogMessage();

  String dataTransformationSidebarSuccessDialogMessage();

  String dataTransformationTextForAlertColumnsOrder();

  SafeHtml dataTransformationTextForIsReferencedBy(String table, String column);

  SafeHtml dataTransformationTextForIsRelatedTo(String table, String column);

  String dataTransformationTextForSeparetedRadioButton();

  String dataTransformationTextForAggregationRadioButton();

  String dataTransformationTableRowList(List<String> columns);

  /********************************************
   * Preferences Panel
   *******************************************/
  String preferencesPanelTextForDescription();

  /********************************************
   * Batch Jobs
   *******************************************/
  String batchJobsTextForPageTitle();

  String batchJobsTextForPageDescription();

  String batchJobsTextForJobId();

  String batchJobsTextForDatabase();

  String batchJobsTextForTable();

  String batchJobsTextForName();

  String batchJobsTextForCreateTime();

  String batchJobsTextForStartTime();

  String batchJobsTextForEndTime();

  String batchJobsTextForStatus();

  String batchJobsTextForDetail();

  /********************************************
   * Table Management Panel
   *******************************************/
  String tableManagementPageTitle();

  String tableManagementPageToastDescription();

  String tableManagementPageTableTextForDescription();

  String tableManagementPageTableHeaderTextForShow();

  String tableManagementPageTableHeaderTextForLabel();

  String tableManagementPageTableHeaderTextForDescription();

  String tableManagementPageDialogSelectionError();

  String tableManagementPageDialogInputError();

  /********************************************
   * Column Management Panel
   *******************************************/
  String columnManagementPageTitle();

  String columnManagementPageToastDescription();

  String columnManagementPageTableTextForDescription();

  String columnManagementPageDialogErrorDescription();

  String columnManagementPageTextForArrowUp();

  String columnManagementPageTextForArrowDown();

  String columnManagementPageTooltipForTable();

  String columnManagementPageTooltipForDetails();

  String columnManagementPageTooltipForAdvancedSearch();

  /********************************************
   * Sidebar
   *******************************************/
  String tableHeader();
}
