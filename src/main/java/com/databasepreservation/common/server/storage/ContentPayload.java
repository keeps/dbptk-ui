/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ContentPayload {
    /**
     * Create a new inputstream that should be explicitly closed after being
     * consumed.
     *
     * @return
     */
    public InputStream createInputStream() throws IOException;

    /**
     * Write the current stream to the specified file path.
     *
     * @param path
     * @throws IOException
     */
    public void writeToPath(Path path) throws IOException;

    /**
     * Retrieves an URI for the file (e.g. 'file:', 'http:', etc.)
     *
     * @return
     */
    public URI getURI() throws IOException, UnsupportedOperationException;
}
