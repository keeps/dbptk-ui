/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.schema;

import static com.databasepreservation.common.client.ViewerConstants.INDEX_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.utils.SolrUtils;

public abstract class AbstractSolrCollection<M extends IsIndexed> implements SolrCollection<M> {

  @Override
  public List<Field> getFields() {
    List<Field> ret = new ArrayList<>();

    ret.add(new Field(INDEX_ID, Field.TYPE_STRING));
    ret.add(SolrCollection.getSearchField());

    return ret;
  }

  @Override
  public List<DynamicField> getDynamicFields() {
    List<DynamicField> ret = new ArrayList<>();

    ret.add(new DynamicField("*" + ViewerConstants.SOLR_DYN_TEXT_MULTI, Field.TYPE_TEXT).setIndexed(true)
      .setStored(true).setMultiValued(true));
    ret.add(new DynamicField("*" + ViewerConstants.SOLR_DYN_NEST_MULTI, Field.TYPE_TEXT).setIndexed(false)
      .setStored(false).setMultiValued(true));

    return ret;
  }

  @Override
  public SolrInputDocument toSolrDocument(M object) throws ViewerException, RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = new SolrInputDocument();

    if (object != null) {
      doc.addField(INDEX_ID, getUniqueId(object));
    }

    return doc;
  }

  @Override
  public M fromSolrDocument(SolrDocument doc) throws ViewerException {
    M ret;
    try {
      ret = getObjectClass().newInstance();
      ret.setUuid(SolrUtils.objectToString(doc.get(INDEX_ID), null));
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ViewerException(e);
    }

    return ret;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Collections.singletonList(getIndexName());
  }

  @Override
  public String getUniqueId(M modelObject) {
    return modelObject.getUuid();
  }
}
