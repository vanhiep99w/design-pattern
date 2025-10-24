package com.designpatterns.showcase.builder;

import com.designpatterns.showcase.builder.domain.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceItemBuilderTest {

    @Test
    void shouldBuildValidInvoiceItem() {
        InvoiceItem item = InvoiceItem.builder()
                .description("Consulting Services")
                .quantity(5)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        assertNotNull(item);
        assertEquals("Consulting Services", item.getDescription());
        assertEquals(5, item.getQuantity());
        assertEquals(new BigDecimal("100.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("500.00"), item.getTotal());
    }

    @Test
    void shouldCalculateTotalCorrectly() {
        InvoiceItem item = InvoiceItem.builder()
                .description("Product X")
                .quantity(3)
                .unitPrice(new BigDecimal("25.50"))
                .build();

        assertEquals(new BigDecimal("76.50"), item.getTotal());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionMissing() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            InvoiceItem.builder()
                    .quantity(1)
                    .unitPrice(new BigDecimal("10.00"))
                    .build();
        });

        assertEquals("Item description is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionEmpty() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            InvoiceItem.builder()
                    .description("   ")
                    .quantity(1)
                    .unitPrice(new BigDecimal("10.00"))
                    .build();
        });

        assertEquals("Item description is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuantityZero() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            InvoiceItem.builder()
                    .description("Test Item")
                    .quantity(0)
                    .unitPrice(new BigDecimal("10.00"))
                    .build();
        });

        assertEquals("Item quantity must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuantityNegative() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            InvoiceItem.builder()
                    .description("Test Item")
                    .quantity(-5)
                    .unitPrice(new BigDecimal("10.00"))
                    .build();
        });

        assertEquals("Item quantity must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceNull() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            InvoiceItem.builder()
                    .description("Test Item")
                    .quantity(1)
                    .build();
        });

        assertEquals("Item unit price must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceZero() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            InvoiceItem.builder()
                    .description("Test Item")
                    .quantity(1)
                    .unitPrice(BigDecimal.ZERO)
                    .build();
        });

        assertEquals("Item unit price must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceNegative() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            InvoiceItem.builder()
                    .description("Test Item")
                    .quantity(1)
                    .unitPrice(new BigDecimal("-10.00"))
                    .build();
        });

        assertEquals("Item unit price must be positive", exception.getMessage());
    }

    @Test
    void shouldSupportMethodChaining() {
        InvoiceItem.Builder builder = InvoiceItem.builder();

        InvoiceItem.Builder result1 = builder.description("Item");
        InvoiceItem.Builder result2 = result1.quantity(1);
        InvoiceItem.Builder result3 = result2.unitPrice(new BigDecimal("10.00"));

        assertSame(builder, result1);
        assertSame(result1, result2);
        assertSame(result2, result3);
    }
}
