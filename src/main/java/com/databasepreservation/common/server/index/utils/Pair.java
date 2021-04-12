/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.utils;

public class Pair<K, V> extends org.roda.core.data.v2.common.Pair<K, V> {

  private static final long serialVersionUID = 1L;

  public static <K, V> Pair<K, V> of(K key, V value) {
    return new Pair<K, V>(key, value);
  }

  private Pair(K first, V second) {
    super(first, second);
  }
  

}
