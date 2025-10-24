package com.designpatterns.showcase.templatemethod.controller;

import com.designpatterns.showcase.templatemethod.dto.DataImportExportContext;
import com.designpatterns.showcase.templatemethod.dto.OrderProcessingContext;
import com.designpatterns.showcase.templatemethod.dto.PaymentContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TemplateMethodDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessStandardOrder() throws Exception {
        OrderProcessingContext context = OrderProcessingContext.builder()
                .customerId("CUST-001")
                .items(Arrays.asList("Item1", "Item2"))
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        mockMvc.perform(post("/api/template-method-demo/order/standard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(true))
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.confirmationNumber").exists());
    }

    @Test
    void shouldProcessExpressOrder() throws Exception {
        OrderProcessingContext context = OrderProcessingContext.builder()
                .customerId("CUST-001")
                .items(Arrays.asList("Item1", "Item2"))
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        mockMvc.perform(post("/api/template-method-demo/order/express")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(true))
                .andExpect(jsonPath("$.orderId").value(org.hamcrest.Matchers.startsWith("EXP-ORD-")))
                .andExpect(jsonPath("$.confirmationNumber").value(org.hamcrest.Matchers.startsWith("EXP-")));
    }

    @Test
    void shouldProcessCreditCardPayment() throws Exception {
        PaymentContext context = PaymentContext.builder()
                .customerId("CUST-001")
                .amount(BigDecimal.valueOf(250.00))
                .currency("USD")
                .build();

        mockMvc.perform(post("/api/template-method-demo/payment/credit-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.authorizationCode").value(org.hamcrest.Matchers.startsWith("AUTH-CC-")));
    }

    @Test
    void shouldProcessCryptoPayment() throws Exception {
        PaymentContext context = PaymentContext.builder()
                .customerId("CUST-001")
                .amount(BigDecimal.valueOf(0.005))
                .currency("BTC")
                .build();

        mockMvc.perform(post("/api/template-method-demo/payment/crypto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value(org.hamcrest.Matchers.startsWith("CRYPTO-TXN-")))
                .andExpect(jsonPath("$.authorizationCode").value(org.hamcrest.Matchers.startsWith("BLOCKCHAIN-")));
    }

    @Test
    void shouldImportFromDatabase() throws Exception {
        DataImportExportContext context = DataImportExportContext.builder()
                .sourceType("database")
                .destinationType("staging")
                .build();

        mockMvc.perform(post("/api/template-method-demo/import/database")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.operationId").value(org.hamcrest.Matchers.startsWith("DB-IMPORT-")))
                .andExpect(jsonPath("$.recordsRead").value(4));
    }

    @Test
    void shouldImportFromCsv() throws Exception {
        DataImportExportContext context = DataImportExportContext.builder()
                .sourceType("customers.csv")
                .destinationType("staging")
                .build();

        mockMvc.perform(post("/api/template-method-demo/import/csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(context)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.operationId").value(org.hamcrest.Matchers.startsWith("CSV-IMPORT-")))
                .andExpect(jsonPath("$.recordsRead").value(3));
    }

    @Test
    void shouldGetOrderComparison() throws Exception {
        mockMvc.perform(get("/api/template-method-demo/demo/order-comparison"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.standard").exists())
                .andExpect(jsonPath("$.express").exists())
                .andExpect(jsonPath("$.standard.processed").value(true))
                .andExpect(jsonPath("$.express.processed").value(true));
    }

    @Test
    void shouldGetPaymentComparison() throws Exception {
        mockMvc.perform(get("/api/template-method-demo/demo/payment-comparison"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditCard").exists())
                .andExpect(jsonPath("$.crypto").exists())
                .andExpect(jsonPath("$.creditCard.success").value(true))
                .andExpect(jsonPath("$.crypto.success").value(true));
    }
}
