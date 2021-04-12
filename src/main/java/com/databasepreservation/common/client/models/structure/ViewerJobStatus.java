/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
//copy of BatchStatus for use in GWT
public enum ViewerJobStatus {
  COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN, NEW;
}
