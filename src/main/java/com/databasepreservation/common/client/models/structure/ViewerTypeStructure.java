/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

/**
 * Not yet implemented
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTypeStructure extends ViewerType {
  //private static final Logger LOGGER = LoggerFactory.getLogger(ViewerTypeStructure.class);

  public ViewerTypeStructure() {
    super();
  }

  public void setDbType(dbTypes dbType) {
    if (!dbTypes.COMPOSED_STRUCTURE.equals(dbType)) {
     // LOGGER.warn("{} cannot have {} other than {}. Value not set.", ViewerTypeArray.class.getName(),
       // dbTypes.class.getName(), dbTypes.COMPOSED_STRUCTURE.name());
    } else {
      super.setDbType(dbType);
    }
  }
}
