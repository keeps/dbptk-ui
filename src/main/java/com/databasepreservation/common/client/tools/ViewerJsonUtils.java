package com.databasepreservation.common.client.tools;

import java.util.List;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.common.search.SearchInfo;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerJsonUtils {

  public interface FilterMapper extends ObjectMapper<Filter> {
  }

  public interface SorterMapper extends ObjectMapper<Sorter> {
  }

  public interface SubListMapper extends ObjectMapper<Sublist> {
  }

  public interface StringListMapper extends ObjectMapper<List<String>> {
  }

  public interface SearchInfoMapper extends ObjectMapper<SearchInfo> {
  }

  private static FilterMapper filterMapper;
  private static SorterMapper sorterMapper;
  private static SubListMapper subListMapper;
  private static StringListMapper stringListMapper;
  private static SearchInfoMapper searchInfoMapper;

  public static FilterMapper getFilterMapper() {
    if (filterMapper == null) {
      filterMapper = GWT.create(FilterMapper.class);
    }
    return filterMapper;
  }

  public static SorterMapper getSorterMapper() {
    if (sorterMapper == null) {
      sorterMapper = GWT.create(SorterMapper.class);
    }
    return sorterMapper;
  }

  public static SubListMapper getSubListMapper() {
    if (subListMapper == null) {
      subListMapper = GWT.create(SubListMapper.class);
    }
    return subListMapper;
  }

  public static StringListMapper getStringListMapper() {
    if (stringListMapper == null) {
      stringListMapper = GWT.create(StringListMapper.class);
    }
    return stringListMapper;
  }

  public static SearchInfoMapper getSearchInfoMapper() {
    if (searchInfoMapper == null) {
      searchInfoMapper = GWT.create(SearchInfoMapper.class);
    }
    return searchInfoMapper;
  }
}
