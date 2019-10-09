/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.server.index.schema;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.common.exceptions.ViewerException;


public interface SolrCollection<M extends IsIndexed> {

  static Field getSearchField() {
    return new Field(Field.FIELD_SEARCH, Field.TYPE_SEARCH).setStored(false).setMultiValued(true);
  }

  static CopyField getCopyAllToSearchField() {
    return new CopyField(ViewerConstants.INDEX_WILDCARD, Field.FIELD_SEARCH);
  }

  Class<M> getObjectClass();

  String getIndexName();

  List<String> getCommitIndexNames();

  String getUniqueId(M modelObject);

  List<Field> getFields();

  List<CopyField> getCopyFields();

  List<DynamicField> getDynamicFields();

  /**
   * Map an model object to a Solr Document ready to index
   * 
   * @param object
   *          The model object
   * @return the Solr Document ready to index.
   */
  SolrInputDocument toSolrDocument(M object)
    throws ViewerException, RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  M fromSolrDocument(SolrDocument doc) throws ViewerException;

}
