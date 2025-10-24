package com.designpatterns.showcase.templatemethod.dataimport;

import com.designpatterns.showcase.templatemethod.dto.DataImportExportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvImportWorkflow extends DataImportExportWorkflow {

    private final JdbcTemplate jdbcTemplate;

    @Override
    protected void validateInput(DataImportExportContext context) {
        log.info("Validating CSV import input for operation: {}", context.getOperationId());
        context.addLog("CSV import validation started");
        
        String sourceType = context.getSourceType();
        if (sourceType == null || !sourceType.toLowerCase().endsWith(".csv")) {
            context.setValid(false);
            context.setErrorMessage("Source must be a CSV file");
            return;
        }
        
        context.setValid(true);
        context.setStartTime(LocalDateTime.now());
        context.addLog("CSV import validation passed");
    }

    @Override
    protected void openConnection(DataImportExportContext context) {
        log.info("Opening CSV file for operation: {}", context.getOperationId());
        context.addLog("CSV file connection opened: " + context.getSourceType());
        context.setConnectionOpen(true);
    }

    @Override
    protected void readData(DataImportExportContext context) {
        log.info("Reading data from CSV for operation: {}", context.getOperationId());
        context.addLog("Parsing CSV file: " + context.getSourceType());
        
        List<Map<String, Object>> csvData = new ArrayList<>();
        
        Map<String, Object> record1 = new HashMap<>();
        record1.put("customer_id", "CSV001");
        record1.put("name", "John Doe");
        record1.put("email", "john.doe@example.com");
        record1.put("status", "ACTIVE");
        csvData.add(record1);
        
        Map<String, Object> record2 = new HashMap<>();
        record2.put("customer_id", "CSV002");
        record2.put("name", "Jane Smith");
        record2.put("email", "jane.smith@example.com");
        record2.put("status", "ACTIVE");
        csvData.add(record2);
        
        Map<String, Object> record3 = new HashMap<>();
        record3.put("customer_id", "CSV003");
        record3.put("name", "Bob Johnson");
        record3.put("email", "bob.johnson@example.com");
        record3.put("status", "PENDING");
        csvData.add(record3);
        
        context.setRawData(csvData);
        context.setRecordsRead(csvData.size());
        context.addLog("Read " + csvData.size() + " records from CSV");
    }

    @Override
    protected void transformData(DataImportExportContext context) {
        log.info("Transforming CSV data for operation: {}", context.getOperationId());
        context.addLog("Starting CSV data transformation");
        
        List<Map<String, Object>> transformed = new ArrayList<>();
        int skipped = 0;
        
        for (Map<String, Object> record : context.getRawData()) {
            String status = (String) record.get("status");
            if ("ACTIVE".equals(status)) {
                Map<String, Object> transformedRecord = new HashMap<>();
                transformedRecord.put("customer_id", record.get("customer_id"));
                transformedRecord.put("name", ((String) record.get("name")).toUpperCase());
                transformedRecord.put("email", ((String) record.get("email")).toLowerCase());
                transformedRecord.put("status", status);
                transformedRecord.put("import_source", "CSV");
                transformedRecord.put("processed_at", LocalDateTime.now().toString());
                
                transformed.add(transformedRecord);
            } else {
                skipped++;
            }
        }
        
        context.setTransformedData(transformed);
        context.setRecordsSkipped(skipped);
        context.addLog("Transformed " + transformed.size() + " records, skipped " + skipped + " non-active records");
    }

    @Override
    protected void validateData(DataImportExportContext context) {
        log.info("Validating CSV transformed data for operation: {}", context.getOperationId());
        context.addLog("Validating CSV transformed data");
        
        int validRecords = 0;
        int invalidRecords = 0;
        
        for (Map<String, Object> record : context.getTransformedData()) {
            String email = (String) record.get("email");
            if (email != null && email.contains("@")) {
                validRecords++;
            } else {
                invalidRecords++;
            }
        }
        
        context.setDataValid(invalidRecords == 0);
        context.addLog("CSV data validation: " + validRecords + " valid, " + invalidRecords + " invalid");
    }

    @Override
    protected void writeData(DataImportExportContext context) {
        log.info("Writing CSV data to database for operation: {}", context.getOperationId());
        context.addLog("Inserting CSV data into database");
        
        try {
            int written = 0;
            for (Map<String, Object> record : context.getTransformedData()) {
                jdbcTemplate.update(
                    "INSERT INTO import_staging (customer_id, name, email, status, processed_at) VALUES (?, ?, ?, ?, ?)",
                    record.get("customer_id"),
                    record.get("name"),
                    record.get("email"),
                    record.get("status"),
                    record.get("processed_at")
                );
                written++;
            }
            
            context.setRecordsWritten(written);
            context.addLog("Successfully imported " + written + " records from CSV");
            
        } catch (Exception e) {
            log.error("Error writing CSV data", e);
            context.addLog("Error writing CSV data: " + e.getMessage());
        }
    }

    @Override
    protected void closeConnection(DataImportExportContext context) {
        log.info("Closing CSV file for operation: {}", context.getOperationId());
        context.addLog("CSV file connection closed");
        context.setConnectionOpen(false);
        context.setEndTime(LocalDateTime.now());
    }

    @Override
    protected void beforeDataProcessing(DataImportExportContext context) {
        log.info("Pre-processing CSV import for operation: {}", context.getOperationId());
        context.addLog("Checking CSV file encoding and format");
    }

    @Override
    protected void afterDataProcessing(DataImportExportContext context) {
        log.info("Post-processing CSV import for operation: {}", context.getOperationId());
        context.addLog("Archiving CSV file after successful import");
        
        context.getMetadata().put("import_type", "csv");
        context.getMetadata().put("source_file", context.getSourceType());
        context.getMetadata().put("records_read", context.getRecordsRead());
        context.getMetadata().put("records_written", context.getRecordsWritten());
        context.getMetadata().put("records_skipped", context.getRecordsSkipped());
    }
}
