package com.designpatterns.showcase.templatemethod.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataImportExportContext {
    private String operationId;
    private String sourceType;
    private String destinationType;
    
    @Builder.Default
    private boolean valid = true;
    private String errorMessage;
    
    @Builder.Default
    private boolean connectionOpen = false;
    
    @Builder.Default
    private List<Map<String, Object>> rawData = new ArrayList<>();
    
    @Builder.Default
    private List<Map<String, Object>> transformedData = new ArrayList<>();
    
    @Builder.Default
    private boolean dataValid = true;
    
    @Builder.Default
    private boolean success = false;
    
    private int recordsRead;
    private int recordsWritten;
    private int recordsSkipped;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    @Builder.Default
    private List<String> processingLog = new ArrayList<>();
    
    public void addLog(String message) {
        processingLog.add(message);
    }
}
