package com.designpatterns.showcase.templatemethod.payment;

import com.designpatterns.showcase.templatemethod.dto.PaymentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CryptoPaymentWorkflowTest {

    @Autowired
    private CryptoPaymentWorkflow cryptoPaymentWorkflow;

    private PaymentContext context;

    @BeforeEach
    void setUp() {
        context = PaymentContext.builder()
                .transactionId("TEST-CRYPTO-TXN-001")
                .customerId("CUST-001")
                .amount(BigDecimal.valueOf(0.005))
                .currency("BTC")
                .paymentMethod("CRYPTO")
                .build();
    }

    @Test
    void shouldProcessCryptoPaymentSuccessfully() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isValid()).isTrue();
        assertThat(result.isAuthorized()).isTrue();
        assertThat(result.isCaptured()).isTrue();
        assertThat(result.getAuthorizationCode()).isNotNull();
        assertThat(result.getReceiptNumber()).isNotNull();
    }

    @Test
    void shouldGenerateBlockchainHash() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.getAuthorizationCode()).startsWith("BLOCKCHAIN-");
        assertThat(result.getAuthorizationCode().length()).isGreaterThan(16);
    }

    @Test
    void shouldGenerateCryptoReceipt() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.getReceiptNumber()).startsWith("RECEIPT-CRYPTO-");
    }

    @Test
    void shouldValidateSupportedCryptocurrencies() {
        context.setCurrency("BTC");
        PaymentContext btcResult = cryptoPaymentWorkflow.executePayment(context);
        assertThat(btcResult.isValid()).isTrue();

        PaymentContext ethContext = PaymentContext.builder()
                .transactionId("TEST-ETH-001")
                .customerId("CUST-001")
                .amount(BigDecimal.valueOf(0.1))
                .currency("ETH")
                .build();
        PaymentContext ethResult = cryptoPaymentWorkflow.executePayment(ethContext);
        assertThat(ethResult.isValid()).isTrue();
    }

    @Test
    void shouldFailWhenCurrencyIsUnsupported() {
        context.setCurrency("DOGE");

        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Unsupported cryptocurrency");
    }

    @Test
    void shouldLogBlockchainProcessingSteps() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("blockchain"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("network"));
        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("confirmations"));
    }

    @Test
    void shouldVerifyBlockchainNetwork() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> 
            log.contains("Blockchain network") && log.contains("operational"));
    }

    @Test
    void shouldWaitForConfirmations() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("6 confirmations"));
    }

    @Test
    void shouldExecuteCryptoPostProcessing() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> 
            log.contains("wallet balance") || log.contains("tax reporting"));
    }

    @Test
    void shouldSendBlockchainExplorerLink() {
        PaymentContext result = cryptoPaymentWorkflow.executePayment(context);

        assertThat(result.getProcessingLog()).anyMatch(log -> log.contains("explorer"));
    }
}
