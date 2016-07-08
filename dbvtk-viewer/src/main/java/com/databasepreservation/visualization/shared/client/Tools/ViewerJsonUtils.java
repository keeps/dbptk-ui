package com.databasepreservation.visualization.shared.client.Tools;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;

import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerJsonUtils {


  public interface FilterMapper extends ObjectMapper<Filter> {}
  public interface SorterMapper extends ObjectMapper<Sorter> {}
  public interface SubListMapper extends ObjectMapper<Sublist> {}
  public interface StringListMapper extends ObjectMapper<List<String>> {}

  private static FilterMapper filterMapper;
  private static SorterMapper sorterMapper;
  private static SubListMapper subListMapper;
  private static StringListMapper stringListMapper;

  public static FilterMapper getFilterMapper() {
    if(filterMapper == null){
      filterMapper = GWT.create(FilterMapper.class);
    }
    return filterMapper;
  }

  public static SorterMapper getSorterMapper() {
    if(sorterMapper == null){
      sorterMapper = GWT.create(SorterMapper.class);
    }
    return sorterMapper;
  }

  public static SubListMapper getSubListMapper() {
    if(subListMapper == null){
      subListMapper = GWT.create(SubListMapper.class);
    }
    return subListMapper;
  }

  public static StringListMapper getStringListMapper() {
    if(stringListMapper == null){
      stringListMapper = GWT.create(StringListMapper.class);
    }
    return stringListMapper;
  }
}
