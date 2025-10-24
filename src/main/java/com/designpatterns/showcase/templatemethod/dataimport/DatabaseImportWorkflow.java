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
public class DatabaseImportWorkflow extends DataImportExportWorkflow {

    private final JdbcTemplate jdbcTemplate;

    @Override
    protected void validateInput(DataImportExportContext context) {
        log.info("Validating database import input for operation: {}", context.getOperationId());
        context.addLog("Database import validation started");
        
        if (context.getSourceType() == null || context.getSourceType().isEmpty()) {
            context.setValid(false);
            context.setErrorMessage("Source type is required");
            return;
        }
        
        context.setValid(true);
        context.setStartTime(LocalDateTime.now());
        context.addLog("Database import validation passed");
    }

    @Override
    protected void openConnection(DataImportExportContext context) {
        log.info("Opening database connection for operation: {}", context.getOperationId());
        context.addLog("Database connection established via JdbcTemplate");
        
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            context.setConnectionOpen(true);
            context.addLog("Database connection verified");
        } catch (Exception e) {
            log.error("Failed to verify database connection", e);
            context.setConnectionOpen(false);
            context.setErrorMessage("Database connection failed: " + e.getMessage());
        }
    }

    @Override
    protected void readData(DataImportExportContext context) {
        log.info("Reading data from database for operation: {}", context.getOperationId());
        context.addLog("Reading customer data from database");
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT customer_id, name, email, status FROM customers WHERE status = ?", 
                "ACTIVE"
            );
            
            context.setRawData(results);
            context.setRecordsRead(results.size());
            context.addLog("Read " + results.size() + " records from database");
            
        } catch (Exception e) {
            log.error("Error reading data from database", e);
            context.setRawData(new ArrayList<>());
            context.setRecordsRead(0);
            context.addLog("Error reading data: " + e.getMessage());
        }
    }

    @Override
    protected void transformData(DataImportExportContext context) {
        log.info("Transforming data for operation: {}", context.getOperationId());
        context.addLog("Starting data transformation");
        
        List<Map<String, Object>> transformed = new ArrayList<>();
        
        for (Map<String, Object> record : context.getRawData()) {
            Map<String, Object> transformedRecord = new HashMap<>();
            transformedRecord.put("customer_id", record.get("customer_id"));
            transformedRecord.put("name", record.get("name"));
            transformedRecord.put("email", record.get("email"));
            transformedRecord.put("status", record.get("status"));
            transformedRecord.put("processed_at", LocalDateTime.now().toString());
            
            transformed.add(transformedRecord);
        }
        
        context.setTransformedData(transformed);
        context.addLog("Transformed " + transformed.size() + " records");
    }

    @Override
    protected void validateData(DataImportExportContext context) {
        log.info("Validating transformed data for operation: {}", context.getOperationId());
        context.addLog("Validating transformed data");
        
        int validRecords = 0;
        int invalidRecords = 0;
        
        for (Map<String, Object> record : context.getTransformedData()) {
            if (record.get("customer_id") != null && record.get("email") != null) {
                validRecords++;
            } else {
                invalidRecords++;
            }
        }
        
        context.setDataValid(invalidRecords == 0);
        context.addLog("Data validation: " + validRecords + " valid, " + invalidRecords + " invalid");
    }

    @Override
    protected void writeData(DataImportExportContext context) {
        log.info("Writing transformed data for operation: {}", context.getOperationId());
        context.addLog("Writing data to import staging table");
        
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
            context.addLog("Successfully wrote " + written + " records to staging table");
            
        } catch (Exception e) {
            log.error("Error writing data", e);
            context.addLog("Error writing data: " + e.getMessage());
        }
    }

    @Override
    protected void closeConnection(DataImportExportContext context) {
        log.info("Closing database connection for operation: {}", context.getOperationId());
        context.addLog("Database connection closed");
        context.setConnectionOpen(false);
        context.setEndTime(LocalDateTime.now());
    }

    @Override
    protected void afterDataProcessing(DataImportExportContext context) {
        log.info("Post-processing database import for operation: {}", context.getOperationId());
        context.addLog("Generating import summary report");
        
        LocalDateTime endTime = LocalDateTime.now();
        context.setEndTime(endTime);
        
        context.getMetadata().put("import_type", "database");
        context.getMetadata().put("records_read", context.getRecordsRead());
        context.getMetadata().put("records_written", context.getRecordsWritten());
        context.getMetadata().put("duration_seconds", 
            java.time.Duration.between(context.getStartTime(), endTime).getSeconds());
    }
}
