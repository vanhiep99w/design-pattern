package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorFactoryTest {

    private PaymentProcessorFactory factory;
    private CreditCardPaymentProcessor creditCardProcessor;
    private PayPalPaymentProcessor payPalProcessor;
    private CryptoPaymentProcessor cryptoProcessor;

    @BeforeEach
    void setUp() {
        creditCardProcessor = new CreditCardPaymentProcessor();
        payPalProcessor = new PayPalPaymentProcessor();
        cryptoProcessor = new CryptoPaymentProcessor();
        
        factory = new PaymentProcessorFactory(
                Arrays.asList(creditCardProcessor, payPalProcessor, cryptoProcessor)
        );
    }

    @Test
    void shouldReturnCreditCardProcessor() {
        PaymentProcessor processor = factory.getPaymentProcessor("CREDIT_CARD");
        
        assertNotNull(processor);
        assertEquals("CREDIT_CARD", processor.getPaymentType());
        assertInstanceOf(CreditCardPaymentProcessor.class, processor);
    }

    @Test
    void shouldReturnPayPalProcessor() {
        PaymentProcessor processor = factory.getPaymentProcessor("PAYPAL");
        
        assertNotNull(processor);
        assertEquals("PAYPAL", processor.getPaymentType());
        assertInstanceOf(PayPalPaymentProcessor.class, processor);
    }

    @Test
    void shouldReturnCryptoProcessor() {
        PaymentProcessor processor = factory.getPaymentProcessor("CRYPTOCURRENCY");
        
        assertNotNull(processor);
        assertEquals("CRYPTOCURRENCY", processor.getPaymentType());
        assertInstanceOf(CryptoPaymentProcessor.class, processor);
    }

    @Test
    void shouldHandleCaseInsensitivePaymentType() {
        PaymentProcessor processor1 = factory.getPaymentProcessor("credit_card");
        PaymentProcessor processor2 = factory.getPaymentProcessor("CrEdIt_CaRd");
        PaymentProcessor processor3 = factory.getPaymentProcessor("CREDIT_CARD");
        
        assertEquals("CREDIT_CARD", processor1.getPaymentType());
        assertEquals("CREDIT_CARD", processor2.getPaymentType());
        assertEquals("CREDIT_CARD", processor3.getPaymentType());
    }

    @Test
    void shouldHandlePaymentTypeWithWhitespace() {
        PaymentProcessor processor = factory.getPaymentProcessor(" PAYPAL ");
        
        assertNotNull(processor);
        assertEquals("PAYPAL", processor.getPaymentType());
    }

    @Test
    void shouldThrowExceptionForUnsupportedPaymentType() {
        UnsupportedPaymentTypeException exception = assertThrows(
                UnsupportedPaymentTypeException.class,
                () -> factory.getPaymentProcessor("BANK_TRANSFER")
        );
        
        assertTrue(exception.getMessage().contains("Unsupported payment type"));
    }

    @Test
    void shouldThrowExceptionForNullPaymentType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getPaymentProcessor(null)
        );
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldReturnTrueForSupportedPaymentType() {
        assertTrue(factory.isPaymentTypeSupported("CREDIT_CARD"));
        assertTrue(factory.isPaymentTypeSupported("PAYPAL"));
        assertTrue(factory.isPaymentTypeSupported("CRYPTOCURRENCY"));
    }

    @Test
    void shouldReturnFalseForUnsupportedPaymentType() {
        assertFalse(factory.isPaymentTypeSupported("BANK_TRANSFER"));
        assertFalse(factory.isPaymentTypeSupported("CASH"));
    }

    @Test
    void shouldReturnFalseForNullPaymentType() {
        assertFalse(factory.isPaymentTypeSupported(null));
    }

    @Test
    void shouldReturnAllProcessors() {
        Map<String, PaymentProcessor> processors = factory.getAllProcessors();
        
        assertEquals(3, processors.size());
        assertTrue(processors.containsKey("CREDIT_CARD"));
        assertTrue(processors.containsKey("PAYPAL"));
        assertTrue(processors.containsKey("CRYPTOCURRENCY"));
    }

    @Test
    void shouldReturnImmutableCopyOfProcessors() {
        Map<String, PaymentProcessor> processors = factory.getAllProcessors();
        processors.clear();
        
        Map<String, PaymentProcessor> processorsAgain = factory.getAllProcessors();
        assertEquals(3, processorsAgain.size());
    }
}
