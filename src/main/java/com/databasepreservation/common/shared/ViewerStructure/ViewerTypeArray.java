package com.databasepreservation.common.shared.ViewerStructure;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTypeArray extends ViewerType {
  //private static final Logger LOGGER = LoggerFactory.getLogger(ViewerTypeArray.class);

  // the type of each element in the array
  private ViewerType elementType;

  public ViewerTypeArray() {
    super();
    setDbType(dbTypes.COMPOSED_ARRAY);
  }

  public ViewerType getElementType() {
    return elementType;
  }

  public void setElementType(ViewerType elementType) {
    this.elementType = elementType;
  }

  public void setDbType(dbTypes dbType) {
    if (!dbTypes.COMPOSED_ARRAY.equals(dbType)) {
     // LOGGER.warn("{} cannot have {} other than {}. Value not set.", ViewerTypeArray.class.getName(),
       // dbTypes.class.getName(), dbTypes.COMPOSED_ARRAY.name());
    } else {
      super.setDbType(dbType);
    }
  }
}
