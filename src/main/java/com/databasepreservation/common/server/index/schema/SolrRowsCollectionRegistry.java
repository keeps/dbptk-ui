/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.server.index.schema;

import static com.databasepreservation.common.shared.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.server.index.schema.collections.RowsCollection;
import com.databasepreservation.common.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.common.exceptions.ViewerException;

public final class SolrRowsCollectionRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrRowsCollectionRegistry.class);

  private SolrRowsCollectionRegistry() {
  }

  private static final Map<String, RowsCollection> REGISTRY = new HashMap<>();

  public static void register(RowsCollection collection) {
    if (!REGISTRY.containsKey(collection.getDatabaseUUID())) {
      LOGGER.debug("Registering rows collection: {}", collection.getIndexName());
      REGISTRY.put(collection.getDatabaseUUID(), collection);
    }
  }

  public static void registerExisting(Collection<String> existingCollections) {
    existingCollections.stream().filter(c -> c.startsWith(SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX)).forEach(index -> {
      String databaseUUID = index.substring(SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX.length());
      if (StringUtils.isNotBlank(databaseUUID)) {
        register(new RowsCollection(databaseUUID));
      }
    });
  }

  public static Collection<RowsCollection> registry() {
    return Collections.unmodifiableCollection(REGISTRY.values());
  }

  public static List<String> registryIndexNames() {
    return registry().stream().map(col -> col.getIndexName()).collect(Collectors.toList());
  }

  public static RowsCollection get(String databaseUUID) {
    return REGISTRY.get(databaseUUID);
  }

  public static ViewerRow fromSolrDocument(String databaseUUID, SolrDocument doc, List<String> fieldsToReturn)
    throws ViewerException, NotSupportedException {
    RowsCollection solrCollection = get(databaseUUID);
    if (solrCollection != null) {
      return solrCollection.fromSolrDocument(doc);
    } else {
      throw new NotSupportedException("Could not find Solr collection relative to '" + databaseUUID + "' in registry.");
    }
  }

  public static ViewerRow fromSolrDocument(String databaseUUID, SolrDocument doc)
    throws ViewerException, NotSupportedException {
    return fromSolrDocument(databaseUUID, doc, Collections.emptyList());
  }

  public static SolrInputDocument toSolrDocument(String databaseUUID, ViewerRow object) throws ViewerException,
    RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, NotSupportedException {
    RowsCollection solrCollection = get(databaseUUID);
    if (solrCollection != null) {
      return solrCollection.toSolrDocument(object);
    } else {
      throw new NotSupportedException("Could not find Solr collection relative to '" + databaseUUID + "' in registry.");
    }
  }

  public static String getIndexName(String databaseUUID) throws NotSupportedException {
    RowsCollection solrCollection = get(databaseUUID);
    if (solrCollection != null) {
      return solrCollection.getIndexName();
    } else {
      throw new NotSupportedException("Could not find Solr collection relative to '" + databaseUUID + "' in registry.");
    }
  }

  public static List<String> getCommitIndexNames(String databaseUUID) throws NotSupportedException {
    RowsCollection solrCollection = get(databaseUUID);
    if (solrCollection != null) {
      return solrCollection.getCommitIndexNames();
    } else {
      throw new NotSupportedException("Could not find Solr collection relative to '" + databaseUUID + "' in registry.");
    }
  }
}
