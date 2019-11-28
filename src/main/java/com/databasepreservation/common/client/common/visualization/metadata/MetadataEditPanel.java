package com.databasepreservation.common.client.common.visualization.metadata;

import com.databasepreservation.common.client.common.lists.MetadataTableList;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface MetadataEditPanel<T> {

  MetadataTableList createTable();

  Column<T, ?> getDescriptionColumn();

  void updateSIARDbundle(String name, String value);
}
