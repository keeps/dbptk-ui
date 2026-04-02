package com.databasepreservation.common.server.batch.core;

import java.io.Serializable;

/**
 * Marker interface to indicate that a chunk-oriented step should be executed
 * asynchronously using Spring Batch Integration's AsyncItemProcessor and AsyncItemWriter.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface AsyncChunkStepDefinition<I extends Serializable, O extends Serializable>
        extends ChunkStepDefinition<I, O> {
}
