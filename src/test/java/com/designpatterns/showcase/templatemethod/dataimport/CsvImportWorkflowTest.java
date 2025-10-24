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
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CsvImportWorkflowTest {

    @Autowired
    private CsvImportWorkflow csvImportWorkflow;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DataImportExportContext context;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM import_staging");
        
        context = DataImportExportContext.builder()
                .operationId("TEST-CSV-IMPORT-001")
                .sourceType("customers.csv")
                .destinationType("staging")
                .build();
    }

    @Test
    void shouldImportCsvDataSuccessfully() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isValid()).isTrue();
        assertThat(result.isDataValid()).isTrue();
        assertThat(result.getRecordsRead()).isEqualTo(3);
    }

    @Test
    void shouldFilterActiveRecordsOnly() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getRecordsRead()).isEqualTo(3);
        assertThat(result.getRecordsWritten()).isEqualTo(2);
        assertThat(result.getRecordsSkipped()).isEqualTo(1);
    }

    @Test
    void shouldTransformCsvData() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getTransformedData()).allMatch(record -> 
            record.get("name").toString().equals(record.get("name").toString().toUpperCase()));
        assertThat(result.getTransformedData()).allMatch(record -> 
            record.get("email").toString().equals(record.get("email").toString().toLowerCase()));
        assertThat(result.getTransformedData()).allMatch(record -> 
            record.containsKey("import_source"));
    }

    @Test
    void shouldValidateCsvFileExtension() {
        context.setSourceType("customers.txt");

        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("CSV file");
    }

    @Test
    void shouldWriteToDatabase() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM import_staging", Integer.class);
        assertThat(count).isEqualTo(result.getRecordsWritten());
    }

    @Test
    void shouldLogCsvProcessingSteps() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getProcessingLog()).contains("CSV import validation started");
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("Parsing CSV"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("CSV data transformation"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("encoding and format"));
    }

    @Test
    void shouldExecutePreProcessingHook() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> 
            log.contains("encoding and format"));
    }

    @Test
    void shouldExecutePostProcessingHook() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("Archiving CSV"));
    }

    @Test
    void shouldPopulateCsvMetadata() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getMetadata()).containsKey("import_type");
        assertThat(result.getMetadata().get("import_type")).isEqualTo("csv");
        assertThat(result.getMetadata()).containsKey("source_file");
        assertThat(result.getMetadata().get("source_file")).isEqualTo("customers.csv");
        assertThat(result.getMetadata()).containsKey("records_skipped");
    }

    @Test
    void shouldValidateEmailsInCsvData() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.isDataValid()).isTrue();
        assertThat(result.getTransformedData()).allMatch(record -> 
            record.get("email").toString().contains("@"));
    }

    @Test
    void shouldSetCsvTimestamps() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getStartTime()).isNotNull();
        assertThat(result.getEndTime()).isNotNull();
    }

    @Test
    void shouldAddImportSourceTag() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.getTransformedData()).allMatch(record -> 
            "CSV".equals(record.get("import_source")));
    }

    @Test
    void shouldCloseFileConnectionAfterImport() {
        DataImportExportContext result = csvImportWorkflow.executeImport(context);

        assertThat(result.isConnectionOpen()).isFalse();
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("file connection closed"));
    }
}
