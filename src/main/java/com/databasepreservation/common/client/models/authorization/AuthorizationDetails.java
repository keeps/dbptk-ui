/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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
