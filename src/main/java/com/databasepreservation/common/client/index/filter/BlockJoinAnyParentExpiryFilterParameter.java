package com.databasepreservation.common.client.index.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 *         <p>
 *         This class is meant to serve as a bandaid for Solr's delicate query
 *         parser when using transformers. It should be replaced with a more
 *         generic solution if these issues are solved. Do not use this class
 *         for other purposes, nor mix it with other parameters in a filter.
 */
public class BlockJoinAnyParentExpiryFilterParameter extends DateRangeFilterParameter {
  private List<String> groups;

  public BlockJoinAnyParentExpiryFilterParameter() {
    super();
  }

  public BlockJoinAnyParentExpiryFilterParameter(
    BlockJoinAnyParentExpiryFilterParameter blockJoinAnyParentExpiryFilterParameter) {
    super();
    setGroups(blockJoinAnyParentExpiryFilterParameter.getGroups());
  }

  public BlockJoinAnyParentExpiryFilterParameter(Collection<String> groups, Date fromValue, Date toValue) {
    super("expiry_date", fromValue, toValue);
    setGroups(new ArrayList<>(groups));
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  @Override
  public String toString() {
    return "{!parent which='*:* -_nest_path_:*'}[groups:" + groups.toString() + "; expiry:" + super.toString() + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    BlockJoinAnyParentExpiryFilterParameter other = (BlockJoinAnyParentExpiryFilterParameter) obj;
    if (groups != other.groups)
      return false;
    return true;
  }
}
