/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
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
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.*;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.facets.*;
import com.databasepreservation.common.client.index.sort.SortParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.filter.solr.TermsFilterParameter;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.schema.SolrRowsCollectionRegistry;
import com.databasepreservation.common.server.index.schema.collections.RowsCollection;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SolrUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);
  private static final String DEFAULT_QUERY_PARSER_OPERATOR = "AND";
  private static final Set<String> NON_REPEATABLE_FIELDS = new HashSet<>(Arrays.asList(RodaConstants.AIP_TITLE,
    RodaConstants.AIP_LEVEL, RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL));

  private static Map<String, List<String>> liteFieldsForEachClass = new HashMap<>();

  public static final String COMMON = "common";
  public static final String CONF = "conf";
  public static final String SCHEMA = "managed-schema";

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
    String humanFriendlyName = "unknown";
    if (resultClass.equals(ViewerDatabase.class)) {
      humanFriendlyName = "database";
    } else if (resultClass.equals(SavedSearch.class)) {
      humanFriendlyName = "saved search";
    } else if (resultClass.equals(ViewerRow.class)) {
      humanFriendlyName = "row";
    }
    return humanFriendlyName;
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, SolrCollection<T> collection, Filter filter,
    Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    return find(index, collection, filter, sorter, sublist, Facets.NONE, new ArrayList<>());
  }

  public static String findSIARDFile(SolrClient index, String collectionName, Filter filter)
    throws RequestNotValidException, GenericException {
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    try {
      QueryResponse response = index.query(collectionName, query);
      SolrDocumentList list = response.getResults();
      if (list.isEmpty()) {
        return null;
      }
      SolrDocument entry = list.get(0);
      return objectToString(entry.get(ViewerConstants.INDEX_ID), null);
    } catch (SolrException | SolrServerException | IOException e) {
      throw buildGenericException(e);
    }
  }

  public static <T extends IsIndexed> IndexResult<T> find(SolrClient index, SolrCollection<T> collection, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, List<String> fieldsToReturn)
    throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[0]));
    }
    parseAndConfigureFacets(facets, query);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query);
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
        ret = new IndexResult<T>(offset, limit, totalCount, docs, facetResults);
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
    return find(index, SolrRowsCollectionRegistry.get(databaseUUID), filter, sorter, sublist, facets,
      new ArrayList<>());
  }

  public static IndexResult<ViewerRow> findRows(SolrClient index, String databaseUUID, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, List<String> fieldsToReturn) throws GenericException, RequestNotValidException {
    return find(index, SolrRowsCollectionRegistry.get(databaseUUID), filter, sorter, sublist, facets, fieldsToReturn);
  }

  public static Pair<IndexResult<ViewerRow>, String> findRows(SolrClient index, String databaseUUID, Filter filter, Sorter sorter, int pageSize, String cursorMark, List<String> fieldsToReturn)
      throws GenericException, RequestNotValidException {

    Pair<IndexResult<ViewerRow>, String> ret;
    SolrQuery query = new SolrQuery();
    query.setParam("q.op", DEFAULT_QUERY_PARSER_OPERATOR);
    query.setQuery(parseFilter(filter));

    query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    query.setRows(pageSize);
    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);

    if (!fieldsToReturn.isEmpty()) {
      query.setFields(fieldsToReturn.toArray(new String[0]));
    }

    final RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query);
      final IndexResult<ViewerRow> result = queryResponseToIndexResult(response, collection, Facets.NONE);
      ret = Pair.of(result, response.getNextCursorMark());
    } catch (SolrServerException | IOException  e) {
      throw new GenericException("Could not query index", e);
    } catch (SolrException e) {
      throw new RequestNotValidException(e);
    } catch (RuntimeException e) {
      throw new GenericException("Unexpected exception while querying index", e);
    }

    return ret;
  }

  public static Pair<IndexResult<ViewerRow>, String> findRows(SolrClient index, String databaseUUID, SolrQuery query, Sorter sorter, int pageSize, String cursorMark)
      throws GenericException, RequestNotValidException {

    Pair<IndexResult<ViewerRow>, String> ret;

    query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
    query.setRows(pageSize);
    final List<SolrQuery.SortClause> sortClauses = parseSorter(sorter);
    sortClauses.add(SolrQuery.SortClause.asc(RodaConstants.INDEX_UUID));
    query.setSorts(sortClauses);

    final RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

    try {
      QueryResponse response = index.query(collection.getIndexName(), query);
      final IndexResult<ViewerRow> result = queryResponseToIndexResult(response, collection, Facets.NONE);
      ret = Pair.of(result, response.getNextCursorMark());
    } catch (SolrServerException | IOException  e) {
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
    List<SolrQuery> queryList)
      throws GenericException, RequestNotValidException {
    IndexResult<ViewerRow> ret;

    SolrQuery query = new SolrQuery();
    String nestedPath = "";
    for (int i = 0; i < queryList.size(); i++) {
      SolrQuery subquery = queryList.get(i);
      if(i  == 0){
        query.set("q", subquery.getQuery());
        if (subquery.getFields() != null && !subquery.getFields().isEmpty()) {
          query.set("fl", subquery.getFields());
        }
      } else {
        if(nestedPath.isEmpty()){
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
      QueryResponse response = index.query(collection.getIndexName(), query);
      ret = queryResponseToIndexResult(response, collection, Facets.NONE);
    } catch (SolrServerException | IOException  e) {
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
      query.setFacetSort(FacetParams.FACET_SORT_INDEX);
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
    if (facetParameter.getMinCount() != FacetParameter.DEFAULT_MIN_COUNT) {
      query.add(String.format("f.%s.facet.mincount", facetParameter.getName()),
        String.valueOf(facetParameter.getMinCount()));
    }
    if (facetParameter.getLimit() != SimpleFacetParameter.DEFAULT_LIMIT) {
      query.add(String.format("f.%s.facet.limit", facetParameter.getName()), String.valueOf(facetParameter.getLimit()));
    }
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

    for (SolrDocument doc : docList) {
      T result;
      try {
        result = collection.fromSolrDocument(doc);
      } catch (ViewerException e) {
        throw new GenericException(e);
      }
      docs.add(result);
    }

    return new IndexResult<>(offset, limit, totalCount, docs, facetResults);
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
      SolrDocument doc = index.getById(collection.getIndexName(), id);
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
    } else if (parameter instanceof OneOfManyFilterParameter) {
      OneOfManyFilterParameter param = (OneOfManyFilterParameter) parameter;
      appendValuesUsingOROperator(ret, param.getName(), param.getValues(), prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter param = (BasicSearchFilterParameter) parameter;
      appendBasicSearch(ret, param.getName(), param.getValue(), "AND", prefixWithANDOperatorIfBuilderNotEmpty);
    } else if (parameter instanceof EmptyKeyFilterParameter) {
      EmptyKeyFilterParameter param = (EmptyKeyFilterParameter) parameter;
      appendANDOperator(ret, true);
      ret.append("(*:* NOT " + param.getName() + ":*)");
    } else if (parameter instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter param = (DateRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Date.class, param.getFromValue(), String.class,
          processToDate(param.getToValue(), param.getGranularity(), false),
          prefixWithANDOperatorIfBuilderNotEmpty);
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
    } else if (parameter instanceof TermsFilterParameter){
      TermsFilterParameter param = (TermsFilterParameter)parameter;
      ret.append("({!terms f="+param.getField()+" v=" + param.getParameterValue() + "})");
    }else {
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
        ret.append(" OR ").append("(").append(fromKey).append(":[* TO ")
            .append(processToDate(fromValue, granularity)).append("]");
        ret.append(" AND ").append(toKey).append(":[").append(processFromDate(toValue)).append(" TO *]").append(")");
      }

      ret.append(")");
    }
  }

  private static String processFromDate(Date fromValue) {
    final String ret;

    if (fromValue != null) {
      Instant instant = Instant.ofEpochMilli(fromValue.getTime());
      return instant.toString();
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
      sb.append(Instant.ofEpochMilli(toValue.getTime()).toString());
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
        String date = Instant.ofEpochMilli((((Date) value).getTime())).toString();
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

  private static void appendExactMatch(StringBuilder ret, String key, String value, boolean appendDoubleQuotes,
    boolean prefixWithANDOperatorIfBuilderNotEmpty) {
    appendANDOperator(ret, prefixWithANDOperatorIfBuilderNotEmpty);
    ret.append("(").append(key).append(": ");
    if (appendDoubleQuotes) {
      ret.append("\"");
    }
    ret.append(value.replaceAll("(\")", "\\\\$1"));
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
      appendWhiteSpaceTokenizedString(ret, key, value, operator);
    }
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

  private static void appendWhiteSpaceTokenizedString(StringBuilder ret, String key, String value, String operator) {
    appendANDOperator(ret, true);

    String[] split = value.trim().split("\\s+");
    ret.append("(");
    for (int i = 0; i < split.length; i++) {
      if (i != 0 && operator != null) {
        ret.append(" ").append(operator).append(" ");
      }
      if (split[i].matches("(AND|OR|NOT)")) {
        ret.append(key).append(": \"").append(split[i]).append("\"");
      } else {
        ret.append(key).append(": (").append(escapeSolrSpecialChars(split[i])).append(")");
      }
    }
    ret.append(")");
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
  private static String getFilterQueries(User user, boolean justActive) {

    StringBuilder fq = new StringBuilder();

    // TODO find a better way to define admin super powers
    if (user != null && !user.getName().equals("admin")) {
      fq.append("(");
      String usersKey = RodaConstants.INDEX_PERMISSION_USERS_PREFIX + Permissions.PermissionType.READ;
      appendExactMatch(fq, usersKey, user.getId(), true, false);

      String groupsKey = RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + Permissions.PermissionType.READ;
      appendValuesUsingOROperatorForQuery(fq, groupsKey, new ArrayList<>(user.getGroups()), true);

      fq.append(")");
    }

    if (justActive) {
      appendExactMatch(fq, RodaConstants.INDEX_STATE, AIPState.ACTIVE.toString(), true, true);
    }

    return fq.toString();
  }

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
      } else {
        LOGGER.warn("Could not convert Solr object to string, unsupported class: {}", object.getClass().getName());
      }
    }
    return ret;
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
}
