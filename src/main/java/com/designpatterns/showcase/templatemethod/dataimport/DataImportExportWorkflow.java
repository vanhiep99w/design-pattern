package com.designpatterns.showcase.templatemethod.dataimport;

import com.designpatterns.showcase.templatemethod.dto.DataImportExportContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DataImportExportWorkflow {

    public final DataImportExportContext executeImport(DataImportExportContext context) {
        log.info("Starting data import workflow: {}", context.getOperationId());
        
        validateInput(context);
        
        if (!context.isValid()) {
            log.warn("Input validation failed: {}", context.getErrorMessage());
            context.setSuccess(false);
            return context;
        }
        
        openConnection(context);
        
        if (!context.isConnectionOpen()) {
            log.error("Failed to open data source connection");
            context.setSuccess(false);
            context.setErrorMessage("Connection failed");
            return context;
        }
        
        try {
            beforeDataProcessing(context);
            
            readData(context);
            
            transformData(context);
            
            validateData(context);
            
            if (!context.isDataValid()) {
                log.warn("Data validation failed after transformation");
                context.setSuccess(false);
                return context;
            }
            
            writeData(context);
            
            afterDataProcessing(context);
            
            context.setSuccess(true);
            log.info("Data import completed successfully: {}", context.getOperationId());
            
        } catch (Exception e) {
            log.error("Error during data import: {}", e.getMessage(), e);
            handleError(context, e);
            context.setSuccess(false);
        } finally {
            closeConnection(context);
        }
        
        return context;
    }

    protected abstract void validateInput(DataImportExportContext context);

    protected abstract void openConnection(DataImportExportContext context);

    protected abstract void readData(DataImportExportContext context);

    protected abstract void transformData(DataImportExportContext context);

    protected abstract void validateData(DataImportExportContext context);

    protected abstract void writeData(DataImportExportContext context);

    protected abstract void closeConnection(DataImportExportContext context);

    protected void beforeDataProcessing(DataImportExportContext context) {
        log.debug("Pre-processing hook for operation: {}", context.getOperationId());
    }

    protected void afterDataProcessing(DataImportExportContext context) {
        log.debug("Post-processing hook for operation: {}", context.getOperationId());
    }

    protected void handleError(DataImportExportContext context, Exception e) {
        log.error("Default error handling for operation: {}", context.getOperationId());
        context.setErrorMessage("Import failed: " + e.getMessage());
        context.addLog("Error: " + e.getMessage());
    }
}
