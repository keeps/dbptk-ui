package com.databasepreservation.common.shared.client.common.visualization.browse.metadata;

import com.databasepreservation.common.shared.client.common.lists.MetadataTableList;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface MetadataEditPanel<T> {

  MetadataTableList createTable();

  Column<T, ?> getDescriptionColumn();

  void updateSIARDbundle(String name, String value);
}
