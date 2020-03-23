package com.databasepreservation.modules.viewer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
  public static final String PARAMETER_LOB_FOLDER = "lob-folder";

  private static final Parameter databaseUUID = new Parameter().longName(PARAMETER_DATABASE_UUID).shortName("dbid")
    .description("Database UUID to use in Solr").required(false).hasArgument(true).setOptionalArgument(false);

  private static final Parameter lobFolder = new Parameter().longName(PARAMETER_LOB_FOLDER).shortName("lf")
    .description("Folder to place database LOBs").required(true).hasArgument(true).setOptionalArgument(false);

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
    return "internal-dbvtk-export";
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Map<String, Parameter> getAllParameters() {
    HashMap<String, Parameter> parameterHashMap = new HashMap<>();
    parameterHashMap.put(databaseUUID.longName(), databaseUUID);
    parameterHashMap.put(lobFolder.longName(), lobFolder);
    return parameterHashMap;
  }

  @Override
  public Parameters getConnectionParameters() throws UnsupportedModuleException {
    return null;
  }

  @Override
  public Parameters getImportModuleParameters() throws UnsupportedModuleException {
    throw DatabaseModuleFactory.ExceptionBuilder.UnsupportedModuleExceptionForImportModule();
  }

  @Override
  public Parameters getExportModuleParameters() throws UnsupportedModuleException {
    return new Parameters(Arrays.asList(databaseUUID, lobFolder), null);
  }

  @Override
  public DatabaseImportModule buildImportModule(Map<Parameter, String> parameters, Reporter reporter)
    throws UnsupportedModuleException, LicenseNotAcceptedException {
    throw DatabaseModuleFactory.ExceptionBuilder.UnsupportedModuleExceptionForImportModule();
  }

  @Override
  public DatabaseFilterModule buildExportModule(Map<Parameter, String> parameters, Reporter reporter)
    throws UnsupportedModuleException, LicenseNotAcceptedException {
    String pDatabaseUUID = parameters.get(databaseUUID);

    Path pLobFolder = Paths.get(parameters.get(lobFolder));

    reporter.exportModuleParameters(getModuleName(), PARAMETER_LOB_FOLDER, pLobFolder.toString());

    if (StringUtils.isBlank(pDatabaseUUID)) {
      return new DbvtkExportModule(pLobFolder);
    } else {
      return new DbvtkExportModule(pDatabaseUUID, pLobFolder);
    }
  }
}
