package com.designpatterns.showcase.templatemethod.controller;

import com.designpatterns.showcase.templatemethod.dataimport.CsvImportWorkflow;
import com.designpatterns.showcase.templatemethod.dataimport.DatabaseImportWorkflow;
import com.designpatterns.showcase.templatemethod.dto.DataImportExportContext;
import com.designpatterns.showcase.templatemethod.dto.OrderProcessingContext;
import com.designpatterns.showcase.templatemethod.dto.PaymentContext;
import com.designpatterns.showcase.templatemethod.order.ExpressOrderWorkflow;
import com.designpatterns.showcase.templatemethod.order.StandardOrderWorkflow;
import com.designpatterns.showcase.templatemethod.payment.CreditCardPaymentWorkflow;
import com.designpatterns.showcase.templatemethod.payment.CryptoPaymentWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/template-method-demo")
@RequiredArgsConstructor
public class TemplateMethodDemoController {

    private final StandardOrderWorkflow standardOrderWorkflow;
    private final ExpressOrderWorkflow expressOrderWorkflow;
    private final CreditCardPaymentWorkflow creditCardPaymentWorkflow;
    private final CryptoPaymentWorkflow cryptoPaymentWorkflow;
    private final DatabaseImportWorkflow databaseImportWorkflow;
    private final CsvImportWorkflow csvImportWorkflow;

    @PostMapping("/order/standard")
    public ResponseEntity<OrderProcessingContext> processStandardOrder(@RequestBody OrderProcessingContext context) {
        log.info("Processing standard order: {}", context.getOrderId());
        
        if (context.getOrderId() == null) {
            context.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        OrderProcessingContext result = standardOrderWorkflow.processOrder(context);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/order/express")
    public ResponseEntity<OrderProcessingContext> processExpressOrder(@RequestBody OrderProcessingContext context) {
        log.info("Processing express order: {}", context.getOrderId());
        
        if (context.getOrderId() == null) {
            context.setOrderId("EXP-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        OrderProcessingContext result = expressOrderWorkflow.processOrder(context);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payment/credit-card")
    public ResponseEntity<PaymentContext> processCreditCardPayment(@RequestBody PaymentContext context) {
        log.info("Processing credit card payment: {}", context.getTransactionId());
        
        if (context.getTransactionId() == null) {
            context.setTransactionId("TXN-" + UUID.randomUUID().toString());
        }
        
        PaymentContext result = creditCardPaymentWorkflow.executePayment(context);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payment/crypto")
    public ResponseEntity<PaymentContext> processCryptoPayment(@RequestBody PaymentContext context) {
        log.info("Processing crypto payment: {}", context.getTransactionId());
        
        if (context.getTransactionId() == null) {
            context.setTransactionId("CRYPTO-TXN-" + UUID.randomUUID().toString());
        }
        
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/database")
    public ResponseEntity<DataImportExportContext> importFromDatabase(@RequestBody DataImportExportContext context) {
        log.info("Starting database import: {}", context.getOperationId());
        
        if (context.getOperationId() == null) {
            context.setOperationId("DB-IMPORT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        DataImportExportContext result = databaseImportWorkflow.executeImport(context);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/csv")
    public ResponseEntity<DataImportExportContext> importFromCsv(@RequestBody DataImportExportContext context) {
        log.info("Starting CSV import: {}", context.getOperationId());
        
        if (context.getOperationId() == null) {
            context.setOperationId("CSV-IMPORT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        DataImportExportContext result = csvImportWorkflow.executeImport(context);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/demo/order-comparison")
    public ResponseEntity<Object> demonstrateOrderComparison() {
        log.info("Demonstrating order workflow comparison");
        
        OrderProcessingContext standardContext = OrderProcessingContext.builder()
                .orderId("DEMO-STD-001")
                .customerId("CUST-001")
                .items(java.util.Arrays.asList("Item1", "Item2"))
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        
        OrderProcessingContext expressContext = OrderProcessingContext.builder()
                .orderId("DEMO-EXP-001")
                .customerId("CUST-001")
                .items(java.util.Arrays.asList("Item1", "Item2"))
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        
        OrderProcessingContext standardResult = standardOrderWorkflow.processOrder(standardContext);
        OrderProcessingContext expressResult = expressOrderWorkflow.processOrder(expressContext);
        
        return ResponseEntity.ok(java.util.Map.of(
                "standard", standardResult,
                "express", expressResult
        ));
    }

    @GetMapping("/demo/payment-comparison")
    public ResponseEntity<Object> demonstratePaymentComparison() {
        log.info("Demonstrating payment workflow comparison");
        
        PaymentContext creditCardContext = PaymentContext.builder()
                .transactionId("DEMO-CC-001")
                .customerId("CUST-001")
                .amount(BigDecimal.valueOf(250.00))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .build();
        
        PaymentContext cryptoContext = PaymentContext.builder()
                .transactionId("DEMO-CRYPTO-001")
                .customerId("CUST-001")
                .amount(BigDecimal.valueOf(0.005))
                .currency("BTC")
                .paymentMethod("CRYPTO")
                .build();
        
        PaymentContext creditCardResult = creditCardPaymentWorkflow.executePayment(creditCardContext);
        PaymentContext cryptoResult = cryptoPaymentWorkflow.executePayment(cryptoContext);
        
        return ResponseEntity.ok(java.util.Map.of(
                "creditCard", creditCardResult,
                "crypto", cryptoResult
        ));
    }
}
