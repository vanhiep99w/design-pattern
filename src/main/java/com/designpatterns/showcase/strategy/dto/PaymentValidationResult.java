package com.designpatterns.showcase.strategy.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaymentValidationResult {
    private boolean valid;
    private String validationType;
    private List<String> errors;
    private List<String> warnings;
    private Integer riskScore;
    private String message;
}
