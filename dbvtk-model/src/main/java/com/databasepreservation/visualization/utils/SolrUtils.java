/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.impl.ZkClientClusterStateProvider;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.FacetParams;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.RangeFacetParameter;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.modules.CloseableUtils;
import com.databasepreservation.utils.FileUtils;
import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.transformers.SolrTransformer;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SolrUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);

  /** Private empty constructor */
  private SolrUtils() {

  }

  public static String randomUUID() {
    return UUID.randomUUID().toString();
  }

  public static String getTableCollectionName(String tableUUID) {
    return ViewerSafeConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + tableUUID;
  }

  public static void setupSolrCloudConfigsets(String zkHost) {
    ZkClientClusterStateProvider zkClient = null;
    JarFile jar = null;

    // get resources and copy them to a temporary directory
    Path databaseDir = null;
    Path tableDir = null;
    Path savedSearchesDir = null;

    try {
      File jarFile = new File(SolrManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      zkClient = new ZkClientClusterStateProvider(zkHost);

      // if it is a directory the application in being run from an IDE
      // in that case do not setup (assuming setup is done)
      if (!jarFile.isDirectory()) {
        databaseDir = Files.createTempDirectory("dbv_db_");
        tableDir = Files.createTempDirectory("dbv_tab_");
        savedSearchesDir = Files.createTempDirectory("dbv_tab_");
        jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries();

        // copy files to temporary directories
        while (entries.hasMoreElements()) {
          String name = entries.nextElement().getName();

          String nameWithoutOriginPart = null;
          Path destination = null;
          if (name.startsWith(ViewerSafeConstants.SOLR_CONFIGSET_DATABASE_RESOURCE + "/")) {
            nameWithoutOriginPart = name.substring(ViewerSafeConstants.SOLR_CONFIGSET_DATABASE_RESOURCE.length() + 1);
            destination = databaseDir;
          } else if (name.startsWith(ViewerSafeConstants.SOLR_CONFIGSET_TABLE_RESOURCE + "/")) {
            nameWithoutOriginPart = name.substring(ViewerSafeConstants.SOLR_CONFIGSET_TABLE_RESOURCE.length() + 1);
            destination = tableDir;
          } else if (name.startsWith(ViewerSafeConstants.SOLR_CONFIGSET_SEARCHES_RESOURCE + "/")) {
            nameWithoutOriginPart = name.substring(ViewerSafeConstants.SOLR_CONFIGSET_SEARCHES_RESOURCE.length() + 1);
            destination = savedSearchesDir;
          } else {
            continue;
          }

          Path output = destination.resolve(nameWithoutOriginPart);
          copyResourceToPath(name, output);
        }

        // upload configs to solr
        uploadConfig(zkClient, databaseDir, ViewerSafeConstants.SOLR_CONFIGSET_DATABASE);
        uploadConfig(zkClient, tableDir, ViewerSafeConstants.SOLR_CONFIGSET_TABLE);
        uploadConfig(zkClient, savedSearchesDir, ViewerSafeConstants.SOLR_CONFIGSET_SEARCHES);
      }
    } catch (IOException | URISyntaxException e) {
      LOGGER.error("Could not extract Solr configset", e);
    } finally {
      // delete temporary files
      FileUtils.deleteDirectoryRecursiveQuietly(databaseDir);
      FileUtils.deleteDirectoryRecursiveQuietly(tableDir);
      FileUtils.deleteDirectoryRecursiveQuietly(savedSearchesDir);
      CloseableUtils.closeQuietly(zkClient);
      CloseableUtils.closeQuietly(jar);
    }
  }

  private static void copyResourceToPath(String name, Path output) throws IOException {
    if (name.endsWith("/")) {
      Files.createDirectories(output);
    } else {
      InputStream inputStream = SolrManager.class.getResourceAsStream("/" + name);
      output = Files.createFile(output);
      OutputStream outputStream = Files.newOutputStream(output, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      IOUtils.copy(inputStream, outputStream);
      inputStream.close();
      outputStream.close();
    }
  }

  private static void uploadConfig(ZkClientClusterStateProvider client, Path configPath, String configset) {
    try {
      client.uploadConfig(configPath, configset);
    } catch (IOException e) {
      LOGGER.debug("error uploading configset {} to solr", configset, e);
    }
  }

  // TODO: Handle Viewer datatypes
  private static <T> T solrDocumentTo(Class<T> resultClass, SolrDocument doc) throws GenericException {
    T ret = null;
    try {
      if (resultClass.equals(ViewerDatabase.class)) {
        ret = resultClass.cast(SolrTransformer.toDatabase(doc));
      } else if (resultClass.equals(ViewerRow.class)) {
        ret = resultClass.cast(SolrTransformer.toRow(doc));
      } else if (resultClass.equals(SavedSearch.class)) {
        ret = resultClass.cast(SolrTransformer.toSavedSearch(doc));
      } else {
        throw new GenericException("Cannot find class index name: " + resultClass.getName());
      }
    } catch (ViewerException e) {
      throw new GenericException("Cannot retrieve " + resultClass.getName(), e);
    }

    // if (resultClass.equals(IndexedAIP.class)) {
    // ret = resultClass.cast(solrDocumentToIndexAIP(doc));
    // } else if (resultClass.equals(IndexedRepresentation.class) ||
    // resultClass.equals(Representation.class)) {
    // ret = resultClass.cast(solrDocumentToRepresentation(doc));
    // } else {
    // throw new GenericException("Cannot find class index name: " +
    // resultClass.getName());
    // }
    return ret;
  }

  // TODO: Handle Viewer datatypes
  private static <T> String getIndexName(Class<T> resultClass) throws GenericException {
    String indexName = null;
    if (resultClass.equals(ViewerDatabase.class)) {
      indexName = ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME;
    } else if (resultClass.equals(SavedSearch.class)) {
      indexName = ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME;
    } else if (resultClass.equals(ViewerRow.class)) {
      throw new GenericException("Can not determine collection name from " + ViewerRow.class.getName() + " class name");
    } else {
      throw new GenericException("Cannot find class index name: " + resultClass.getName());
    }
    return indexName;
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, sorter, sublist, null);
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    parseAndConfigureFacets(facets, query);

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets);
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

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, User user, boolean showInactive) throws GenericException,
    RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());
    parseAndConfigureFacets(facets, query);
    if (hasPermissionFilters(classToRetrieve)) {
      query.addFilterQuery(getFilterQueries(user, showInactive));
    }

    try {
      QueryResponse response = index.query(getIndexName(classToRetrieve), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets);
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not query index", e);
    }

    return ret;
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve,
    String tableUUID, Filter filter, Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, tableUUID, filter, sorter, sublist, null);
  }

  public static <T extends Serializable> IndexResult<T> find(SolrClient index, Class<T> classToRetrieve,
    String tableUUID, Filter filter, Sorter sorter, Sublist sublist, Facets facets) throws GenericException,
    RequestNotValidException {
    IndexResult<T> ret;
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    query.setStart(sublist.getFirstElementIndex());
    query.setRows(sublist.getMaximumElementCount());

    parseAndConfigureFacets(facets, query);

    try {
      QueryResponse response = index.query(getTableCollectionName(tableUUID), query);
      ret = queryResponseToIndexResult(response, classToRetrieve, facets);
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not query index", e);
    }

    return ret;
  }

  public static InputStream findCSV(SolrClient index, String collection, Filter filter, Sorter sorter, Sublist sublist,
    List<String> fields) throws GenericException, RequestNotValidException {
    SolrQuery query = new SolrQuery();
    query.setQuery(parseFilter(filter));
    query.setSorts(parseSorter(sorter));
    if (sublist != null) {
      query.setStart(sublist.getFirstElementIndex());
      query.setRows(sublist.getMaximumElementCount());
    }
    query.setFields(fields.toArray(new String[0]));

    LOGGER.debug("CSV export query object: " + query.toString());
    LOGGER.debug("CSV export query: " + query.toQueryString());

    try {
      QueryRequest queryRequest = new QueryRequest(query);
      queryRequest.setResponseParser(new InputStreamResponseParser("csv"));
      QueryResponse response = queryRequest.process(index, collection);

      Object stream = response.getResponse().get("stream");
      if (stream instanceof InputStream) {
        return (InputStream) stream;
      } else {
        throw new GenericException("Result was not an input stream. Its string representation was: "
          + stream.toString());
      }
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not query index", e);
    }
  }

  public static List<SolrQuery.SortClause> parseSorter(Sorter sorter) {
    List<SolrQuery.SortClause> ret = new ArrayList<SolrQuery.SortClause>();
    if (sorter != null) {
      for (SortParameter sortParameter : sorter.getParameters()) {
        ret.add(new SolrQuery.SortClause(sortParameter.getName(), sortParameter.isDescending() ? SolrQuery.ORDER.desc
          : SolrQuery.ORDER.asc));
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
          appendValuesUsingOROperator(filterQuery, facetParameter.getName(),
            ((SimpleFacetParameter) facetParameter).getValues());
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

  private static <T> boolean hasPermissionFilters(Class<T> resultClass) throws GenericException {
    return resultClass.equals(AIP.class) || resultClass.equals(IndexedAIP.class)
      || resultClass.equals(Representation.class) || resultClass.equals(IndexedRepresentation.class)
      || resultClass.equals(IndexedFile.class) || resultClass.equals(IndexedPreservationEvent.class);
  }

  public static <T extends Serializable> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
    Class<T> responseClass, Facets facets) throws GenericException {
    return queryResponseToIndexResult(response, responseClass, null, facets);
  }

  public static <T extends Serializable> IndexResult<T> queryResponseToIndexResult(QueryResponse response,
    Class<T> responseClass, List<String> columnNames, Facets facets) throws GenericException {
    final SolrDocumentList docList = response.getResults();
    final List<FacetFieldResult> facetResults = processFacetFields(facets, response.getFacetFields());
    final long offset = docList.getStart();
    final long limit = docList.size();
    final long totalCount = docList.getNumFound();
    final List<T> docs = new ArrayList<T>();

    for (SolrDocument doc : docList) {
      T result;
      result = solrDocumentTo(responseClass, doc);
      docs.add(result);
    }

    return new IndexResult<T>(offset, limit, totalCount, docs, facetResults);
  }

  private static List<FacetFieldResult> processFacetFields(Facets facets, List<FacetField> facetFields) {
    List<FacetFieldResult> ret = new ArrayList<FacetFieldResult>();
    FacetFieldResult facetResult;
    if (facetFields != null) {
      for (FacetField facet : facetFields) {
        LOGGER.trace("facet:{} count:{}", facet.getName(), facet.getValueCount());
        facetResult = new FacetFieldResult(facet.getName(), facet.getValueCount(), facets.getParameters()
          .get(facet.getName()).getValues());
        for (FacetField.Count count : facet.getValues()) {
          LOGGER.trace("   value:{} value:{}", count.getName(), count.getCount());
          facetResult.addFacetValue(count.getName(), count.getName(), count.getCount());
        }
        ret.add(facetResult);
      }
    }
    return ret;
  }

  public static <T extends Serializable> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter)
    throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0)).getTotalCount();
  }

  public static <T extends Serializable> Long count(SolrClient index, Class<T> classToRetrieve, Filter filter,
    User user, boolean showInactive) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, filter, null, new Sublist(0, 0), null, user, showInactive).getTotalCount();
  }

  public static <T extends Serializable> Long count(SolrClient index, Class<T> classToRetrieve, String tableUUID,
    Filter filter) throws GenericException, RequestNotValidException {
    return find(index, classToRetrieve, tableUUID, filter, null, new Sublist(0, 0)).getTotalCount();
  }

  public static <T> T retrieve(SolrClient index, Class<T> classToRetrieve, String id) throws NotFoundException,
    GenericException {
    T ret;
    try {
      SolrDocument doc = index.getById(getIndexName(classToRetrieve), id);
      if (doc != null) {
        ret = solrDocumentTo(classToRetrieve, doc);
      } else {
        throw new NotFoundException("Could not find document " + id);
      }
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not retrieve " + classToRetrieve.getName() + " from index", e);
    }
    return ret;
  }

  public static <T> T retrieve(SolrClient index, Class<T> classToRetrieve, String tableUUID, String rowUUID)
    throws NotFoundException, GenericException {
    T ret;
    try {
      SolrDocument doc = index.getById(getTableCollectionName(tableUUID), rowUUID);
      if (doc != null) {
        ret = solrDocumentTo(classToRetrieve, doc);
      } else {
        throw new NotFoundException("Could not find document " + rowUUID);
      }
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not retrieve " + classToRetrieve.getName() + " from index", e);
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
        parseFilterParameter(ret, parameter);
      }

      if (ret.length() == 0) {
        ret.append("*:*");
      }
    }

    LOGGER.trace("Converting filter {} to query {}", filter, ret);
    return ret.toString();
  }

  private static void parseFilterParameter(StringBuilder ret, FilterParameter parameter)
    throws RequestNotValidException {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter simplePar = (SimpleFilterParameter) parameter;
      appendExactMatch(ret, simplePar.getName(), simplePar.getValue(), true, true);
    } else if (parameter instanceof OneOfManyFilterParameter) {
      OneOfManyFilterParameter param = (OneOfManyFilterParameter) parameter;
      appendValuesUsingOROperator(ret, param.getName(), param.getValues());
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter param = (BasicSearchFilterParameter) parameter;
      appendBasicSearch(ret, param.getName(), param.getValue(), "AND", true);
    } else if (parameter instanceof EmptyKeyFilterParameter) {
      EmptyKeyFilterParameter param = (EmptyKeyFilterParameter) parameter;
      appendANDOperator(ret, true);
      ret.append("(*:* NOT ").append(param.getName()).append(":*)");
    } else if (parameter instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter param = (DateRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Date.class, param.getFromValue(), String.class,
        processToDate(param.getToValue(), param.getGranularity(), false));
    } else if (parameter instanceof DateIntervalFilterParameter) {
      DateIntervalFilterParameter param = (DateIntervalFilterParameter) parameter;
      appendRangeInterval(ret, param.getFromName(), param.getToName(), param.getFromValue(), param.getToValue(),
        param.getGranularity());
    } else if (parameter instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter param = (LongRangeFilterParameter) parameter;
      appendRange(ret, param.getName(), Long.class, param.getFromValue(), Long.class, param.getToValue());
    } else {
      LOGGER.error("Unsupported filter parameter class: {}", parameter.getClass().getName());
      throw new RequestNotValidException("Unsupported filter parameter class: " + parameter.getClass().getName());
    }
  }

  private static void appendRangeInterval(StringBuilder ret, String fromKey, String toKey, Date fromValue,
    Date toValue, RodaConstants.DateGranularity granularity) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, true);
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

  private static String processToDate(Date toValue, RodaConstants.DateGranularity granularity, boolean returnStartOnNull) {
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

  private static <T extends Serializable> void generateRangeValue(StringBuilder ret, Class<T> valueClass, T value) {
    if (value != null) {
      if (valueClass.equals(Date.class)) {
        String date = Instant.ofEpochMilli((Date.class.cast(value).getTime())).toString();
        LOGGER.trace("Appending date value \"{}\" to range", date);
        ret.append(date);
      } else if (valueClass.equals(Long.class)) {
        ret.append(Long.class.cast(value));
      } else if (valueClass.equals(String.class)) {
        ret.append(String.class.cast(value));
      } else {
        LOGGER.error("Cannot process range of the type {}", valueClass);
      }
    } else {
      ret.append("*");
    }
  }

  private static <T extends Serializable, T1 extends Serializable> void appendRange(StringBuilder ret, String key,
    Class<T> fromClass, T fromValue, Class<T1> toClass, T1 toValue) {
    if (fromValue != null || toValue != null) {
      appendANDOperator(ret, true);

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

  /**
   * Method that knows how to escape characters for Solr
   * <p>
   * <code>+ - && || ! ( ) { } [ ] ^ " ~ * ? : \</code>
   * </p>
   * <p>
   * Note: chars <code>'-', '"' and '*'</code> are not being escaped on purpose
   * </p>
   *
   * @return a string with special characters escaped
   */
  // FIXME perhaps && and || are not being properly escaped: see how to do it
  public static String escapeSolrSpecialChars(String string) {
    return string.replaceAll("([+&|!(){}\\[\\]\\^\\\\~?:\"])", "\\\\$1");
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

  public static <T extends IsIndexed> void delete(SolrClient index, Class<T> classToDelete, Filter filter)
    throws GenericException, RequestNotValidException {
    try {
      index.deleteByQuery(getIndexName(classToDelete), parseFilter(filter));
    } catch (SolrServerException | SolrException | IOException e) {
      throw new GenericException("Could not delete items", e);
    }
  }

  public static <T extends IsIndexed> void delete(SolrClient index, Class<T> classToDelete, List<String> ids)
    throws GenericException {
    try {
      index.deleteById(getIndexName(classToDelete), ids);
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
      appendExactMatch(fq, RodaConstants.STATE, AIPState.ACTIVE.toString(), true, true);
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
}
