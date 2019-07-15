package com.databasepreservation.main.desktop.client.dbptk.metadata;

import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface MetadataEditPanel<T> {

  MetadataTableList createTable();

  Column<T, ?> getDescriptionColumn();

  void updateSIARDbundle(String name, String value);
}
