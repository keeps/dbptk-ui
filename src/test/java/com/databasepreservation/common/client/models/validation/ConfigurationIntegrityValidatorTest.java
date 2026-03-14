package com.databasepreservation.common.client.models.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualTableStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.exceptions.DependencyViolationException;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.configuration.validation.ConfigurationIntegrityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 * 
 *         Unit tests for the ConfigurationIntegrityValidator. Validates that
 *         the DAG correctly prevents the deletion and structural modification
 *         of Virtual Tables, Columns, and Foreign Keys when active downstream
 *         dependencies exist.
 */
public class ConfigurationIntegrityValidatorTest {

  private ConfigurationManager configManagerMock;
  private ConfigurationIntegrityValidator validator;
  private static final String DATABASE_UUID = "test-db-uuid";

  @BeforeEach
  public void setUp() {
    configManagerMock = mock(ConfigurationManager.class);
    validator = new ConfigurationIntegrityValidator(configManagerMock);
  }

  @Test
  public void testAllowDeletionWhenNoDependenciesExist() throws DependencyViolationException {
    // Arrange
    CollectionStatus oldStatus = createBaseCollection();
    ColumnStatus virtualCol = createVirtualColumn("col_1", "Virtual_Col", "{{actor_id}}");
    oldStatus.getTables().get(0).getColumns().add(virtualCol);

    CollectionStatus newStatus = createBaseCollection();
    ColumnStatus virtualColToRemove = createVirtualColumn("col_1", "Virtual_Col", "{{actor_id}}");
    virtualColToRemove.getVirtualColumnStatus().setProcessingState(ProcessingState.TO_REMOVE);
    newStatus.getTables().get(0).getColumns().add(virtualColToRemove);

    // Act & Assert - Should not throw any exception (If it throws, the test fails
    // automatically)
    validator.validateStateTransitions(DATABASE_UUID, oldStatus, newStatus);
  }

  @Test
  public void testBlockDeletionOfVirtualColumnUsedByVirtualFK() {
    // Arrange
    CollectionStatus oldStatus = createBaseCollection();
    ColumnStatus virtualCol = createVirtualColumn("col_1", "Virtual_Col", "{{actor_id}}");
    ForeignKeysStatus virtualFk = createVirtualFK("fk_1", "Virtual_FK", "col_1", "target_table");
    oldStatus.getTables().get(0).getColumns().add(virtualCol);
    oldStatus.getTables().get(0).getForeignKeys().add(virtualFk);

    CollectionStatus newStatus = createBaseCollection();
    ColumnStatus virtualColToRemove = createVirtualColumn("col_1", "Virtual_Col", "{{actor_id}}");
    virtualColToRemove.getVirtualColumnStatus().setProcessingState(ProcessingState.TO_REMOVE); // User wants to delete
    ForeignKeysStatus activeVirtualFk = createVirtualFK("fk_1", "Virtual_FK", "col_1", "target_table"); // Still active
    newStatus.getTables().get(0).getColumns().add(virtualColToRemove);
    newStatus.getTables().get(0).getForeignKeys().add(activeVirtualFk);

    // Act
    DependencyViolationException exception = assertThrows(DependencyViolationException.class, () -> {
      validator.validateStateTransitions(DATABASE_UUID, oldStatus, newStatus);
    });

    // Assert
    assertTrue("Error message should contain details about the blocked deletion",
      exception.getMessage().contains("Cannot be <b>deleted</b>: Virtual Column 'Virtual_Col' is required by [fk_1]"));
  }

  @Test
  public void testBlockModificationOfVirtualColumnUsedByVirtualFK() {
    // Arrange
    CollectionStatus oldStatus = createBaseCollection();
    ColumnStatus virtualCol = createVirtualColumn("col_1", "Virtual_Col", "{{actor_id}}");
    ForeignKeysStatus virtualFk = createVirtualFK("fk_1", "Virtual_FK", "col_1", "target_table");
    oldStatus.getTables().get(0).getColumns().add(virtualCol);
    oldStatus.getTables().get(0).getForeignKeys().add(virtualFk);

    CollectionStatus newStatus = createBaseCollection();
    // User modified the template from {{actor_id}} to {{film_id}}
    ColumnStatus modifiedVirtualCol = createVirtualColumn("col_1", "Virtual_Col", "{{film_id}}");
    ForeignKeysStatus activeVirtualFk = createVirtualFK("fk_1", "Virtual_FK", "col_1", "target_table");
    newStatus.getTables().get(0).getColumns().add(modifiedVirtualCol);
    newStatus.getTables().get(0).getForeignKeys().add(activeVirtualFk);

    // Act
    DependencyViolationException exception = assertThrows(DependencyViolationException.class, () -> {
      validator.validateStateTransitions(DATABASE_UUID, oldStatus, newStatus);
    });

    // Assert
    assertTrue("Error message should block modification due to dependency",
      exception.getMessage().contains("Cannot be <b>modified</b>: Virtual Column 'Virtual_Col' is required by [fk_1]"));
  }

