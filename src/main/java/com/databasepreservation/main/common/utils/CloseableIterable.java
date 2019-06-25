package com.databasepreservation.main.common.utils;

import java.io.Closeable;

public interface CloseableIterable<T> extends Closeable, Iterable<T> {
}
