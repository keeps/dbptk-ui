package com.databasepreservation.common.client.common.search;

import java.util.Collections;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.panel.SearchFieldPanel;
import com.databasepreservation.common.client.common.search.panel.SearchFieldPanelFactory;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class DatabaseAdvancedSearchFields extends FlowPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DatabaseAdvancedSearchFields() {
    addStyleName("searchAdvancedFieldsPanel");
    addStyleName("database-metadata-advanced-search");

    addSearchField(ViewerConstants.SOLR_DATABASES_METADATA_NAME, messages.siardMetadata_databaseName());
    addSearchField(ViewerConstants.SOLR_DATABASES_METADATA_DESCRIPTION, messages.description());
    addSearchField(ViewerConstants.SOLR_DATABASES_METADATA_DATA_OWNER, messages.siardMetadata_dataOwner());
    addSearchField(ViewerConstants.SOLR_DATABASES_METADATA_ORIGIN_TIMESPAN,
      messages.siardMetadata_dataOriginTimeSpan());

    addSearchField("{!parent which='content_type:database'}" + ViewerConstants.SOLR_DATABASES_TABLE_NAME,
      messages.schema_tableName());
    addSearchField("{!parent which='content_type:database'}" + ViewerConstants.SOLR_DATABASES_COLUMN_NAME,
      messages.columnName());
  }

  private void addSearchField(String solrField, String label) {
    SearchField field = new SearchField(solrField, Collections.singletonList(solrField), label,
      ViewerConstants.SEARCH_FIELD_TYPE_TEXT);
    SearchFieldPanel panel = SearchFieldPanelFactory.getSearchFieldPanel(field);
    panel.setup();
    add(panel);
  }
}