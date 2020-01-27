package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVPrinter;
import org.roda.core.data.utils.JsonUtils;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ResultsCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IndexResult<ViewerRow> results;
  private final TableStatus configTable;
  private final ViewerRow row;
  private final List<String> fieldsToReturn;
  private final boolean exportDescription;

  /**
   * Constructor.
   *
   * @param results
   *          the results to write to output stream.
   * @param filename
   *          the filename.
   * @param delimiter
   *          the CSV field delimiter.
   */
  public ResultsCSVOutputStream(final IndexResult<ViewerRow> results, final TableStatus configTable,
    final String filename, final boolean exportDescription, final char delimiter, String fieldsToHeader) {
    super(filename, delimiter);
    this.results = results;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    ;
    this.exportDescription = exportDescription;
    this.row = null;
  }

  public ResultsCSVOutputStream(final ViewerRow row, final TableStatus configTable, final String filename,
    final boolean exportDescription, final char delimiter, String fieldsToHeader) {
    super(filename, delimiter);
    this.row = row;
    this.results = null;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    this.exportDescription = exportDescription;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;

    if (this.results == null) {
      singleRow(writer, printer);
    } else {
      multiRow(writer, printer);
    }

    writer.flush();
  }

  private void singleRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException {
    printer = getFormat()
      .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0])).print(writer);
    printer.printRecord(row.getCellValues(fieldsToReturn));
  }

  private void multiRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException {
    boolean isFirst = true;
    for (final ViewerRow row : this.results.getResults()) {
      if (isFirst) {
        printer = getFormat()
          .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0]))
          .print(writer);
        isFirst = false;
      }

      printer.printRecord(getCellValues(row));
    }
  }

  private List<String> getCellValues(ViewerRow row) {
    List<String> values = new ArrayList<>();
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_ID);
    fieldsToReturn.remove(ViewerConstants.SOLR_ROWS_TABLE_UUID);

    for (String solrColumnName : fieldsToReturn) {
      final ColumnStatus columnConfig = configTable.getColumnById(solrColumnName);

      if (columnConfig != null && columnConfig.getType().equals(ViewerType.dbTypes.NESTED)) {
        // treat nested
        if (!row.getNestedRowList().isEmpty()) {
          String template = columnConfig.getSearchStatus().getList().getTemplate().getTemplate();
          StringBuilder stringBuilder = new StringBuilder();
          row.getNestedRowList().forEach(nestedRow -> {
            if (nestedRow.getNestedUUID().equals(solrColumnName)) {
              if (template != null && !template.isEmpty()) {
                final Map<String, String> map = cellsToJson(nestedRow.getCells(), columnConfig.getNestedColumns());
                Handlebars handlebars = new Handlebars();
                try {
                  Template handlebarTemplate = handlebars.compileInline(template);
                  stringBuilder.append(handlebarTemplate.apply(map));
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
          });
          values.add(stringBuilder.toString());
        }
      } else {
        // treat non-nested
        if (row.getCells().get(solrColumnName) == null) {
          values.add("");
        } else {
          values.add(row.getCells().get(solrColumnName).getValue());
        }
      }
    }

    return values;
  }

  private static Map<String, String> cellsToJson(Map<String, ViewerCell> cells, NestedColumnStatus nestedConfig) {
    final List<String> nestedFields = nestedConfig.getNestedFields();
    final List<String> nestedSolrNames = nestedConfig.getNestedSolrNames();
    int index = 0;

    Map<String, String> nestedValues = new HashMap<>();

    if (cells != null && !cells.isEmpty()) {
      for (String nestedField : nestedFields) {
        final String solrName = nestedSolrNames.get(index++);
        nestedValues.put(nestedField, cells.get(solrName).getValue());
      }
    }

    return nestedValues;
  }
}
