/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.utils;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.facets.FacetFieldResult;
import com.databasepreservation.common.client.index.facets.FacetParameter;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.facets.RangeFacetParameter;
import com.databasepreservation.common.client.index.facets.SimpleFacetParameter;
import com.databasepreservation.common.client.index.filter.AndFiltersParameters;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.BlockJoinAnyParentExpiryFilterParameter;
import com.databasepreservation.common.client.index.filter.BlockJoinParentFilterParameter;
import com.databasepreservation.common.client.index.filter.BoostedSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.CrossCollectionInnerJoinFilterParameter;
import com.databasepreservation.common.client.index.filter.DateIntervalFilterParameter;
import com.databasepreservation.common.client.index.filter.DateRangeFilterParameter;
import com.databasepreservation.common.client.index.filter.EDismaxSimplerQueryFilterParameter;
import com.databasepreservation.common.client.index.filter.EmptyKeyFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.FiltersParameters;
import com.databasepreservation.common.client.index.filter.InnerJoinFilterParameter;
import com.databasepreservation.common.client.index.filter.LongRangeFilterParameter;
import com.databasepreservation.common.client.index.filter.NotSimpleFilterParameter;
import com.databasepreservation.common.client.index.filter.OneOfManyFilterParameter;
import com.databasepreservation.common.client.index.filter.OrFiltersParameters;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.sort.SortParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.structure.ViewerCandidateKey;
import com.databasepreservation.common.client.models.structure.ViewerCheckConstraint;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerPrimaryKey;
import com.databasepreservation.common.client.models.structure.ViewerPrivilegeStructure;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerRoleStructure;
import com.databasepreservation.common.client.models.structure.ViewerRoutine;
import com.databasepreservation.common.client.models.structure.ViewerRoutineParameter;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerTrigger;
import com.databasepreservation.common.client.models.structure.ViewerUserStructure;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.filter.solr.TermsFilterParameter;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.schema.SolrRowsCollectionRegistry;
import com.databasepreservation.common.server.index.schema.collections.RowsCollection;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SolrUtils {
  public static final String COMMON = "common";
  public static final String CONF = "conf";
  public static final String SCHEMA = "managed-schema.xml";
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);
  private static final String DEFAULT_QUERY_PARSER_OPERATOR = "AND";
  private static final Set<String> NON_REPEATABLE_FIELDS = new HashSet<>(Arrays.asList(RodaConstants.AIP_TITLE,
    RodaConstants.AIP_LEVEL, RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL));
  private static Map<String, List<String>> liteFieldsForEachClass = new HashMap<>();

  private SolrUtils() {
    // do nothing
  }

  public static String randomUUID() {
    return UUID.randomUUID().toString();
  }

  public static String UUIDFromString(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
  }

  private static <T> String getHumanFriendlyName(Class<T> resultClass) throws GenericException {
    String humanFriendlyName = ViewerConstants.UNKNOWN;
    if (resultClass.equals(ViewerDatabase.class)) {
      humanFriendlyName = "database";
    } else if (resultClass.equals(SavedSearch.class)) {
      humanFriendlyName = "saved search";
    } else if (resultClass.equals(ViewerRow.class)) {
      humanFriendlyName = "row";
    }
    return humanFriendlyName;
  }

  private static void applyFiltersToQuery(SolrQuery query, Filter mainFilter, List<Filter> filterQueries)
    throws RequestNotValidException {
    Filter qFilter = new Filter();
    List<Filter> fqFilters = new ArrayList<>();

    if (filterQueries != null) {
      fqFilters.addAll(filterQueries);
    }

    if (mainFilter != null && mainFilter.getParameters() != null) {
      for (FilterParameter param : mainFilter.getParameters()) {
        String paramString = parseFilter(new Filter(param));

        if (paramString.contains("{!") || paramString.contains("_query_:")) {
          fqFilters.add(new Filter(param));
        } else {
          qFilter.add(param);
        }
      }
    }

    query.setQuery(parseFilter(qFilter));

    List<String> parsedFilterQueries = new ArrayList<>();
    for (Filter fq : fqFilters) {
      parsedFilterQueries.add(parseFilter(fq));
    }
    if (!parsedFilterQueries.isEmpty()) {
      query.setFilterQueries(parsedFilterQueries.toArray(new String[0]));
    }
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, SolrCollection<T> collection, Filter filter,
    Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    return find(index, collection, filter, sorter, sublist, Facets.NONE, new ArrayList<>(), new HashMap<>());
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, SolrCollection<T> collection, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, List<String> fieldsToReturn, Map<String, String> extraParameters)
    throws GenericException, RequestNotValidException {
    return find(index, collection, filter, sorter, sublist, facets, fieldsToReturn, extraParameters, new ArrayList<>(),
      "lucene", List.of(), false, List.of());
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, SolrCollection<T> collection, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, List<String> fieldsToReturn, Map<String, String> extraParameters,
    List<Filter> filterQueries, String defType, List<String> queryFields, boolean highlighting,
    List<String> highlightedFields) throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();

    applyFiltersToQuery(query, filter, filterQueries);

    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());

    if (!extraParameters.isEmpty()) {
      List<String> extraFields = new ArrayList<>();
      for (Map.Entry<String, String> entry : extraParameters.entrySet()) {
        query.setParam(entry.getKey(), entry.getValue());
        extraFields.add(entry.getKey());
      }
    }

    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[0]));
    }

    if (defType != null) {
      query.setParam("defType", defType);
      if (defType.equals(ViewerConstants.SOLR_EDISMAX) && queryFields != null) {
        query.setParam("qf", String.join(" ", queryFields));
      }
      query.setParam("hl", highlighting);
      if (highlighting) {
        query.setParam("hl.fl", (highlightedFields != null) ? String.join(" ", highlightedFields) : "");

        String hlQ = buildHighlightQuery(filter, highlightedFields);
        if (StringUtils.isNotBlank(hlQ)) {
          query.setParam("hl.q", hlQ);
          query.setParam("hl.highlightMultiTerm", "true");
          if (defType.equals(ViewerConstants.SOLR_EDISMAX)) {
            query.setParam("hl.requireFieldMatch", "false");
          } else {
            query.setParam("hl.requireFieldMatch", "true");
          }
        }

        query.setParam("hl.tag.pre", ViewerConfiguration.getInstance().getViewerConfigurationAsString("<b>",
          ViewerConstants.PROPERTY_SEARCH_HIGHLIGHT_TAG_PRE));
        query.setParam("hl.tag.post", ViewerConfiguration.getInstance().getViewerConfigurationAsString("</b>",
          ViewerConstants.PROPERTY_SEARCH_HIGHLIGHT_TAG_POST));
        query.setParam("hl.encoder", "html");
        query.setParam("hl.fragsize", ViewerConfiguration.getInstance().getViewerConfigurationAsString("50",
          ViewerConstants.PROPERTY_SEARCH_HIGHLIGHT_FRAGSIZE));
        query.setParam("hl.maxAnalyzedChars", ViewerConfiguration.getInstance().getViewerConfigurationAsString("51200",
          ViewerConstants.PROPERTY_SEARCH_HIGHLIGHT_MAX_ANALYZED_CHARS));
      }
    }

    parseAndConfigureFacets(facets, query);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query, SolrRequest.METHOD.POST);
      ret = queryResponseToIndexResult(response, collection, facets);
    } catch (SolrException e) {
      boolean shouldReturnEmptyResult = (e.code() == 404);
      // there may be other cases where an empty result should be returned
      if (shouldReturnEmptyResult) {
        // build an empty IndexedResult
        final SolrDocumentList docList = new SolrDocumentList();
        final List<FacetFieldResult> facetResults = processFacetFields(facets, null);
        final long offset = docList.getStart();
        final long limit = docList.size();
        final long totalCount = docList.getNumFound();
        final List<T> docs = new ArrayList<T>();
        final Map<String, Map<String, List<String>>> highlightingInfo = new HashMap<>();
        ret = new IndexResult<T>(offset, limit, totalCount, docs, facetResults, highlightingInfo);
      } else {
        throw buildGenericException(e);
      }
    } catch (SolrServerException | IOException e) {
      throw buildGenericException(e);
    }

    return ret;
  }

  public static <T extends IsIndexed> IndexResult<T> findHits(SolrClient index, SolrCollection<T> collection,
    String alias, Filter filter, Sorter sorter, Sublist sublist, Facets facets, String defType,
    List<String> queryFields) throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();

    applyFiltersToQuery(query, filter, null);

    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(0);

    parseAndConfigureFacets(facets, query);

    query.setParam("defType", defType);
    query.setParam("qf", String.join(" ", queryFields));

    try {
      QueryRequest request = new QueryRequest(query);
      request.setMethod(SolrRequest.METHOD.POST);

      NamedList<Object> namedList = index.request(request, alias);
      QueryResponse response = new QueryResponse();
      response.setResponse(namedList);

      ret = queryResponseToIndexResult(response, collection, facets);
    } catch (SolrException e) {
      boolean shouldReturnEmptyResult = (e.code() == 404);
      // there may be other cases where an empty result should be returned
      if (shouldReturnEmptyResult) {
        // build an empty IndexedResult
        final SolrDocumentList docList = new SolrDocumentList();
        final List<FacetFieldResult> facetResults = processFacetFields(facets, null);
        final long offset = docList.getStart();
        final long limit = docList.size();
        final long totalCount = docList.getNumFound();
        final List<T> docs = new ArrayList<T>();
        final Map<String, Map<String, List<String>>> highlighting = new HashMap<>();
        ret = new IndexResult<T>(offset, limit, totalCount, docs, facetResults, highlighting);
      } else {
        throw buildGenericException(e);
      }
    } catch (SolrServerException | IOException e) {
      throw buildGenericException(e);
    }

    return ret;
  }

  public static IndexResult<ViewerRow> findRows(SolrClient index, String databaseUUID, Filter filter, Sorter sorter,
    Sublist sublist) throws GenericException, RequestNotValidException {
    return findRows(index, databaseUUID, filter, sorter, sublist, Facets.NONE);
  }

  public static IndexResult<ViewerRow> findRows(SolrClient index, String databaseUUID, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    return find(index, SolrRowsCollectionRegistry.get(databaseUUID), filter, sorter, sublist, facets, new ArrayList<>(),
      new HashMap<>());
  }

  public static IndexResult<ViewerRow> findRows(SolrClient index, String databaseUUID, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, List<String> fieldsToReturn, Map<String, String> extraParameters)
    throws GenericException, RequestNotValidException {
    return find(index, SolrRowsCollectionRegistry.get(databaseUUID), filter, sorter, sublist, facets, fieldsToReturn,
      extraParameters);
  }

  public static IndexResult<ViewerRow> findRows(SolrClient index, String databaseUUID, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, List<String> fieldsToReturn, Map<String, String> extraParameters, String defType,
    Filter filterQuery, List<String> queryFields, boolean highlighting, List<String> highlightedFields)
    throws GenericException, RequestNotValidException {
    return find(index, SolrRowsCollectionRegistry.get(databaseUUID), filter, sorter, sublist, facets, fieldsToReturn,
      extraParameters, (filterQuery != null) ? List.of(filterQuery) : Collections.emptyList(), defType, queryFields,
      highlighting, highlightedFields);
  }

  public static Pair<IndexResult<ViewerRow>, String> findRows(SolrClient index, String databaseUUID, Filter filter,
    Sorter sorter, int pageSize, String cursorMark, List<String> fieldsToReturn)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findRows(index, databaseUUID, filter, sorter, pageSize, cursorMark, fieldsToReturn,
      new HashMap<>());
  }

  public static <T extends IsIndexed> Pair<IndexResult<T>, String> find(SolrClient index, Class<T> classToRetrieve,
    Filter filter, Sorter sorter, int pageSize, String cursorMark, List<String> fieldsToReturn,
    Map<String, String> extraParameters) throws RequestNotValidException, GenericException {
    return find(index, classToRetrieve, filter, sorter, pageSize, cursorMark, fieldsToReturn, extraParameters,
      new ArrayList<>());
  }

  public static <T extends IsIndexed> Pair<IndexResult<T>, String> find(SolrClient index, Class<T> classToRetrieve,
    Filter filter, Sorter sorter, int pageSize, String cursorMark, List<String> fieldsToReturn,
    Map<String, String> extraParameters, List<Filter> filterQueries) throws RequestNotValidException, GenericException {
    Pair<IndexResult<T>, String> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);

    applyFiltersToQuery(query, filter, filterQueries);

    query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    query.setRows(pageSize);
    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);

    if (!extraParameters.isEmpty()) {
      List<String> extraFields = new ArrayList<>();
      for (Map.Entry<String, String> entry : extraParameters.entrySet()) {
        query.setParam(entry.getKey(), entry.getValue());
        extraFields.add(entry.getKey());
      }
    }

    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[0]));
    }

    try {
      QueryResponse response = index.query(SolrDefaultCollectionRegistry.get(classToRetrieve).getIndexName(), query,
        SolrRequest.METHOD.POST);
      IndexResult<T> result = queryResponseToIndexResult(response, SolrDefaultCollectionRegistry.get(classToRetrieve),
        Facets.NONE);
      ret = Pair.of(result, response.getNextCursorMark());
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  public static Pair<IndexResult<ViewerRow>, String> findRows(SolrClient index, String databaseUUID, Filter filter,
    Sorter sorter, int pageSize, String cursorMark, List<String> fieldsToReturn, Map<String, String> extraParameters)
    throws GenericException, RequestNotValidException {

    Pair<IndexResult<ViewerRow>, String> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);

    applyFiltersToQuery(query, filter, null);

    query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    query.setRows(pageSize);
    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);

    if (!extraParameters.isEmpty()) {
      List<String> extraFields = new ArrayList<>();
      for (Map.Entry<String, String> entry : extraParameters.entrySet()) {
        query.setParam(entry.getKey(), entry.getValue());
        extraFields.add(entry.getKey());
      }
    }

    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[0]));
    }

    final RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query, SolrRequest.METHOD.POST);
      final IndexResult<ViewerRow> result = queryResponseToIndexResult(response, collection, Facets.NONE);
      ret = Pair.of(result, response.getNextCursorMark());
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  public static Pair<IndexResult<ViewerRow>, String> findRows(SolrClient index, String databaseUUID, Filter filter,
    Sorter sorter, int pageSize, String cursorMark, List<String> fieldsToReturn, Map<String, String> extraParameters,
    Filter filterQuery, String defType, List<String> queryFields) throws GenericException, RequestNotValidException {

    Pair<IndexResult<ViewerRow>, String> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);

    applyFiltersToQuery(query, filter, List.of(filterQuery));

    query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    query.setRows(pageSize);
    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);

    if (!extraParameters.isEmpty()) {
      List<String> extraFields = new ArrayList<>();
      for (Map.Entry<String, String> entry : extraParameters.entrySet()) {
        query.setParam(entry.getKey(), entry.getValue());
        extraFields.add(entry.getKey());
      }
    }

    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[0]));
    }

    query.setParam("defType", defType);
    query.setParam("qf", (queryFields != null) ? String.join(" ", queryFields) : "");

    final RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query, SolrRequest.METHOD.POST);
      final IndexResult<ViewerRow> result = queryResponseToIndexResult(response, collection, Facets.NONE);
      ret = Pair.of(result, response.getNextCursorMark());
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  public static Pair<IndexResult<ViewerRow>, String> findRows(SolrClient index, String databaseUUID, SolrQuery query,
    Sorter sorter, int pageSize, String cursorMark) throws GenericException, RequestNotValidException {

    Pair<IndexResult<ViewerRow>, String> ret;

    query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    query.setRows(pageSize);
    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);

    final RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query, SolrRequest.METHOD.POST);
      final IndexResult<ViewerRow> result = queryResponseToIndexResult(response, collection, Facets.NONE);
      ret = Pair.of(result, response.getNextCursorMark());
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  public static SolrQuery buildQuery(Filter filter, List<String> fieldsToReturn) throws RequestNotValidException {
    SolrQuery query = new SolrQuery();

    query.setQuery(parseFilter(filter));
    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[0]));
    }

    return query;
  }

  public static IndexResult<ViewerRow> findRowsWithSubQuery(SolrClient index, String databaseUUID,
    List<SolrQuery> queryList) throws GenericException, RequestNotValidException {
    IndexResult<ViewerRow> ret;

    SolrQuery query = new SolrQuery();
    String nestedPath = "";
    for (int i = 0; i < queryList.size(); i++) {
      SolrQuery subquery = queryList.get(i);
      if (i == 0) {
        query.set("q", subquery.getQuery());
        if (subquery.getFields() != null && !subquery.getFields().isEmpty()) {
          query.set("fl", subquery.getFields());
        }
      } else {
        if (nestedPath.isEmpty()) {
          nestedPath = "nested";
        } else {
          nestedPath = nestedPath + ".nested";
        }
        query.set(nestedPath + ".q", subquery.getQuery());
        if (subquery.getFields() != null && !subquery.getFields().isEmpty()) {
          query.set(nestedPath + ".fl", subquery.getFields());
        }
      }
    }

    final RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query, SolrRequest.METHOD.POST);
      ret = queryResponseToIndexResult(response, collection, Facets.NONE);
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  public static List<SolrQuery.SortClause> parseSorter(Sorter sorter) {
    List<SolrQuery.SortClause> ret = new ArrayList<SolrQuery.SortClause>();
    if (sorter != null) {
      for (SortParameter sortParameter : sorter.getParameters()) {
        ret.add(new SolrQuery.SortClause(sortParameter.getName(),
          sortParameter.isDescending() ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc));
      }
    }
    return ret;
  }

  private static void parseAndConfigureFacets(Facets facets, SolrQuery query) {
    if (facets != null) {
      query.setFacetSort(FacetParams.FACET_SORT_COUNT);
      if (!"".equals(facets.getQuery())) {
        query.addFacetQuery(facets.getQuery());
      }
      StringBuilder filterQuery = new StringBuilder();
      for (Map.Entry<String, FacetParameter> parameter : facets.getParameters().entrySet()) {
        FacetParameter facetParameter = parameter.getValue();

        if (facetParameter instanceof SimpleFacetParameter) {
          setQueryFacetParameter(query, (SimpleFacetParameter) facetParameter);
          appendValuesUsingOROperator(filterQuery, facetParameter.getName(), facetParameter.getValues());
        } else if (facetParameter instanceof RangeFacetParameter) {
          LOGGER.error("Unsupported facet parameter class: {}", facetParameter.getClass().getName());
        } else {
          LOGGER.error("Unsupported facet parameter class: {}", facetParameter.getClass().getName());
        }
      }
      if (filterQuery.length() > 0) {
        query.addFilterQuery(filterQuery.toString());
        LOGGER.trace("Query after defining facets: " + query.toString());
      }
    }
  }

  private static void setQueryFacetParameter(SolrQuery query, SimpleFacetParameter facetParameter) {
    query.addFacetField(facetParameter.getName());
    query.add(String.format("f.%s.facet.mincount", facetParameter.getName()),
      String.valueOf(facetParameter.getMinCount()));
    query.add(String.format("f.%s.facet.limit", facetParameter.getName()), String.valueOf(facetParameter.getLimit()));
    query.add(String.format("f.%s.facet.offset", facetParameter.getName()), String.valueOf(facetParameter.getOffset()));
  }

  public static <T extends IsIndexed> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
    SolrCollection<T> collection, Facets facets) throws GenericException {
    return queryResponseToIndexResult(response, collection, null, facets);
  }

  public static <T extends IsIndexed> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
    SolrCollection<T> collection, List<String> columnNames, Facets facets) throws GenericException {
    final SolrDocumentList docList = response.getResults();
    final List<FacetFieldResult> facetResults = processFacetFields(facets, response.getFacetFields());
    final long offset = docList.getStart();
    final long limit = docList.size();
    final long totalCount = docList.getNumFound();
    final List<T> docs = new ArrayList<>();
    final Map<String, Map<String, List<String>>> highlightingInfo = response.getHighlighting();

    for (SolrDocument doc : docList) {
      T result;
      try {
        result = collection.fromSolrDocument(doc);
      } catch (ViewerException e) {
        throw new GenericException(e);
      }
      docs.add(result);
    }

    return new IndexResult<>(offset, limit, totalCount, docs, facetResults, highlightingInfo);
  }

  private static List<FacetFieldResult> processFacetFields(Facets facets, List<FacetField> facetFields) {
    List<FacetFieldResult> ret = new ArrayList<FacetFieldResult>();
    FacetFieldResult facetResult;
    if (facetFields != null) {
      for (FacetField facet : facetFields) {
        LOGGER.trace("facet:{} count:{}", facet.getName(), facet.getValueCount());
        facetResult = new FacetFieldResult(facet.getName(), facet.getValueCount(),
          facets.getParameters().get(facet.getName()).getValues());
        for (FacetField.Count count : facet.getValues()) {
          LOGGER.trace("   value:{} value:{}", count.getName(), count.getCount());
          facetResult.addFacetValue(count.getName(), count.getName(), count.getCount());
        }
        ret.add(facetResult);
      }
    }
    return ret;
  }

  public static <T extends IsIndexed> Long count(SolrClient index, SolrCollection<T> collection, Filter filter)
    throws GenericException, RequestNotValidException {
    return find(index, collection, filter, null, new Sublist(0, 0)).getTotalCount();
  }

  public static Long countRows(SolrClient index, String databaseUUID, Filter filter)
    throws GenericException, RequestNotValidException {
    return findRows(index, databaseUUID, filter, null, new Sublist(0, 0)).getTotalCount();
  }

  public static <T extends IsIndexed> T retrieve(SolrClient index, SolrCollection<T> collection, String id)
    throws NotFoundException, GenericException {
    T ret;
    Class<T> classToRetrieve = collection.getObjectClass();
    try {
      Map<String, String> param = new HashMap<>();
      param.put("fl", "*, [child]");
      SolrParams params = new MapSolrParams(param);
      SolrDocument doc = index.getById(collection.getIndexName(), id, params);
      if (doc != null) {
        try {
          ret = collection.fromSolrDocument(doc);
        } catch (ViewerException e) {
          throw new GenericException("Could not get " + getHumanFriendlyName(classToRetrieve) + " from Solr document");
        }
      } else {
        throw new NotFoundException("Could not find " + getHumanFriendlyName(classToRetrieve) + " " + id);
      }
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not retrieve " + classToRetrieve.getName() + " from index", e);
    } catch (SolrException e) {
      if (e.code() == 404) {
        throw new NotFoundException("Could not find " + getHumanFriendlyName(classToRetrieve) + " " + id);
      } else {
        throw new GenericException("Could not retrieve " + classToRetrieve.getName() + " from index", e);
      }
    }
    return ret;
  }

  public static ViewerRow retrieveRows(SolrClient index, String databaseUUID, String rowUUID)
    throws NotFoundException, GenericException {
    ViewerRow ret;
    Class<ViewerRow> classToRetrieve = ViewerRow.class;
    try {
      RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

      Map<String, String> param = new HashMap<>();
      param.put("fl", "*, [child]");
      SolrParams params = new MapSolrParams(param);
      SolrDocument doc = index.getById(collection.getIndexName(), rowUUID, params);
      if (doc != null) {
        try {
          ret = collection.fromSolrDocument(doc);
        } catch (ViewerException e) {
          throw new GenericException("Could not get " + getHumanFriendlyName(classToRetrieve) + " from Solr document");
        }
      } else {
        throw new NotFoundException("Could not find " + getHumanFriendlyName(classToRetrieve) + " " + rowUUID);
      }
    } catch (SolrServerException | IOException e) {
      throw new GenericException("Could not retrieve " + classToRetrieve.getName() + " from index", e);
    } catch (SolrException e) {
      if (e.code() == 404) {
        throw new NotFoundException("Could not find " + getHumanFriendlyName(classToRetrieve) + " " + rowUUID);
      } else {
        throw new GenericException("Could not retrieve " + classToRetrieve.getName() + " from index", e);
      }
    }
    return ret;
  }

  public static String getSolrQuery(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    // query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    // parseAndConfigureFacets(facets, query);

    return query.toQueryString();
  }

  public static String parseFilter(Filter filter) throws RequestNotValidException {
    StringBuilder ret = new StringBuilder();

    if (filter == null || filter.getParameters().isEmpty()) {
      ret.append("*:*");
    } else {
      for (FilterParameter parameter : filter.getParameters()) {
        parseFilterParameter(ret, parameter, true);
      }

      if (ret.length() == 0) {
        ret.append("*:*");
      }
    }

    LOGGER.trace("Converting filter {} to query {}", filter, ret);
    return ret.toString();
  }

  private static void parseFilterParameter(StringBuilder ret, FilterParameter parameter,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter simplePar = (SimpleFilterParameter) parameter;
      appendExactMatch(ret, simplePar.getName(), simplePar.getValue(), true, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof EDismaxSimplerQueryFilterParameter) {
      EDismaxSimplerQueryFilterParameter eDismaxSimplePar = (EDismaxSimplerQueryFilterParameter) parameter;
      appendEDismaxSearchTerm(ret, eDismaxSimplePar.getValue(), false, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof OneOfManyFilterParameter) {
      OneOfManyFilterParameter param = (OneOfManyFilterParameter) parameter;
      appendValuesUsingOROperator(ret, param.getName(), param.getValues(), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter param = (BasicSearchFilterParameter) parameter;
      appendBasicSearch(ret, param.getName(), param.getValue(), "AND", prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof BoostedSearchFilterParameter) {
      BoostedSearchFilterParameter param = (BoostedSearchFilterParameter) parameter;
      appendBoostedSearch(ret, param.getBoostedFilter(), param.getBoostFactor(),
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof EmptyKeyFilterParameter) {
      EmptyKeyFilterParameter param = (EmptyKeyFilterParameter) parameter;
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
      ret.append("(*:* NOT " + param.getName() + ":*)");
    } else if (parameter instanceof BlockJoinAnyParentExpiryFilterParameter) {
      BlockJoinAnyParentExpiryFilterParameter param = (BlockJoinAnyParentExpiryFilterParameter) parameter;
      appendExpiryParameter(ret, param);
    } else if (parameter instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter param = (DateRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Date.class, param.getFromValue(), String.class,
        processToDate(param.getToValue(), param.getGranularity(), false), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof DateIntervalFilterParameter) {
      DateIntervalFilterParameter param = (DateIntervalFilterParameter) parameter;
      appendRangeInterval(ret, param.getFromName(), param.getToName(), param.getFromValue(), param.getToValue(),
        param.getGranularity(), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter param = (LongRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Long.class, param.getFromValue(), Long.class, param.getToValue(),
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof NotSimpleFilterParameter) {
      NotSimpleFilterParameter notSimplePar = (NotSimpleFilterParameter) parameter;
      appendNotExactMatch(ret, notSimplePar.getName(), notSimplePar.getValue(), true,
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof OrFiltersParameters || parameter instanceof AndFiltersParameters) {
      FiltersParameters filters = (FiltersParameters) parameter;
      appendFiltersWithOperator(ret, parameter instanceof OrFiltersParameters ? "OR" : "AND", filters.getValues(),
        prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof TermsFilterParameter) {
      TermsFilterParameter param = (TermsFilterParameter) parameter;
      ret.append("({!terms f=" + param.getField() + " v=" + param.getParameterValue() + "})");
    } else if (parameter instanceof InnerJoinFilterParameter) {
      InnerJoinFilterParameter param = (InnerJoinFilterParameter) parameter;
      ret.append("{!join from=nestedOriginalUUID to=uuid }_root_:" + param.getRowUUID() + " AND nestedUUID:"
        + param.getNestedOriginalUUID());
    } else if (parameter instanceof CrossCollectionInnerJoinFilterParameter) {
      CrossCollectionInnerJoinFilterParameter param = (CrossCollectionInnerJoinFilterParameter) parameter;
      ret.append(
        "{!join method=crossCollection fromIndex=" + param.getFromIndex() + " from=nestedOriginalUUID to=uuid }_root_:"
          + param.getRowUUID() + " AND nestedUUID:" + param.getNestedOriginalUUID());
    } else if (parameter instanceof BlockJoinParentFilterParameter) {
      BlockJoinParentFilterParameter param = (BlockJoinParentFilterParameter) parameter;
      ret.append("+({!parent which='tableId:" + param.getParentTableId() + "' filters='nestedUUID:"
        + param.getNestedUUID() + "' }" + param.getSolrName() + ":" + param.getValue() + ")");
    } else {
      LOGGER.error("Unsupported filter parameter class: {}", parameter.getClass().getName());
      throw new RequestNotValidException("Unsupported filter parameter class: " + parameter.getClass().getName());
    }
  }

  private static void appendRangeInterval(StringBuilder ret, String fromKey, String toKey, Date fromValue, Date toValue,
    RodaConstants.DateGranularity granularity, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
      ret.append("(");

      ret.append(fromKey).append(":[");
      ret.append(processFromDate(fromValue));
      ret.append(" TO ");
      ret.append(processToDate(toValue, granularity));
      ret.append("]").append(" OR ");

      ret.append(toKey).append(":[");
      ret.append(processFromDate(fromValue));
      ret.append(" TO ");
      ret.append(processToDate(toValue, granularity));
      ret.append("]");

      if (fromValue != null && toValue != null) {
        ret.append(" OR ").append("(").append(fromKey).append(":[* TO ").append(processToDate(fromValue, granularity))
          .append("]");
        ret.append(" AND ").append(toKey).append(":[").append(processFromDate(toValue)).append(" TO *]").append(")");
      }

      ret.append(")");
    }
  }

  private static void appendExpiryParameter(StringBuilder ret, BlockJoinAnyParentExpiryFilterParameter param) {
    ret.append("{!parent which='*:* -_nest_path_:*'}(");
    if (param.getGroups().isEmpty()) {
      // impossible query if no groups are provided
      ret.append("-*:*");
    } else {
      for (int i = 0; i < param.getGroups().size(); i++) {
        if (i > 0) {
          ret.append(" OR ");
        }
        ret.append("(");
        ret.append("group_value:(" + param.getGroups().get(i) + ")");
        ret.append(" AND ");
        appendRange(ret, "expiry_date", Date.class, param.getFromValue(), Date.class, param.getToValue(), false);
        ret.append(") OR (group_value:(" + param.getGroups().get(i) + ") AND -expiry_date:*)");
      }
    }
    ret.append(")");
  }

  private static String processFromDate(Date fromValue) {
    final String ret;

    if (fromValue != null) {
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_NO_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

      // Instant instant = Instant.ofEpochMilli(fromValue.getTime());
      // return instant.toString();
      return format.format(fromValue);
    } else {
      ret = "*";
    }

    return ret;
  }

  private static String processToDate(Date toValue, RodaConstants.DateGranularity granularity) {
    return processToDate(toValue, granularity, true);
  }

  private static String processToDate(Date toValue, RodaConstants.DateGranularity granularity,
    boolean returnStartOnNull) {
    final String ret;
    StringBuilder sb = new StringBuilder();
    if (toValue != null) {
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_NO_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

      sb.append(format.format(toValue));
      // sb.append(Instant.ofEpochMilli(toValue.getTime()).toString());
      switch (granularity) {
        case YEAR:
          sb.append("+1YEAR-1MILLISECOND");
          break;
        case MONTH:
          sb.append("+1MONTH-1MILLISECOND");
          break;
        case DAY:
          sb.append("+1DAY-1MILLISECOND");
          break;
        case HOUR:
          sb.append("+1HOUR-1MILLISECOND");
          break;
        case MINUTE:
          sb.append("+1MINUTE-1MILLISECOND");
          break;
        case SECOND:
          sb.append("+1SECOND-1MILLISECOND");
          break;
        default:
          // do nothing
          break;
      }
      ret = sb.toString();
    } else {
      ret = returnStartOnNull ? "*" : null;
    }
    return ret;
  }

  private static void appendNotExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendExactMatch(ret, "*:* -" + key, value, appendDoubleQuotes, prefixWithANDOperatorIfBuilderNotEmpty);
  }

  private static <T extends Serializable> void generateRangeValue(StringBuilder ret, Class<T> valueClass, T value) {
    if (value != null) {
      if (valueClass.equals(Date.class)) {
        SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_NO_MILLIS);
        format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        final String date = format.format((Date) value);
        LOGGER.trace("Appending date value \"{}\" to range", date);
        ret.append(date);
      } else if (valueClass.equals(Long.class)) {
        ret.append((Long) value);
      } else if (valueClass.equals(String.class)) {
        ret.append((String) value);
      } else {
        LOGGER.error("Cannot process range of the type {}", valueClass);
      }
    } else {
      ret.append("*");
    }
  }

  private static <T extends Serializable, T1 extends Serializable> void appendRange(StringBuilder ret, String key,
    Class<T> fromClass, T fromValue, Class<T1> toClass, T1 toValue, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("(").append(key).append(":[");
      generateRangeValue(ret, fromClass, fromValue);
      ret.append(" TO ");
      generateRangeValue(ret, toClass, toValue);
      ret.append("])");
    }
  }

  private static void appendANDOperator(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty && ret.length() > 0) {
      ret.append(" AND ");
    }
  }

  private static void appendOROperator(StringBuilder ret, boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (prefixWithANDOperatorIfBuilderNotEmpty && ret.length() > 0) {
      ret.append(" OR ");
    }
  }

  private static void appendValuesUsingOROperator(StringBuilder ret, String key, List<String> values) {
    if (!values.isEmpty()) {
      appendANDOperator(ret, true);

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" OR ");
        }
        appendExactMatch(ret, key, values.get(i), true, false);
      }
      ret.append(")");
    }
  }

  private static void appendValuesUsingOROperator(StringBuilder ret, String key, List<String> values,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (!values.isEmpty()) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" OR ");
        }
        appendExactMatch(ret, key, values.get(i), true, false);
      }
      ret.append(")");
    }
  }

  private static void appendExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
    ret.append("( ").append(key).append(":");
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(value.replaceAll("(\")", "\\\\$1"));
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(" )");
  }

  private static void appendEDismaxSearchTerm(StringBuilder ret, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
    ret.append("(");
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(value);
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(")");
  }

  private static void appendBasicSearch(StringBuilder ret, String key, String value, String operator,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    if (StringUtils.isBlank(value)) {
      appendExactMatch(ret, key, "*", false, prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (value.matches("^\".+\"$")) {
      appendExactMatch(ret, key, value.substring(1, value.length() - 1), true, prefixWithANDOperatorIfBuilderNotEmpty);
    } else {
      appendWhiteSpaceTokenizedString(ret, key, value, operator, prefixWithANDOperatorIfBuilderNotEmpty);
    }
  }

  private static void appendBoostedSearch(StringBuilder ret, FilterParameter boostedFilter, float boostFactor,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

    ret.append("(");
    parseFilterParameter(ret, boostedFilter, false);
    ret.append(")^");
    ret.append(boostFactor);
  }

  private static void appendFiltersWithOperator(StringBuilder ret, String operator, List<FilterParameter> values,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) throws RequestNotValidException {
    if (!values.isEmpty()) {
      appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" ").append(operator).append(" ");
        }
        parseFilterParameter(ret, values.get(i), false);
      }
      ret.append(")");
    }
  }

  private static void appendWhiteSpaceTokenizedString(StringBuilder ret, String key, String value, String operator,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);

    boolean isParent = key != null && key.startsWith("{!parent");
    String parentTag = "";
    String fieldName = key;

    if (isParent) {
      int closeIdx = key.indexOf('}');
      if (closeIdx > 0) {
        parentTag = key.substring(0, closeIdx + 1);
        fieldName = key.substring(closeIdx + 1);
      }

      ret.append("_query_:\"");
      ret.append(parentTag);
      ret.append(fieldName).append(":(");
    } else {
      ret.append("(");
    }

    String[] split = value.trim().split("\\s+");
    for (int i = 0; i < split.length; i++) {
      if (i != 0 && operator != null) {
        ret.append(" ").append(operator).append(" ");
      }

      if (split[i].matches("(AND|OR|NOT)")) {
        if (isParent) {
          ret.append(split[i]);
        } else {
          ret.append(fieldName).append(":\"").append(split[i]).append("\"");
        }
      } else {
        if (isParent) {
          ret.append(escapeSolrSpecialChars(split[i]));
        } else {
          ret.append(fieldName).append(":(").append(escapeSolrSpecialChars(split[i])).append(")");
        }
      }
    }

    if (isParent) {
      ret.append(")\"");
    } else {
      ret.append(")");
    }
  }

  public static Map<String, Object> asValueUpdate(Object value) {
    Map<String, Object> fieldModifier = new HashMap<>(1);
    // 20160511 this workaround fixes solr wrong behaviour with partial update
    // of empty lists
    if (value instanceof List && ((List<?>) value).isEmpty()) {
      value = null;
    }
    fieldModifier.put("set", value);
    return fieldModifier;
  }

  public static Map<String, Object> addValueUpdate(Object value) {
    Map<String, Object> fieldModifier = new HashMap<>(1);
    // 20160511 this workaround fixes solr wrong behaviour with partial update
    // of empty lists
    if (value instanceof List && ((List<?>) value).isEmpty()) {
      value = null;
    }
    fieldModifier.put("add", value);
    return fieldModifier;
  }

  public static Map<String, Object> setValueUpdate(Object value) {
    Map<String, Object> fieldModifier = new HashMap<>(1);
    // 20160511 this workaround fixes solr wrong behaviour with partial update
    // of empty lists
    if (value instanceof List && ((List<?>) value).isEmpty()) {
      value = null;
    }
    fieldModifier.put("set", value);
    return fieldModifier;
  }

  public static <T extends IsIndexed> void delete(SolrClient index, SolrCollection<T> collection, Filter filter)
    throws GenericException, RequestNotValidException {
    try {
      index.deleteByQuery(collection.getIndexName(), parseFilter(filter));
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not delete items", e);
    }
  }

  public static <T extends IsIndexed> void delete(SolrClient index, SolrCollection<T> collection, List<String> ids)
    throws GenericException {
    try {
      index.deleteById(collection.getIndexName(), ids);
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not delete items", e);
    }
  }

  /*
   * Roda user > Apache Solr filter query
   * ____________________________________________________________________________________________________________________
   */
  // private static String getFilterQueries(User user, boolean justActive) {
  //
  // StringBuilder fq = new StringBuilder();
  //
  // // TODO find a better way to define admin super powers
  // if (user != null && !user.getName().equals("admin")) {
  // fq.append("(");
  // String usersKey = RodaConstants.INDEX_PERMISSION_USERS_PREFIX +
  // Permissions.PermissionType.READ;
  // appendExactMatch(fq, usersKey, user.getId(), true, false);
  //
  // String groupsKey = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX +
  // Permissions.PermissionType.READ;
  // appendValuesUsingOROperatorForQuery(fq, groupsKey, new
  // ArrayList<>(user.getGroups()), true);
  //
  // fq.append(")");
  // }
  //
  // if (justActive) {
  // appendExactMatch(fq, RodaConstants.INDEX_STATE, AIPState.ACTIVE.toString(),
  // true, true);
  // }
  //
  // return fq.toString();
  // }

  private static void appendValuesUsingOROperatorForQuery(StringBuilder ret, String key, List<String> values,
    boolean prependWithOrIfNeeded) {
    if (!values.isEmpty()) {
      if (prependWithOrIfNeeded) {
        appendOROperator(ret, true);
      } else {
        appendANDOperator(ret, true);
      }

      ret.append("(");
      for (int i = 0; i < values.size(); i++) {
        if (i != 0) {
          ret.append(" OR ");
        }
        appendExactMatch(ret, key, values.get(i), true, false);
      }
      ret.append(")");
    }
  }

  private static GenericException buildGenericException(Exception e) {
    String error = "Could not query index";
    String message = e.getMessage();

    if (e.getCause() instanceof ConnectException) {
      error = "Could not connect to Solr";
    } else {
      String bodytag = "<body>";
      if (message != null && message.contains(bodytag)) {
        message = message.substring(message.indexOf(bodytag) + bodytag.length(), message.indexOf("</body>"));
        message = message.replaceAll("\\<[^>]*?>", "");
      }
    }
    return new GenericException(error + ", message: " + message, e);
  }

  /*
   *
   * -----------------------------------------------------------------------------
   * bom a partir daqui
   * -----------------------------------------------------------------------------
   * 
   */

  /*
   * "Internal" helper methods
   * ____________________________________________________________________________________________________________________
   */

  /**
   * Method that knows how to escape characters for Solr
   * <p>
   * <code>+ - && || ! ( ) { } [ ] ^ " ~ * ? : /</code>
   * </p>
   * <p>
   * Note: chars <code>'*'</code> are not being escaped on purpose
   * </p>
   *
   * @return a string with special characters escaped
   */
  // FIXME perhaps && and || are not being properly escaped: see how to do it
  public static String escapeSolrSpecialChars(String string) {
    return string.replaceAll("([+&|!(){}\\[\\-\\]\\^\\\\~:\"/])", "\\\\$1");
  }

  public static List<String> objectToListString(Object object) {
    List<String> ret;
    if (object == null) {
      ret = new ArrayList<>();
    } else if (object instanceof String) {
      List<String> l = new ArrayList<>();
      l.add((String) object);
      return l;
    } else if (object instanceof List<?>) {
      List<?> l = (List<?>) object;
      ret = new ArrayList<>();
      for (Object o : l) {
        ret.add(o.toString());
      }
    } else {
      LOGGER.error("Could not convert Solr object to List<String> ({})", object.getClass().getName());
      ret = new ArrayList<>();
    }
    return ret;
  }

  public static Integer objectToInteger(Object object, Integer defaultValue) {
    Integer ret = defaultValue;
    if (object != null) {
      if (object instanceof Integer) {
        ret = (Integer) object;
      } else if (object instanceof String) {
        try {
          ret = Integer.parseInt((String) object);
        } catch (NumberFormatException e) {
          LOGGER.error("Could not convert Solr object to integer", e);
        }
      } else {
        LOGGER.error("Could not convert Solr object to integer ({})", object.getClass().getName());
      }
    }

    return ret;
  }

  public static Long objectToLong(Object object, Long defaultValue) {
    Long ret = defaultValue;
    if (object != null) {
      if (object instanceof Long) {
        ret = (Long) object;
      } else if (object instanceof String) {
        try {
          ret = Long.parseLong((String) object);
        } catch (NumberFormatException e) {
          LOGGER.error("Could not convert Solr object to long", e);
        }
      } else {
        LOGGER.error("Could not convert Solr object to long ({})", object.getClass().getName());
      }
    }
    return ret;
  }

  public static Float objectToFloat(Object object) {
    Float ret;
    if (object instanceof Float) {
      ret = (Float) object;
    } else if (object instanceof String) {
      try {
        ret = Float.parseFloat((String) object);
      } catch (NumberFormatException e) {
        LOGGER.error("Could not convert Solr object to float", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to float ({})", object.getClass().getName());
      ret = null;
    }
    return ret;
  }

  public static Date parseDate(String date) throws ParseException {
    Date ret;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // TemporalAccessor temporal = DateTimeFormatter.ISO_INSTANT.parse(date);
      // Instant instant = Instant.from(temporal);
      // ret = Date.from(instant);

      // TODO change to the former only when GWT supports Instant

      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_NO_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.parse(date);
    } else {
      ret = null;
    }
    return ret;
  }

  public static Date parseDateWithMillis(String date) throws ParseException {
    Date ret;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // TemporalAccessor temporal = DateTimeFormatter.ISO_INSTANT.parse(date);
      // Instant instant = Instant.from(temporal);
      // ret = Date.from(instant);

      // TODO change to the former only when GWT supports Instant

      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_WITH_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.parse(date);
    } else {
      ret = null;
    }
    return ret;
  }

  public static String formatDate(Date date) {
    String ret = null;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
      // TODO change to former only when GWT supports Instant
      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_NO_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.format(date);
    }
    return ret;
  }

  public static String formatDateWithMillis(Date date) {
    String ret = null;
    if (date != null) {
      // XXX with java.time (not working for 1213-01-01T00:00:00Z)
      // return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
      // TODO change to former only when GWT supports Instant
      // with fixed date parser
      SimpleDateFormat format = new SimpleDateFormat(RodaConstants.ISO8601_WITH_MILLIS);
      format.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
      ret = format.format(date);
    }
    return ret;
  }

  public static Instant parseInstant(String instant) {
    return Instant.from((DateTimeFormatter.ISO_INSTANT.parse(instant)));
  }

  public static String formatInstant(Instant instant) {
    return DateTimeFormatter.ISO_INSTANT.format(instant);
  }

  public static Instant dateToInstant(Date date) {
    return date != null ? date.toInstant() : null;
  }

  public static Date objectToDate(Object object) {
    Date ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof Date) {
      ret = (Date) object;
    } else if (object instanceof String) {
      try {
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = parseDate((String) object);
      } catch (ParseException e) {
        LOGGER.error("Could not convert Solr object to date", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to date, unsupported class: {}", object.getClass().getName());
      ret = null;
    }

    return ret;
  }

  public static Date objectToDateWithMillis(Object object) {
    Date ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof Date) {
      ret = (Date) object;
    } else if (object instanceof String) {
      try {
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = parseDateWithMillis((String) object);
      } catch (ParseException e) {
        LOGGER.error("Could not convert Solr object to date", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to date, unsupported class: {}", object.getClass().getName());
      ret = null;
    }

    return ret;
  }

  public static Instant objectToInstant(Object object) {
    Instant ret;
    if (object == null) {
      ret = null;
    } else if (object instanceof Instant) {
      ret = (Instant) object;
    } else if (object instanceof String) {
      try {
        LOGGER.trace("Parsing date ({}) from string", object);
        ret = parseInstant((String) object);
      } catch (DateTimeParseException e) {
        LOGGER.error("Could not convert Solr object to date", e);
        ret = null;
      }
    } else {
      LOGGER.error("Could not convert Solr object to date, unsupported class: {}", object.getClass().getName());
      ret = null;
    }

    return ret;
  }

  public static Boolean objectToBoolean(Object object, Boolean defaultValue) {
    Boolean ret = defaultValue;
    if (object != null) {
      if (object instanceof Boolean) {
        ret = (Boolean) object;
      } else if (object instanceof String) {
        ret = Boolean.parseBoolean((String) object);
      } else {
        LOGGER.error("Could not convert Solr object to Boolean ({})", object.getClass().getName());
      }
    }
    return ret;
  }

  public static String objectToString(Object object, String defaultValue) {
    String ret = defaultValue;
    if (object != null) {
      if (object instanceof String) {
        ret = (String) object;
      } else if (object instanceof java.util.List) {
        java.util.List<?> list = (java.util.List<?>) object;
        ret = list.isEmpty() ? defaultValue : String.valueOf(list.get(0));
      } else {
        LOGGER.warn("Could not convert Solr object to string, unsupported class: {}", object.getClass().getName());
      }
    }
    return ret;
  }

  public static Boolean getSolrBooleanValue(String originalValue) {
    return originalValue != null
      && (originalValue.startsWith("1") || originalValue.startsWith("t") || originalValue.startsWith("T"));
  }

  public static <E extends Enum<E>> String formatEnum(E enumValue) {
    return enumValue.name();
  }

  public static <E extends Enum<E>> E parseEnum(Class<E> enumeration, String enumValue) {
    return Enum.valueOf(enumeration, enumValue);
  }

  public static <E extends Enum<E>> E objectToEnum(Object object, Class<E> enumeration, E defaultValue) {
    E ret = defaultValue;
    if (object != null) {
      if (object instanceof String) {
        String name = (String) object;
        try {
          ret = parseEnum(enumeration, name);
        } catch (IllegalArgumentException e) {
          LOGGER.warn("Invalid name for enumeration: {}, name: {}", enumeration.getName(), name);
        } catch (NullPointerException e) {
          LOGGER.warn("Error parsing enumeration: {}, name: {}", enumeration.getName(), name);
        }
      } else {
        LOGGER.warn("Could not convert Solr object to enumeration: {}, unsupported class: {}", enumeration.getName(),
          object.getClass().getName());
      }
    }
    return ret;
  }

  public static Map<String, AuthorizationDetails> objectToDatabasePermissions(Object object) {
    Map<String, AuthorizationDetails> ret = new HashMap<>();

    if (object != null) {
      if (object instanceof SolrDocument doc) {
        String group = objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PERMISSIONS_GROUP), null);
        if (group != null) {
          AuthorizationDetails authorizationDetails = new AuthorizationDetails();
          authorizationDetails.setExpiry(objectToDate(doc.get(ViewerConstants.SOLR_DATABASES_PERMISSIONS_EXPIRY)));
          ret.put(group, authorizationDetails);
        }
      } else {
        List<SolrDocument> documents = (List<SolrDocument>) object;
        documents.forEach(doc -> {
          String group = objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PERMISSIONS_GROUP), null);
          if (group != null) {
            AuthorizationDetails authorizationDetails = new AuthorizationDetails();
            authorizationDetails.setExpiry(objectToDate(doc.get(ViewerConstants.SOLR_DATABASES_PERMISSIONS_EXPIRY)));
            ret.put(group, authorizationDetails);
          }
        });
      }
    }

    return ret;
  }

  public static ViewerSchema documentToSchema(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerSchema schema = new ViewerSchema();
    schema.setUuid(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_SCHEMA_UUID), null));
    schema.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_SCHEMA_NAME), null));
    schema.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_SCHEMA_DESCRIPTION), null));
    schema.setFolder(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_SCHEMA_FOLDER), null));

    List<ViewerTable> tables = new ArrayList<>();
    List<ViewerView> views = new ArrayList<>();
    List<ViewerRoutine> routines = new ArrayList<>();

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TABLES)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TABLES) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TABLES)) {
        if (child instanceof SolrDocument) {
          tables.add(documentToTable((SolrDocument) child));
        }
      }
    }

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_VIEWS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_VIEWS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_VIEWS)) {
        if (child instanceof SolrDocument) {
          views.add(documentToView((SolrDocument) child));
        }
      }
    }

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROUTINES)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROUTINES) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROUTINES)) {
        if (child instanceof SolrDocument) {
          routines.add(documentToRoutine((SolrDocument) child));
        }
      }
    }

    schema.setTables(tables);
    schema.setViews(views);
    schema.setRoutines(routines);

    return schema;
  }

  public static ViewerTable documentToTable(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerTable table = new ViewerTable();
    table.setUuid(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_UUID), null));
    table.setId(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_ID), null));
    table.setSourceType(objectToEnum(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_SOURCE_TYPE), ViewerSourceType.class,
      ViewerSourceType.NATIVE));
    table.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_NAME), null));
    table.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_DESCRIPTION), null));
    table.setFolder(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_FOLDER), null));
    table.setCountRows(objectToLong(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_ROWS), 0L));
    table.setSchemaUUID(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_SCHEMA_UUID), null));
    table.setSchemaName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_SCHEMA_NAME), null));
    table.setNameWithoutPrefix(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_NAME_WITHOUT_PREFIX), null));
    table.setCustomView(objectToBoolean(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_CUSTOM_VIEW), false));
    table.setMaterializedView(objectToBoolean(doc.get(ViewerConstants.SOLR_DATABASES_TABLE_MATERIALIZED_VIEW), false));

    List<ViewerColumn> columns = new ArrayList<>();
    List<ViewerForeignKey> fks = new ArrayList<>();
    List<ViewerCandidateKey> cks = new ArrayList<>();
    List<ViewerCheckConstraint> checks = new ArrayList<>();
    List<ViewerTrigger> triggers = new ArrayList<>();

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMNS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMNS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMNS)) {
        if (child instanceof SolrDocument) {
          columns.add(documentToColumn((SolrDocument) child));
        }
      }
    }

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEYS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEYS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEYS)) {
        if (child instanceof SolrDocument) {
          table.setPrimaryKey(documentToPrimaryKey((SolrDocument) child));
        }
      }
    }

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEYS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEYS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEYS)) {
        if (child instanceof SolrDocument) {
          fks.add(documentToForeignKey((SolrDocument) child));
        }
      }
    }

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEYS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEYS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEYS)) {
        if (child instanceof SolrDocument) {
          cks.add(documentToCandidateKey((SolrDocument) child));
        }
      }
    }

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINTS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINTS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINTS)) {
        if (child instanceof SolrDocument) {
          checks.add(documentToCheckConstraint((SolrDocument) child));
        }
      }
    }

    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TRIGGERS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TRIGGERS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TRIGGERS)) {
        if (child instanceof SolrDocument) {
          triggers.add(documentToTrigger((SolrDocument) child));
        }
      }
    }

    table.setColumns(columns);
    table.setForeignKeys(fks);
    table.setCandidateKeys(cks);
    table.setCheckConstraints(checks);
    table.setTriggers(triggers);

    return table;
  }

  public static ViewerView documentToView(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerView view = new ViewerView();
    view.setUuid(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_VIEW_UUID), null));
    view.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_VIEW_NAME), null));
    view.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_VIEW_DESCRIPTION), null));
    view.setQuery(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_VIEW_QUERY), null));
    view.setQueryOriginal(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_VIEW_QUERY_ORIGINAL), null));
    view.setSchemaUUID(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_VIEW_SCHEMA_UUID), null));
    view.setSchemaName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_VIEW_SCHEMA_NAME), null));

    List<ViewerColumn> columns = new ArrayList<>();
    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMNS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMNS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMNS)) {
        if (child instanceof SolrDocument) {
          columns.add(documentToColumn((SolrDocument) child));
        }
      }
    }

    view.setColumns(columns);
    return view;
  }

  public static ViewerRoutine documentToRoutine(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerRoutine routine = new ViewerRoutine();
    routine.setUuid(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROUTINE_UUID), null));
    routine.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROUTINE_NAME), null));
    routine.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROUTINE_DESCRIPTION), null));
    routine.setSource(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROUTINE_SOURCE), null));
    routine.setBody(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROUTINE_BODY), null));
    routine.setCharacteristic(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROUTINE_CHARACTERISTIC), null));
    routine.setReturnType(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROUTINE_RETURN_TYPE), null));

    List<ViewerRoutineParameter> params = new ArrayList<>();
    if (doc.containsKey(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PARAMETERS)
      && doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PARAMETERS) != null) {
      for (Object child : doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PARAMETERS)) {
        if (child instanceof SolrDocument) {
          params.add(documentToParameter((SolrDocument) child));
        }
      }
    }

    routine.setParameters(params);
    return routine;
  }

  public static ViewerColumn documentToColumn(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerColumn viewerColumn = new ViewerColumn();
    viewerColumn.setSolrName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_SOLR_NAME), null));
    viewerColumn.setSourceType(objectToEnum(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_SOURCE_TYPE),
      ViewerSourceType.class, ViewerSourceType.NATIVE));
    viewerColumn.setDisplayName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_NAME), null));
    viewerColumn.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_DESCRIPTION), null));
    viewerColumn.setDefaultValue(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_DEFAULT_VALUE), null));
    viewerColumn.setNillable(objectToBoolean(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_NILLABLE), true));
    viewerColumn
      .setAutoIncrement(objectToBoolean(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_AUTO_INCREMENT), false));
    viewerColumn
      .setColumnIndexInEnclosingTable(objectToInteger(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_INDEX), 0));

    com.databasepreservation.common.client.models.structure.ViewerType type = new com.databasepreservation.common.client.models.structure.ViewerType();
    type.setOriginalTypeName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_TYPE_ORIGINAL), null));
    type.setTypeName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_TYPE_NAME), null));
    String dbTypeStr = objectToString(doc.get(ViewerConstants.SOLR_DATABASES_COLUMN_TYPE_DB), null);
    if (dbTypeStr != null && !dbTypeStr.isEmpty()) {
      try {
        type.setDbType(com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.valueOf(dbTypeStr));
      } catch (Exception e) {
      }
    }
    viewerColumn.setType(type);
    return viewerColumn;
  }

  public static ViewerPrimaryKey documentToPrimaryKey(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerPrimaryKey viewerPrimaryKey = new ViewerPrimaryKey();
    viewerPrimaryKey.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PK_NAME), null));
    viewerPrimaryKey.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PK_DESCRIPTION), null));
    viewerPrimaryKey.setColumnIndexesInViewerTable(
      objectToIntegerList(doc.getFieldValues(ViewerConstants.SOLR_DATABASES_PK_COLUMN_INDEXES)));
    return viewerPrimaryKey;
  }

  public static ViewerCandidateKey documentToCandidateKey(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerCandidateKey viewerCandidateKey = new ViewerCandidateKey();
    viewerCandidateKey.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_CK_NAME), null));
    viewerCandidateKey.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_CK_DESCRIPTION), null));
    viewerCandidateKey.setColumnIndexesInViewerTable(
      objectToIntegerList(doc.getFieldValues(ViewerConstants.SOLR_DATABASES_CK_COLUMN_INDEXES)));
    return viewerCandidateKey;
  }

  public static ViewerForeignKey documentToForeignKey(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerForeignKey viewerForeignKey = new ViewerForeignKey();
    viewerForeignKey.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_FK_NAME), null));
    viewerForeignKey.setSourceType(objectToEnum(doc.get(ViewerConstants.SOLR_DATABASES_FK_SOURCE_TYPE),
      ViewerSourceType.class, ViewerSourceType.NATIVE));
    viewerForeignKey.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_FK_DESCRIPTION), null));
    viewerForeignKey
      .setReferencedTableUUID(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_FK_REFERENCED_TABLE_UUID), null));
    viewerForeignKey
      .setReferencedTableId(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_FK_REFERENCED_TABLE_ID), null));
    viewerForeignKey.setMatchType(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_FK_MATCH_TYPE), null));
    viewerForeignKey.setDeleteAction(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_FK_DELETE_ACTION), null));
    viewerForeignKey.setUpdateAction(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_FK_UPDATE_ACTION), null));

    List<Integer> sources = objectToIntegerList(
      doc.getFieldValues(ViewerConstants.SOLR_DATABASES_FK_REFERENCE_SOURCE_IDX));
    List<Integer> refs = objectToIntegerList(doc.getFieldValues(ViewerConstants.SOLR_DATABASES_FK_REFERENCE_REF_IDX));
    List<ViewerReference> refList = new ArrayList<>();

    if (sources != null && refs != null && sources.size() == refs.size()) {
      for (int i = 0; i < sources.size(); i++) {
        ViewerReference r = new ViewerReference();
        r.setSourceColumnIndex(sources.get(i));
        r.setReferencedColumnIndex(refs.get(i));
        refList.add(r);
      }
    }
    viewerForeignKey.setReferences(refList);
    return viewerForeignKey;
  }

  public static ViewerCheckConstraint documentToCheckConstraint(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerCheckConstraint viewerCheckConstraint = new ViewerCheckConstraint();
    viewerCheckConstraint.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_CHECK_NAME), null));
    viewerCheckConstraint
      .setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_CHECK_DESCRIPTION), null));
    viewerCheckConstraint.setCondition(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_CHECK_CONDITION), null));
    return viewerCheckConstraint;
  }

  public static ViewerTrigger documentToTrigger(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerTrigger viewerTrigger = new ViewerTrigger();
    viewerTrigger.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TRIGGER_NAME), null));
    viewerTrigger.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TRIGGER_DESCRIPTION), null));
    viewerTrigger.setActionTime(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TRIGGER_ACTION_TIME), null));
    viewerTrigger.setTriggerEvent(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TRIGGER_EVENT), null));
    viewerTrigger.setAliasList(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TRIGGER_ALIAS_LIST), null));
    viewerTrigger.setTriggeredAction(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_TRIGGER_ACTION), null));
    return viewerTrigger;
  }

  public static ViewerRoutineParameter documentToParameter(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerRoutineParameter viewerRoutineParameter = new ViewerRoutineParameter();
    viewerRoutineParameter.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PARAMETER_NAME), null));
    viewerRoutineParameter.setMode(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PARAMETER_MODE), null));
    viewerRoutineParameter
      .setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PARAMETER_DESCRIPTION), null));

    com.databasepreservation.common.client.models.structure.ViewerType type = new com.databasepreservation.common.client.models.structure.ViewerType();
    type.setOriginalTypeName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PARAMETER_TYPE_ORIGINAL), null));
    type.setTypeName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PARAMETER_TYPE_NAME), null));
    String dbTypeStr = objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PARAMETER_TYPE_DB), null);
    if (dbTypeStr != null && !dbTypeStr.isEmpty()) {
      type.setDbType(com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.valueOf(dbTypeStr));
    }

    viewerRoutineParameter.setType(type);
    return viewerRoutineParameter;
  }

  public static ViewerUserStructure documentToUser(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerUserStructure viewerUserStructure = new ViewerUserStructure();
    viewerUserStructure.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_USER_NAME), null));
    viewerUserStructure.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_USER_DESCRIPTION), null));
    return viewerUserStructure;
  }

  public static ViewerRoleStructure documentToRole(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerRoleStructure viewerRoleStructure = new ViewerRoleStructure();
    viewerRoleStructure.setName(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROLE_NAME), null));
    viewerRoleStructure.setAdmin(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROLE_ADMIN), null));
    viewerRoleStructure.setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_ROLE_DESCRIPTION), null));
    return viewerRoleStructure;
  }

  public static ViewerPrivilegeStructure documentToPrivilege(SolrDocument doc) {
    if (doc == null) {
      return null;
    }
    ViewerPrivilegeStructure viewerPrivilegeStructure = new ViewerPrivilegeStructure();
    viewerPrivilegeStructure.setType(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PRIVILEGE_TYPE), null));
    viewerPrivilegeStructure
      .setGrantor(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PRIVILEGE_GRANTOR), null));
    viewerPrivilegeStructure
      .setGrantee(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PRIVILEGE_GRANTEE), null));
    viewerPrivilegeStructure.setObject(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PRIVILEGE_OBJECT), null));
    viewerPrivilegeStructure.setOption(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PRIVILEGE_OPTION), null));
    viewerPrivilegeStructure
      .setDescription(objectToString(doc.get(ViewerConstants.SOLR_DATABASES_PRIVILEGE_DESCRIPTION), null));
    return viewerPrivilegeStructure;
  }

  public static List<Integer> objectToIntegerList(Object object) {
    List<Integer> list = new ArrayList<>();
    if (object == null) {
      return list;
    }

    if (object instanceof java.util.Collection) {
      for (Object v : (java.util.Collection<?>) object) {
        addIntegerToList(list, v);
      }
    } else {
      addIntegerToList(list, object);
    }
    return list;
  }

  private static void addIntegerToList(List<Integer> list, Object v) {
    if (v instanceof Number) {
      list.add(((Number) v).intValue());
    } else if (v instanceof String) {
      try {
        list.add(Integer.parseInt((String) v));
      } catch (NumberFormatException e) {
        LOGGER.error("Attempted to convert an invalid value to Integer from Solr: '{}'", v);
      }
    }
  }

  public static String createSearchAllAlias(SolrClient index, String aliasName, List<String> collections)
    throws SolrServerException, IOException {
    if (!collections.isEmpty()) {
      CollectionAdminRequest.CreateAlias request = CollectionAdminRequest.createAlias(aliasName,
        String.join(",", collections));
      request.setMethod(SolrRequest.METHOD.POST);
      index.request(request);
    }
    return aliasName;
  }

  public static void deleteSearchAllAlias(SolrClient index, String aliasName) throws SolrServerException, IOException {
    CollectionAdminRequest.DeleteAlias request = CollectionAdminRequest.deleteAlias(aliasName);
    request.setMethod(SolrRequest.METHOD.POST);
    index.request(request);
  }

  public static Filter removeIndexIdFromSearch(Filter filter) {
    for (FilterParameter parameter : filter.getParameters()) {
      if (parameter instanceof BasicSearchFilterParameter) {
        String searchValue = ((BasicSearchFilterParameter) parameter).getValue();
        if (searchValue != null) {
          NotSimpleFilterParameter notSimpleFilterParameter = new NotSimpleFilterParameter();
          notSimpleFilterParameter.setName(ViewerConstants.INDEX_ID);
          notSimpleFilterParameter.setValue(searchValue);
          filter.getParameters().add(notSimpleFilterParameter);
          return filter;
        }
      }
    }
    return filter;
  }

  /**
   * Helper method to build a query string for highlighting based on the provided
   * filter and highlighted fields. It recursively processes the filter parameters
   * and constructs a query string that can be used for highlighting in Solr. The
   * method handles different types of filter parameters, including simple
   * filters, range filters, date filters, and more complex combinations of
   * filters.
   */

  private static String buildHighlightQuery(Filter filter, List<String> highlightedFields) {
    if (filter == null || filter.getParameters() == null) {
      return "";
    }
    List<String> hlQueries = new ArrayList<>();
    for (FilterParameter param : filter.getParameters()) {
      buildHighlightQueryRecursive(param, highlightedFields, hlQueries);
    }
    return String.join(" OR ", hlQueries).trim();
  }

  private static void buildHighlightQueryRecursive(FilterParameter parameter, List<String> highlightedFields,
    List<String> hlQueries) {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter p = (SimpleFilterParameter) parameter;
      String field = findHighlightField(p.getName(), highlightedFields);
      hlQueries.add(field + ":(" + escapeSolrSpecialChars(p.getValue()) + ")");

    } else if (parameter instanceof LongRangeFilterParameter ||
            parameter instanceof DateRangeFilterParameter ||
            parameter instanceof DateIntervalFilterParameter) {
      // Range filters are not suitable for highlighting, so we skip them
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter p = (BasicSearchFilterParameter) parameter;

      if (ViewerConstants.INDEX_SEARCH.equals(p.getName()) ||
              ViewerConstants.INDEX_LOB_TEXT_SEARCH.equals(p.getName())) {

        String val = escapeSolrSpecialChars(p.getValue());
        boolean isOcrOnlySearch = ViewerConstants.INDEX_LOB_TEXT_SEARCH.equals(p.getName());

        if (highlightedFields != null && !highlightedFields.isEmpty()) {
          List<String> virtualQueries = new ArrayList<>();

          for (String hlField : highlightedFields) {
            boolean isOcrField = hlField.endsWith(ViewerConstants.SOLR_ROWS_EXTRACTED_TEXT_SUFFIX);

            if (isOcrOnlySearch && isOcrField) {
              virtualQueries.add(hlField + ":(" + val + ")");
            } else if (!isOcrOnlySearch && !isOcrField) {
              virtualQueries.add(hlField + ":(" + val + ")");
            }
          }

          if (!virtualQueries.isEmpty()) {
            hlQueries.add("(" + String.join(" OR ", virtualQueries) + ")");
          } else {
            hlQueries.add("(" + val + ")");
          }
        } else {
          hlQueries.add("(" + val + ")");
        }

      } else {
        String field = findHighlightField(p.getName(), highlightedFields);
        hlQueries.add(field + ":(" + escapeSolrSpecialChars(p.getValue()) + ")");
      }

    } else if (parameter instanceof EDismaxSimplerQueryFilterParameter) {
      String val = escapeSolrSpecialChars(((EDismaxSimplerQueryFilterParameter) parameter).getValue());
      if (highlightedFields != null && !highlightedFields.isEmpty()) {
        List<String> edismaxQueries = new ArrayList<>();
        for (String hlField : highlightedFields) {
          edismaxQueries.add(hlField + ":(" + val + ")");
        }
        hlQueries.add("(" + String.join(" OR ", edismaxQueries) + ")");
      } else {
        hlQueries.add("(" + val + ")");
      }

    } else if (parameter instanceof OrFiltersParameters) {
      for (FilterParameter p : ((OrFiltersParameters) parameter).getValues()) {
        buildHighlightQueryRecursive(p, highlightedFields, hlQueries);
      }
    } else if (parameter instanceof AndFiltersParameters) {
      for (FilterParameter p : ((AndFiltersParameters) parameter).getValues()) {
        buildHighlightQueryRecursive(p, highlightedFields, hlQueries);
      }
    }
  }

  private static String findHighlightField(String filterField, List<String> highlightedFields) {
    if (filterField == null || highlightedFields == null || highlightedFields.isEmpty()) {
      return filterField;
    }
    if (highlightedFields.contains(filterField)) {
      return filterField;
    }

    int lastUnderscore = filterField.lastIndexOf('_');
    if (lastUnderscore > 0) {
      String prefix = filterField.substring(0, lastUnderscore + 1);
      for (String hlField : highlightedFields) {
        if (hlField.startsWith(prefix)) {
          return hlField;
        }
      }
    }
    return filterField;
  }
}
