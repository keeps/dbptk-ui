/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.google.gwt.i18n.client.LocaleInfo;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class NestedRowList {

  private ViewerDatabase database;
  private ViewerTable table;
  private Filter filter;
  private CollectionStatus status;

  NestedRowList(ViewerDatabase database, ViewerTable table, Filter filter, CollectionStatus status) {
    this.database = database;
    this.table = table;
    this.filter = filter;
    this.status = status;
  }

  protected void getData() {
    List<String> fieldsToReturn = new ArrayList<>();
    Map<String, String> extraParameters = new HashMap<>();
    DataTransformationUtils.buildNestedFieldsToReturn(table, status, extraParameters, fieldsToReturn);
    FindRequest findRequest = new FindRequest(ViewerDatabase.class.getName(), filter, new Sorter(), new Sublist(), null,
      false, fieldsToReturn, extraParameters);

//    DatabaseService.Util.call(callback).findRows(database.getUuid(), database.getUuid(), findRequest,
//      LocaleInfo.getCurrentLocale().getLocaleName());
  }
}
