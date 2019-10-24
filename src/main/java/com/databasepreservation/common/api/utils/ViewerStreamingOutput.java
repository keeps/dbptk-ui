package com.databasepreservation.common.api.utils;

import com.databasepreservation.common.api.common.ConsumesOutputStream;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ViewerStreamingOutput implements StreamingOutput {
  private final ConsumesOutputStream outputHandler;

  public ViewerStreamingOutput(final ConsumesOutputStream outputHandler) {
    this.outputHandler = outputHandler;
  }

  @Override
  public void write(final OutputStream output) throws IOException {
    outputHandler.consumeOutputStream(output);
  }

  public StreamResponse toStreamResponse() {
    return new StreamResponse(outputHandler);
  }
}
