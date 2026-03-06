/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.filter;


import java.io.Serial;
import java.util.Objects;

/**
 * @author Alexandre Flores
 * 
 */
public class EDismaxSimplerQueryFilterParameter extends FilterParameter {
  @Serial
  private static final long serialVersionUID = -7483070132609341799L;

  private String value;

  /**
   * Constructs an empty {@link EDismaxSimplerQueryFilterParameter}.
   */
  public EDismaxSimplerQueryFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link EDismaxSimplerQueryFilterParameter} cloning an existing
   * {@link EDismaxSimplerQueryFilterParameter}.
   *
   * @param eDismaxSimplerQueryFilterParameter
   *          the {@link EDismaxSimplerQueryFilterParameter} to clone.
   */
  public EDismaxSimplerQueryFilterParameter(EDismaxSimplerQueryFilterParameter eDismaxSimplerQueryFilterParameter) {
    this(eDismaxSimplerQueryFilterParameter.getValue());
  }

  /**
   * Constructs a {@link EDismaxSimplerQueryFilterParameter} with the given parameters.
   *
   * @param value
   */
  public EDismaxSimplerQueryFilterParameter(String value) {
    setValue(value);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "EDismaxSimplerQueryFilterParameter(value=" + getValue() + ")";
  }


  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    EDismaxSimplerQueryFilterParameter that = (EDismaxSimplerQueryFilterParameter) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }
}
