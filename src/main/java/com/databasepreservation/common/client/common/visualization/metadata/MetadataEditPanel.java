/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.metadata;

import com.databasepreservation.common.client.common.lists.widgets.MetadataTableList;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface MetadataEditPanel<T> {

  MetadataTableList createTable();

  Column<T, ?> getDescriptionColumn();

  void updateSIARDbundle(String name, String value);
}
