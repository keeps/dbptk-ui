package com.databasepreservation.common.client.models.authorization;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class AuthorizationDetails implements Serializable {
  private Date expiry;

  public AuthorizationDetails(Date expiry) {
    this.expiry = expiry;
  }

  public AuthorizationDetails() {
    this.expiry = null;
  }

  public Date getExpiry() {
    return expiry;
  }

  public void setExpiry(Date expiry) {
    this.expiry = expiry;
  }

  public boolean hasExpiryDate() {
    return expiry != null;
  }
}
