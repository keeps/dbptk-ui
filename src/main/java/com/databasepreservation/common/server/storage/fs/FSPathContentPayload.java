package com.databasepreservation.common.server.storage.fs;

import com.databasepreservation.common.server.storage.ContentPayload;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class FSPathContentPayload implements ContentPayload {
    private final Path path;

    public FSPathContentPayload(Path path) {
        this.path = path;
    }

    @Override
    public InputStream createInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public void writeToPath(Path outPath) throws IOException {
//        Files.copy(path, outPath, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(path, outPath);
    }

    @Override
    public URI getURI() throws IOException, UnsupportedOperationException {
        return path.toUri();
    }
}
