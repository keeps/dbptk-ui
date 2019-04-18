package com.databasepreservation.visualization.utils;

import java.io.Closeable;

public interface CloseableIterable<T> extends Closeable, Iterable<T> {
}
