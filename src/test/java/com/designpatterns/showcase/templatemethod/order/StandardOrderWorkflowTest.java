package com.designpatterns.showcase.templatemethod.order;

import com.designpatterns.showcase.templatemethod.dto.OrderProcessingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StandardOrderWorkflowTest {

    @Autowired
    private StandardOrderWorkflow standardOrderWorkflow;

    private OrderProcessingContext context;

    @BeforeEach
    void setUp() {
        context = OrderProcessingContext.builder()
                .orderId("TEST-ORD-001")
                .customerId("CUST-001")
                .items(Arrays.asList("Item1", "Item2", "Item3"))
                .totalAmount(BigDecimal.valueOf(150.00))
                .build();
    }

    @Test
    void shouldProcessStandardOrderSuccessfully() {
        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);

        assertThat(result.isProcessed()).isTrue();
        assertThat(result.isValid()).isTrue();
        assertThat(result.isInventoryAvailable()).isTrue();
        assertThat(result.isPaymentSuccessful()).isTrue();
        assertThat(result.getConfirmationNumber()).isNotNull();
        assertThat(result.getPaymentTransactionId()).isNotNull();
    }

    @Test
    void shouldCalculatePricingCorrectly() {
        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(result.getTax()).isEqualByComparingTo(BigDecimal.valueOf(15.00));
        assertThat(result.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(165.00));
    }

    @Test
    void shouldFailWhenCustomerIdIsMissing() {
        context.setCustomerId(null);

        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);

        assertThat(result.isProcessed()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationMessage()).contains("Customer ID is required");
    }

    @Test
    void shouldFailWhenItemsAreEmpty() {
        context.setItems(Arrays.asList());

        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);

        assertThat(result.isProcessed()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationMessage()).contains("at least one item");
    }

    @Test
    void shouldLogProcessingSteps() {
        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);

        assertThat(result.getProcessingLog()).isNotEmpty();
        assertThat(result.getProcessingLog()).contains("Standard validation started");
        assertThat(result.getProcessingLog()).contains("Standard validation passed");
        assertThat(result.getProcessingLog()).contains("Standard inventory check started");
        assertThat(result.getProcessingLog()).contains("Standard pricing calculation started");
        assertThat(result.getProcessingLog()).contains("Standard payment processing started");
    }

    @Test
    void shouldGenerateConfirmationNumber() {
        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);

        assertThat(result.getConfirmationNumber()).startsWith("CONF-");
        assertThat(result.getConfirmationNumber().length()).isGreaterThan(5);
    }

    @Test
    void shouldGenerateTransactionId() {
        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);

        assertThat(result.getPaymentTransactionId()).startsWith("TXN-");
        assertThat(result.getPaymentTransactionId()).contains("-");
    }
}
