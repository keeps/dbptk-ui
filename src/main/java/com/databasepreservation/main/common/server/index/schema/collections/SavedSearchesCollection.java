/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.main.common.server.index.schema.collections;

import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;
import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_SEARCHES_DATE_ADDED;
import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_SEARCHES_DESCRIPTION;
import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_SEARCHES_NAME;
import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_SEARCHES_SEARCH_INFO_JSON;
import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_SEARCHES_TABLE_NAME;
import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_SEARCHES_TABLE_UUID;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.main.common.server.index.schema.AbstractSolrCollection;
import com.databasepreservation.main.common.server.index.schema.CopyField;
import com.databasepreservation.main.common.server.index.schema.Field;
import com.databasepreservation.main.common.server.index.schema.SolrCollection;
import com.databasepreservation.main.common.server.index.utils.SolrUtils;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.search.SavedSearch;
import com.databasepreservation.main.common.shared.exceptions.ViewerException;
import com.databasepreservation.utils.JodaUtils;

public class SavedSearchesCollection extends AbstractSolrCollection<SavedSearch> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SavedSearchesCollection.class);

  @Override
  public Class<SavedSearch> getObjectClass() {
    return SavedSearch.class;
  }

  @Override
  public String getIndexName() {
    return ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(newStoredRequiredNotMultivaluedField(SOLR_SEARCHES_NAME, Field.TYPE_TEXT, true));
    fields.add(newStoredRequiredNotMultivaluedField(SOLR_SEARCHES_TABLE_NAME, Field.TYPE_STRING, true));
    fields.add(new Field(SOLR_SEARCHES_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));

    fields.add(newStoredRequiredNotMultivaluedField(SOLR_SEARCHES_DATE_ADDED, Field.TYPE_DATE, false));
    fields.add(newStoredRequiredNotMultivaluedField(SOLR_SEARCHES_DATABASE_UUID, Field.TYPE_STRING, true));
    fields.add(newStoredRequiredNotMultivaluedField(SOLR_SEARCHES_TABLE_UUID, Field.TYPE_STRING, true));
    fields.add(newStoredRequiredNotMultivaluedField(SOLR_SEARCHES_SEARCH_INFO_JSON, Field.TYPE_STRING, false));

    return fields;
  }

  private Field newStoredRequiredNotMultivaluedField(String name, String type, boolean indexed) {
    return new Field(name, type).setIndexed(indexed).setStored(true).setRequired(true).setMultiValued(false);
  }

  @Override
  public SolrInputDocument toSolrDocument(SavedSearch savedSearch) throws ViewerException, RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(savedSearch);

    if (StringUtils.isBlank(savedSearch.getDateAdded())) {
      savedSearch.setDateAdded(JodaUtils.solrDateFormat(DateTime.now(DateTimeZone.UTC)));
    }

    doc.addField(SOLR_SEARCHES_NAME, savedSearch.getName());
    doc.addField(SOLR_SEARCHES_DESCRIPTION, savedSearch.getDescription());
    doc.addField(SOLR_SEARCHES_DATE_ADDED, savedSearch.getDateAdded());
    doc.addField(SOLR_SEARCHES_DATABASE_UUID, savedSearch.getDatabaseUUID());
    doc.addField(SOLR_SEARCHES_TABLE_UUID, savedSearch.getTableUUID());
    doc.addField(SOLR_SEARCHES_TABLE_NAME, savedSearch.getTableName());
    doc.addField(SOLR_SEARCHES_SEARCH_INFO_JSON, savedSearch.getSearchInfoJson());

    return doc;
  }

  @Override
  public SavedSearch fromSolrDocument(SolrDocument doc) throws ViewerException {
    SavedSearch savedSearch = super.fromSolrDocument(doc);

    savedSearch.setName(SolrUtils.objectToString(doc.get(SOLR_SEARCHES_NAME), null));
    savedSearch.setDescription(SolrUtils.objectToString(doc.get(SOLR_SEARCHES_DESCRIPTION), null));
    savedSearch.setDateAdded(dateToIsoDateString(SolrUtils.objectToDate(doc.get(SOLR_SEARCHES_DATE_ADDED))));
    savedSearch.setDatabaseUUID(SolrUtils.objectToString(doc.get(SOLR_SEARCHES_DATABASE_UUID), null));
    savedSearch.setTableUUID(SolrUtils.objectToString(doc.get(SOLR_SEARCHES_TABLE_UUID), null));
    savedSearch.setTableName(SolrUtils.objectToString(doc.get(SOLR_SEARCHES_TABLE_NAME), null));
    savedSearch.setSearchInfoJson(SolrUtils.objectToString(doc.get(SOLR_SEARCHES_SEARCH_INFO_JSON), null));

    return savedSearch;
  }

  private static String dateToIsoDateString(Date date) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss' UTC'");
    df.setTimeZone(tz);
    return df.format(date);
  }

}
