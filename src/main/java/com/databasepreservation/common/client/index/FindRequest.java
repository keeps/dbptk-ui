/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;

/**
 * A request to a find operation.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FindRequest extends CountRequest {

  @Serial
  private static final long serialVersionUID = 5997470558754294987L;

  /** Sorter. */
  public Sorter sorter;
  /** Sublist (paging). */
  public Sublist sublist;
  /** Facets to return. */
  public Facets facets;
  /** For CSV results, export only facets? */
  public boolean exportFacets;
  /** The index fields to return and use to construct the indexed object. */
  public List<String> fieldsToReturn;
  /** Query parser */
  public String defType;
  /** Filter query */
  public Filter filterQuery;
  /** Query fields */
  public List<String> queryFields;
  /** Use highlighting */
  public boolean highlighting;
  /** highlighted fields */
  public List<String> highlightedFields;
  /** highlight query */
  public Filter highlightQuery;

  public Map<String, String> extraParameters;

  /**
   * Constructor.
   */
  public FindRequest() {
    this(null, new Filter(), new Sorter(), new Sublist(), new Facets());
  }

  /**
   * Constructor.
   *
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   * @param sorter
   *          Sorter.
   * @param sublist
   *          Sublist (paging).
   * @param facets
   *          Facets to return.
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
    final Facets facets) {
    this(classToReturn, filter, sorter, sublist, facets, false, new ArrayList<>());
  }

  /**
   * Constructor.
   * 
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   * @param sorter
   *          Sorter.
   * @param sublist
   *          Sublist (paging).
   * @param facets
   *          Facets to return.
   * @param exportFacets
   *          for CSV results, export only facets?
   * @param fieldsToReturn
   *          the index fields to return.
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
    final Facets facets, final boolean exportFacets, final List<String> fieldsToReturn) {
    this(classToReturn, filter, sorter, sublist, facets, false, fieldsToReturn, new HashMap<>());
  }

  /**
   * Constructor.
   *
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   * @param sorter
   *          Sorter.
   * @param sublist
   *          Sublist (paging).
   * @param facets
   *          Facets to return.
   * @param exportFacets
   *          for CSV results, export only facets?
   * @param fieldsToReturn
   *          the index fields to return.
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
    final Facets facets, final boolean exportFacets, final List<String> fieldsToReturn,
    final Map<String, String> extraParameters) {
    super(classToReturn, filter);
    this.sorter = sorter;
    this.sublist = sublist;
    this.facets = facets;
    this.exportFacets = exportFacets;
    this.fieldsToReturn = fieldsToReturn;
    this.extraParameters = extraParameters;
  }

  /**
   * Constructor.
   *
   * @param classToReturn
   *          Class name of resources to return.
   * @param filter
   *          Filter.
   * @param sorter
   *          Sorter.
   * @param sublist
   *          Sublist (paging).
   * @param facets
   *          Facets to return.
   * @param exportFacets
   *          for CSV results, export only facets?
   * @param fieldsToReturn
   *          the index fields to return.
   * @param extraParameters
   *          extra parameters to send to the server.
   * @param filterQuery
   *          Filter query for filtered searches
   * @param queryFields
   *          Fields over which to search
   * @param highlighting
   *          Use highlighting
   * @param highlightedFields
   *          Highlighted fields
   */
  public FindRequest(final String classToReturn, final Filter filter, final Sorter sorter, final Sublist sublist,
    final Facets facets, final boolean exportFacets, final List<String> fieldsToReturn,
    final Map<String, String> extraParameters, String defType, Filter filterQuery, List<String> queryFields,
    boolean highlighting, List<String> highlightedFields, Filter highlightQuery) {
    this(classToReturn, filter, sorter, sublist, facets, exportFacets, fieldsToReturn, extraParameters);
    this.defType = defType;
    this.filterQuery = filterQuery;
    this.queryFields = queryFields;
    this.highlighting = highlighting;
    this.highlightedFields = highlightedFields;
    this.highlightQuery = highlightQuery;
  }

}
