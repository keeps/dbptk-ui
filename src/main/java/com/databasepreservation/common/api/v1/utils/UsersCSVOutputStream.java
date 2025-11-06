package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;
import org.roda.core.data.exceptions.GenericException;

import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class UsersCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IterableDatabaseResult<ViewerDatabase> databases;

  /**
   * Constructor.
   *
   * @param databases
   *          the results to write to output stream.
   * @param filename
   *          the filename.
   * @param delimiter
   *          the CSV field delimiter.
   */
  public UsersCSVOutputStream(final IterableDatabaseResult<ViewerDatabase> databases, final String filename,
    final char delimiter) {
    super(filename, delimiter);
    this.databases = databases;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;

    try {
      multiRow(writer, printer);
    } catch (GenericException e) {
      throw new IOException("Error writing CSV output stream", e);
    }

    writer.flush();
  }

  @Override
  public Date getLastModified() {
    return null;
  }

  @Override
  public long getSize() {
    return -1;
  }

  private void multiRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException, GenericException {
    boolean isFirst = true;

    Iterator<ViewerDatabase> iterator = this.databases.iterator();
    Map<String, List<String>> usersMap = new HashMap<>();

    while (iterator.hasNext()) {
      ViewerDatabase database = iterator.next();
      Map<String, AuthorizationDetails> permissions = ViewerFactory.getConfigurationManager()
        .getDatabaseStatus(database.getUuid()).getPermissions();
      for (Map.Entry<String, AuthorizationDetails> entry : permissions.entrySet()) {
        String user = entry.getKey();
        if (!usersMap.containsKey(user)) {
          usersMap.put(user, List.of(database.getMetadata().getName()));
        } else {
          usersMap.get(user).add(database.getMetadata().getName());
        }
      }
    }
    for (Map.Entry<String, List<String>> entry : usersMap.entrySet()) {
      if (isFirst) {
        printer = getFormat().withHeader("User", "Databases").print(writer);
        isFirst = false;
      }

      printer.printRecord(entry.getKey(), String.join(", ", entry.getValue()));
    }
  }
}
