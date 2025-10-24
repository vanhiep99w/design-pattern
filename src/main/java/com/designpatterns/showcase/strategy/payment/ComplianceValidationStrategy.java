package com.designpatterns.showcase.strategy.payment;

import com.designpatterns.showcase.strategy.dto.PaymentValidationRequest;
import com.designpatterns.showcase.strategy.dto.PaymentValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component("complianceValidation")
public class ComplianceValidationStrategy implements PaymentValidationStrategy {

    private static final BigDecimal AML_THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal REPORTING_THRESHOLD = new BigDecimal("3000.00");
    private static final Set<String> SANCTIONED_COUNTRIES = Set.of("SANCTIONED1", "SANCTIONED2");
    private static final Set<String> RESTRICTED_PAYMENT_METHODS = Set.of("CRYPTO", "ANONYMOUS");

    @Override
    public PaymentValidationResult validate(PaymentValidationRequest request) {
        log.info("Performing compliance validation for customer: {}", request.getCustomerId());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (request.getBillingCountry() != null
                && SANCTIONED_COUNTRIES.contains(request.getBillingCountry().toUpperCase())) {
            errors.add("Transaction blocked: Country is under sanctions");
        }

        if (request.getPaymentMethod() != null
                && RESTRICTED_PAYMENT_METHODS.contains(request.getPaymentMethod().toUpperCase())) {
            errors.add("Payment method is restricted due to compliance regulations");
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(AML_THRESHOLD) >= 0) {
                warnings.add("Transaction requires AML (Anti-Money Laundering) verification");
                if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
                    errors.add("Customer identification required for transactions over $" + AML_THRESHOLD);
                }
            }

            if (request.getAmount().compareTo(REPORTING_THRESHOLD) >= 0) {
                warnings.add("Transaction will be reported to regulatory authorities");
            }
        }

        if (request.getPaymentDetails() != null) {
            String taxId = request.getPaymentDetails().get("taxId");
            if (request.getAmount() != null
                    && request.getAmount().compareTo(REPORTING_THRESHOLD) >= 0
                    && (taxId == null || taxId.trim().isEmpty())) {
                warnings.add("Tax ID recommended for transactions over $" + REPORTING_THRESHOLD);
            }
        }

        boolean isValid = errors.isEmpty();
        log.info("Compliance validation result: {}", isValid ? "PASSED" : "FAILED");

        return PaymentValidationResult.builder()
                .valid(isValid)
                .validationType("COMPLIANCE")
                .errors(errors)
                .warnings(warnings)
                .riskScore(0)
                .message(isValid ? "Compliance check passed" : "Compliance validation failed")
                .build();
    }

    @Override
    public String getStrategyName() {
        return "COMPLIANCE";
    }
}
