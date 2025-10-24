package com.designpatterns.showcase.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component("cryptoProcessor")
public class CryptoPaymentProcessor implements PaymentProcessor {

    private static final Pattern BITCOIN_ADDRESS_PATTERN = Pattern.compile("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$|^bc1[a-z0-9]{39,59}$");
    private static final Pattern ETHEREUM_ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");
    private static final BigDecimal NETWORK_FEE_BTC = new BigDecimal("0.0001");
    private static final BigDecimal NETWORK_FEE_ETH = new BigDecimal("0.001");
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("10.00");
    private static final int CONFIRMATION_BLOCKS_REQUIRED = 3;

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing crypto payment for customer: {}", request.getCustomerId());

        if (!validatePaymentDetails(request)) {
            return PaymentResult.builder()
                    .success(false)
                    .message("Invalid cryptocurrency wallet details")
                    .paymentType(getPaymentType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (request.getAmount().compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            return PaymentResult.builder()
                    .success(false)
                    .message("Transaction amount is below minimum of " + MIN_TRANSACTION_AMOUNT)
                    .paymentType(getPaymentType())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String cryptoType = request.getPaymentDetails().get("cryptoType");
        BigDecimal cryptoAmount = convertToCrypto(request.getAmount(), cryptoType);
        BigDecimal networkFee = getNetworkFee(cryptoType);
        BigDecimal totalCrypto = cryptoAmount.add(networkFee);

        String transactionId = "CRYPTO-" + cryptoType + "-" + UUID.randomUUID().toString();
        String blockchainHash = generateBlockchainHash();
        
        log.info("Crypto payment processed. Transaction ID: {}, Blockchain Hash: {}, Amount: {} {}", 
                transactionId, blockchainHash, totalCrypto, cryptoType);

        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .message(String.format("Payment processed successfully via %s. Amount: %s %s (+ %s network fee). " +
                        "Blockchain Hash: %s. Confirmations required: %d", 
                        cryptoType, cryptoAmount, cryptoType, networkFee, blockchainHash, CONFIRMATION_BLOCKS_REQUIRED))
                .processedAmount(request.getAmount())
                .paymentType(getPaymentType())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean validatePaymentDetails(PaymentRequest request) {
        Map<String, String> details = request.getPaymentDetails();
        
        if (details == null) {
            log.warn("Payment details are null");
            return false;
        }

        String walletAddress = details.get("walletAddress");
        String cryptoType = details.get("cryptoType");

        if (cryptoType == null || (!cryptoType.equals("BTC") && !cryptoType.equals("ETH"))) {
            log.warn("Unsupported cryptocurrency type: {}", cryptoType);
            return false;
        }

        if (walletAddress == null) {
            log.warn("Wallet address is missing");
            return false;
        }

        boolean isValid = false;
        if (cryptoType.equals("BTC")) {
            isValid = BITCOIN_ADDRESS_PATTERN.matcher(walletAddress).matches();
        } else if (cryptoType.equals("ETH")) {
            isValid = ETHEREUM_ADDRESS_PATTERN.matcher(walletAddress).matches();
        }

        if (!isValid) {
            log.warn("Invalid wallet address format for {}", cryptoType);
            return false;
        }

        String signature = details.get("signature");
        if (signature == null || signature.trim().isEmpty()) {
            log.warn("Transaction signature is missing");
            return false;
        }

        return true;
    }

    @Override
    public String getPaymentType() {
        return "CRYPTOCURRENCY";
    }

    private BigDecimal convertToCrypto(BigDecimal usdAmount, String cryptoType) {
        if (cryptoType.equals("BTC")) {
            return usdAmount.divide(new BigDecimal("45000"), 8, RoundingMode.HALF_UP);
        } else if (cryptoType.equals("ETH")) {
            return usdAmount.divide(new BigDecimal("3000"), 8, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getNetworkFee(String cryptoType) {
        if (cryptoType.equals("BTC")) {
            return NETWORK_FEE_BTC;
        } else if (cryptoType.equals("ETH")) {
            return NETWORK_FEE_ETH;
        }
        return BigDecimal.ZERO;
    }

    private String generateBlockchainHash() {
        return "0x" + UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
