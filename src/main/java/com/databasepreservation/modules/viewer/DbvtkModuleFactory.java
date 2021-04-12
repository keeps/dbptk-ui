/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.modules.viewer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.model.exception.ModuleException;
import org.apache.commons.lang3.StringUtils;

import com.databasepreservation.model.exception.LicenseNotAcceptedException;
import com.databasepreservation.model.exception.UnsupportedModuleException;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.DatabaseModuleFactory;
import com.databasepreservation.model.modules.filters.DatabaseFilterModule;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.parameters.Parameters;
import com.databasepreservation.model.reporters.Reporter;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DbvtkModuleFactory implements DatabaseModuleFactory {
  public static final String PARAMETER_DATABASE_UUID = "database-id";

  private static final Parameter databaseUUID = new Parameter().longName(PARAMETER_DATABASE_UUID).shortName("dbid")
    .description("Database UUID to use in Solr").required(false).hasArgument(true).setOptionalArgument(false);

  @Override
  public boolean producesImportModules() {
    return false;
  }

  @Override
  public boolean producesExportModules() {
    return true;
  }

  @Override
  public String getModuleName() {
    return "internal-dbptke-export";
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Map<String, Parameter> getAllParameters() {
    HashMap<String, Parameter> parameterHashMap = new HashMap<>();
    parameterHashMap.put(databaseUUID.longName(), databaseUUID);
    return parameterHashMap;
  }

  @Override
  public Parameters getConnectionParameters() {
    return null;
  }

  @Override
  public Parameters getImportModuleParameters() throws UnsupportedModuleException {
    throw DatabaseModuleFactory.ExceptionBuilder.UnsupportedModuleExceptionForImportModule();
  }

  @Override
  public Parameters getExportModuleParameters() {
    return new Parameters(Collections.singletonList(databaseUUID), null);
  }

  @Override
  public DatabaseImportModule buildImportModule(Map<Parameter, String> parameters, Reporter reporter)
    throws UnsupportedModuleException {
    throw DatabaseModuleFactory.ExceptionBuilder.UnsupportedModuleExceptionForImportModule();
  }

  @Override
  public DatabaseFilterModule buildExportModule(Map<Parameter, String> parameters, Reporter reporter) throws ModuleException{
    String pDatabaseUUID = parameters.get(databaseUUID);

    reporter.exportModuleParameters(getModuleName(), PARAMETER_DATABASE_UUID, pDatabaseUUID);

    if (StringUtils.isBlank(pDatabaseUUID)) {
      throw new ModuleException().withMessage("Unable to obtain the database to ingest the data");
    } else {
      return new DbvtkExportModule(pDatabaseUUID);
    }
  }
}
