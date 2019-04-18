package com.databasepreservation.visualization.shared.ViewerStructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not yet implemented
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTypeStructure extends ViewerType {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerTypeStructure.class);

  public ViewerTypeStructure() {
    super();
  }

  public void setDbType(dbTypes dbType) {
    if (!dbTypes.COMPOSED_STRUCTURE.equals(dbType)) {
      LOGGER.warn("{} cannot have {} other than {}. Value not set.", ViewerTypeArray.class.getName(),
        dbTypes.class.getName(), dbTypes.COMPOSED_STRUCTURE.name());
    } else {
      super.setDbType(dbType);
    }
  }
}
