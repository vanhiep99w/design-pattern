package com.designpatterns.showcase.templatemethod.payment;

import com.designpatterns.showcase.templatemethod.dto.PaymentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CreditCardPaymentWorkflowTest {

    @Autowired
    private CreditCardPaymentWorkflow creditCardPaymentWorkflow;

    private PaymentContext context;

    @BeforeEach
    void setUp() {
        context = PaymentContext.builder()
                .transactionId("TEST-CC-TXN-001")
                .customerId("CUST-001")
                .amount(BigDecimal.valueOf(250.00))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .build();
    }

    @Test
    void shouldProcessCreditCardPaymentSuccessfully() {
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isValid()).isTrue();
        assertThat(result.isAuthorized()).isTrue();
        assertThat(result.isCaptured()).isTrue();
        assertThat(result.getAuthorizationCode()).isNotNull();
        assertThat(result.getReceiptNumber()).isNotNull();
    }

    @Test
    void shouldGenerateAuthorizationCode() {
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.getAuthorizationCode()).startsWith("AUTH-CC-");
        assertThat(result.getAuthorizationCode().length()).isGreaterThan(8);
    }

    @Test
    void shouldGenerateReceiptNumber() {
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.getReceiptNumber()).startsWith("RECEIPT-CC-");
    }

    @Test
    void shouldCapturePaymentWithTimestamp() {
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.isCaptured()).isTrue();
        assertThat(result.getCapturedAt()).isNotNull();
    }

    @Test
    void shouldFailWhenAmountIsInvalid() {
        context.setAmount(BigDecimal.ZERO);

        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Invalid payment amount");
    }

    @Test
    void shouldFailWhenCustomerIdIsMissing() {
        context.setCustomerId(null);

        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Customer ID is required");
    }

    @Test
    void shouldLogCreditCardProcessingSteps() {
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).contains("Credit card validation started");
        assertThat(result.getProcessingLog()).contains("Credit card validation passed");
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("fraud check"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("authorized"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("captured"));
    }

    @Test
    void shouldExecuteFraudCheck() {
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("fraud check"));
    }

    @Test
    void shouldExecuteRewardsPostProcessing() {
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("rewards points"));
    }
}