  @Test
  public void testBlockDeletionOfVirtualTableUsedByDenormalization() throws Exception {
    // Arrange
    CollectionStatus oldStatus = createBaseCollection();
    oldStatus.getTables().get(0).setUuid("table_1");
    oldStatus.getTables().get(0).setName("Virtual_Table");

    VirtualTableStatus vtStatus = new VirtualTableStatus();
    vtStatus.setProcessingState(ProcessingState.PROCESSED);
    vtStatus.setSourceTableUUID("source_table");
    oldStatus.getTables().get(0).setVirtualTableStatus(vtStatus);
    oldStatus.setDenormalizations(new HashSet<>(Collections.singletonList("denorm_1")));

    // Mock Denormalization config fetching from disk
    DenormalizeConfiguration mockDenorm = new DenormalizeConfiguration();
    mockDenorm.setId("denorm_1");
    mockDenorm.setProcessingState(ProcessingState.PROCESSED);
    mockDenorm.setTableUUID("table_1"); // Denorm depends on the Virtual Table
    when(configManagerMock.getDenormalizeConfigurationFromCollectionStatusEntry(eq(DATABASE_UUID), anyString()))
      .thenReturn(mockDenorm);

    CollectionStatus newStatus = createBaseCollection();
    newStatus.getTables().get(0).setUuid("table_1");
    newStatus.getTables().get(0).setName("Virtual_Table");
    VirtualTableStatus vtStatusToRemove = new VirtualTableStatus();
    vtStatusToRemove.setProcessingState(ProcessingState.TO_REMOVE); // User wants to delete the Virtual Table
    newStatus.getTables().get(0).setVirtualTableStatus(vtStatusToRemove);

    // Act
    DependencyViolationException exception = assertThrows(DependencyViolationException.class, () -> {
      validator.validateStateTransitions(DATABASE_UUID, oldStatus, newStatus);
    });

    // Assert
    assertTrue("Error message should block table deletion", exception.getMessage()
      .contains("Cannot be <b>deleted</b>: Virtual Table 'Virtual_Table' is required by [denorm_1]"));
  }

  @Test
  public void testAllowDeletionIfDependentIsAlsoBeingDeleted() throws DependencyViolationException {
    // Arrange
    CollectionStatus oldStatus = createBaseCollection();
    ColumnStatus virtualCol = createVirtualColumn("col_1", "Virtual_Col", "{{actor_id}}");
    ForeignKeysStatus virtualFk = createVirtualFK("fk_1", "Virtual_FK", "col_1", "target_table");
    oldStatus.getTables().get(0).getColumns().add(virtualCol);
    oldStatus.getTables().get(0).getForeignKeys().add(virtualFk);

    CollectionStatus newStatus = createBaseCollection();
    ColumnStatus virtualColToRemove = createVirtualColumn("col_1", "Virtual_Col", "{{actor_id}}");
    virtualColToRemove.getVirtualColumnStatus().setProcessingState(ProcessingState.TO_REMOVE);

    ForeignKeysStatus fkToRemove = createVirtualFK("fk_1", "Virtual_FK", "col_1", "target_table");
    fkToRemove.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.TO_REMOVE); // FK is ALSO being deleted

    newStatus.getTables().get(0).getColumns().add(virtualColToRemove);
    newStatus.getTables().get(0).getForeignKeys().add(fkToRemove);

    // Act & Assert - Safe to delete both at the same time, should not throw
    validator.validateStateTransitions(DATABASE_UUID, oldStatus, newStatus);
  }

  @Test
  public void testNullSafeguardsDoNotCrashValidation() throws DependencyViolationException {
    // Arrange
    CollectionStatus oldStatus = createBaseCollection();
    CollectionStatus newStatus = createBaseCollection();

    // Injecting stray nulls into the arrays to mimic bad frontend payloads
    newStatus.getTables().add(null);
    newStatus.getTables().get(0).getColumns().add(null);
    newStatus.getTables().get(0).getForeignKeys().add(null);

    // Act & Assert - Should gracefully ignore nulls without throwing
    // NullPointerException
    validator.validateStateTransitions(DATABASE_UUID, oldStatus, newStatus);
  }

  // =================================================================================
  // Helper methods to generate models
  // =================================================================================

  private CollectionStatus createBaseCollection() {
    CollectionStatus status = new CollectionStatus();
    TableStatus table = new TableStatus();
    table.setId("table_1");
    table.setUuid("table_1");
    table.setName("table_1");
    table.setColumns(new ArrayList<>());
    table.setForeignKeys(new ArrayList<>());

    status.setTables(new ArrayList<>());
    status.getTables().add(table);
    return status;
  }

  private ColumnStatus createVirtualColumn(String id, String name, String template) {
    ColumnStatus col = new ColumnStatus();
    col.setId(id);
    col.setName(name);

    VirtualColumnStatus vStatus = new VirtualColumnStatus();
    vStatus.setProcessingState(ProcessingState.PROCESSED);

    TemplateStatus tempStatus = new TemplateStatus();
    tempStatus.setTemplate(template);
    vStatus.setTemplateStatus(tempStatus);

    col.setVirtualColumnStatus(vStatus);
    return col;
  }

  private ForeignKeysStatus createVirtualFK(String id, String name, String sourceColId, String targetTable) {
    ForeignKeysStatus fk = new ForeignKeysStatus();
    fk.setId(id);
    fk.setName(name);
    fk.setReferencedTableUUID(targetTable);

    ForeignKeysStatus.ReferencedColumnStatus ref = new ForeignKeysStatus.ReferencedColumnStatus();
    ref.setSourceColumnId(sourceColId);

    fk.setReferences(new ArrayList<>());
    fk.getReferences().add(ref);

    VirtualForeignKeysStatus vFkStatus = new VirtualForeignKeysStatus();
    vFkStatus.setProcessingState(ProcessingState.PROCESSED);
    TemplateStatus tempStatus = new TemplateStatus();
    tempStatus.setTemplate("{{target_id}}");
    vFkStatus.setTemplateStatus(tempStatus);

    fk.setVirtualForeignKeysStatus(vFkStatus);
    return fk;
  }
}
