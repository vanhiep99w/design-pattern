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
class ExpressOrderWorkflowTest {

    @Autowired
    private ExpressOrderWorkflow expressOrderWorkflow;

    private OrderProcessingContext context;

    @BeforeEach
    void setUp() {
        context = OrderProcessingContext.builder()
                .orderId("TEST-EXP-001")
                .customerId("CUST-001")
                .items(Arrays.asList("Item1", "Item2"))
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void shouldProcessExpressOrderSuccessfully() {
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);

        assertThat(result.isProcessed()).isTrue();
        assertThat(result.isValid()).isTrue();
        assertThat(result.isInventoryAvailable()).isTrue();
        assertThat(result.isPaymentSuccessful()).isTrue();
        assertThat(result.getConfirmationNumber()).isNotNull();
        assertThat(result.getPaymentTransactionId()).isNotNull();
    }

    @Test
    void shouldAddExpressFeeToTotal() {
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);

        assertThat(result.getFinalAmount()).isGreaterThan(result.getTotalAmount());
        BigDecimal baseWithExpressFee = result.getTotalAmount().add(BigDecimal.valueOf(25.00));
        BigDecimal expectedFinal = baseWithExpressFee.add(baseWithExpressFee.multiply(BigDecimal.valueOf(0.10)));
        assertThat(result.getFinalAmount()).isEqualByComparingTo(expectedFinal);
    }

    @Test
    void shouldGenerateExpressConfirmationNumber() {
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);

        assertThat(result.getConfirmationNumber()).startsWith("EXP-");
        assertThat(result.getConfirmationNumber().length()).isGreaterThan(4);
    }

    @Test
    void shouldGenerateExpressTransactionId() {
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);

        assertThat(result.getPaymentTransactionId()).startsWith("EXPRESS-TXN-");
    }

    @Test
    void shouldLogExpressProcessingSteps() {
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);

        assertThat(result.getProcessingLog()).contains("Express validation started");
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("Priority handling enabled"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("express warehouse"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("Express Fee"));
    }

    @Test
    void shouldExecuteExpressPostProcessing() {
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> 
            log.contains("Express shipping") || log.contains("warehouse notified"));
    }

    @Test
    void shouldValidateWithExpressPriority() {
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);

        assertThat(result.getProcessingLog().get(0)).contains("Express validation");
    }
}
