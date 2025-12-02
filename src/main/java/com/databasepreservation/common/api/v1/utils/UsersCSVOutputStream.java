package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroup;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroupsList;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;
import com.databasepreservation.common.server.index.utils.Pair;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class UsersCSVOutputStream extends CSVOutputStream {
  private static final Logger LOGGER = LoggerFactory.getLogger(UsersCSVOutputStream.class);

  /** The loaded databases. */
  private final IterableDatabaseResult<ViewerDatabase> databases;
  /** The system users */
  private final AuthorizationGroupsList authorizationGroups;

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
  public UsersCSVOutputStream(final IterableDatabaseResult<ViewerDatabase> databases,
    AuthorizationGroupsList authorizationGroups, final String filename, final char delimiter) {
    super(filename, delimiter);
    this.databases = databases;
    this.authorizationGroups = authorizationGroups;
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
    Map<String, Pair<String, List<String>>> usersMap = new HashMap<>();

    for (AuthorizationGroup group : this.authorizationGroups.getAuthorizationGroupsList()) {
      if (group.getAttributeName().equals(ViewerFactory.getViewerConfiguration()
        .getViewerConfigurationAsString("fullname", ViewerConstants.USER_EXPORT_FULLNAME_ATTRIBUTE))) {
        usersMap.put(group.getAttributeValue(), Pair.of(group.getLabel(), new ArrayList<>()));
      }
    }

    while (iterator.hasNext()) {
      ViewerDatabase database = iterator.next();
      try {
        Map<String, AuthorizationDetails> permissions = ViewerFactory.getConfigurationManager()
          .getDatabaseStatus(database.getUuid()).getPermissions();
        for (Map.Entry<String, AuthorizationDetails> entry : permissions.entrySet()) {
          String user = entry.getKey();
          if (usersMap.containsKey(user)) {
            usersMap.get(user).getSecond().add(database.getMetadata().getName());
          }
        }
      } catch (GenericException e) {
        LOGGER.debug("Skipping database {} without permissions", e);
      }
    }
    for (Map.Entry<String, Pair<String, List<String>>> entry : usersMap.entrySet()) {
      if (isFirst) {
        printer = getFormat().withHeader("Name", "Label", "Databases").print(writer);
        isFirst = false;
      }

      printer.printRecord(entry.getKey(), entry.getValue().getFirst(), String.join(", ", entry.getValue().getSecond()));
    }
  }
}
