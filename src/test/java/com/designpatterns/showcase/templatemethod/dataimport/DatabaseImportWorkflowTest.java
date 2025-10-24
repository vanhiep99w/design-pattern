package com.designpatterns.showcase.templatemethod.dataimport;

import com.designpatterns.showcase.templatemethod.dto.DataImportExportContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DatabaseImportWorkflowTest {

    @Autowired
    private DatabaseImportWorkflow databaseImportWorkflow;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DataImportExportContext context;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM import_staging");
        
        context = DataImportExportContext.builder()
                .operationId("TEST-DB-IMPORT-001")
                .sourceType("database")
                .destinationType("staging")
                .build();
    }

    @Test
    void shouldImportDataFromDatabaseSuccessfully() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isValid()).isTrue();
        assertThat(result.isDataValid()).isTrue();
        assertThat(result.getRecordsRead()).isGreaterThan(0);
        assertThat(result.getRecordsWritten()).isEqualTo(result.getRecordsRead());
    }

    @Test
    void shouldReadActiveCustomers() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.getRecordsRead()).isEqualTo(4);
        assertThat(result.getRawData()).hasSize(4);
        assertThat(result.getRawData()).allMatch(record -> "ACTIVE".equals(record.get("status")));
    }

    @Test
    void shouldTransformDataCorrectly() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.getTransformedData()).hasSize(result.getRecordsRead());
        assertThat(result.getTransformedData()).allMatch(record -> record.containsKey("processed_at"));
        assertThat(result.getTransformedData()).allMatch(record -> record.get("processed_at") != null);
    }

    @Test
    void shouldWriteToStagingTable() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM import_staging", Integer.class);
        assertThat(count).isEqualTo(result.getRecordsWritten());
    }

    @Test
    void shouldValidateTransformedData() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.isDataValid()).isTrue();
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("valid"));
    }

    @Test
    void shouldLogProcessingSteps() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.getProcessingLog()).contains("Database import validation started");
        assertThat(result.getProcessingLog()).contains("Database import validation passed");
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("Database connection"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("Reading customer data"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("transformation"));
    }

    @Test
    void shouldSetTimestamps() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.getStartTime()).isNotNull();
        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getEndTime()).isAfterOrEqualTo(result.getStartTime());
    }

    @Test
    void shouldPopulateMetadata() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.getMetadata()).containsKey("import_type");
        assertThat(result.getMetadata().get("import_type")).isEqualTo("database");
        assertThat(result.getMetadata()).containsKey("records_read");
        assertThat(result.getMetadata()).containsKey("records_written");
        assertThat(result.getMetadata()).containsKey("duration_seconds");
    }

    @Test
    void shouldVerifyDatabaseConnection() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("connection verified"));
    }

    @Test
    void shouldHandleEmptySourceType() {
        context.setSourceType("");

        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Source type is required");
    }

    @Test
    void shouldCloseConnectionAfterImport() {
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);

        assertThat(result.isConnectionOpen()).isFalse();
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("connection closed"));
    }
}
