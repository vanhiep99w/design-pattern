package com.designpatterns.showcase.builder;

import com.designpatterns.showcase.builder.domain.Invoice;
import com.designpatterns.showcase.builder.domain.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceBuilderTest {

    @Test
    void shouldBuildValidInvoice() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Software License")
                .quantity(1)
                .unitPrice(new BigDecimal("999.99"))
                .build());

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-001")
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .customerAddress("123 Main St")
                .items(items)
                .build();

        assertNotNull(invoice);
        assertEquals("INV-001", invoice.getInvoiceNumber());
        assertEquals("John Doe", invoice.getCustomerName());
        assertEquals("john.doe@example.com", invoice.getCustomerEmail());
        assertEquals(1, invoice.getItems().size());
        assertEquals(new BigDecimal("999.99"), invoice.getSubtotal());
        assertEquals(new BigDecimal("999.99"), invoice.getTotalAmount());
    }

    @Test
    void shouldCalculateTaxCorrectly() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Product A")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .build());

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-002")
                .customerName("Jane Smith")
                .customerEmail("jane@example.com")
                .items(items)
                .taxRate(new BigDecimal("0.10"))
                .build();

        assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("10.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("110.00"), invoice.getTotalAmount());
    }

    @Test
    void shouldApplyDiscountBeforeTax() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Product B")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build());

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-003")
                .customerName("Bob Johnson")
                .customerEmail("bob@example.com")
                .items(items)
                .discountAmount(new BigDecimal("20.00"))
                .taxRate(new BigDecimal("0.10"))
                .build();

        assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("20.00"), invoice.getDiscountAmount());
        assertEquals(new BigDecimal("8.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("88.00"), invoice.getTotalAmount());
    }

    @Test
    void shouldThrowExceptionWhenInvoiceNumberMissing() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Invoice.builder()
                    .customerName("Test Customer")
                    .customerEmail("test@example.com")
                    .items(items)
                    .build();
        });

        assertEquals("Invoice number is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNameMissing() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Invoice.builder()
                    .invoiceNumber("INV-004")
                    .customerEmail("test@example.com")
                    .items(items)
                    .build();
        });

        assertEquals("Customer name is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailInvalid() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Invoice.builder()
                    .invoiceNumber("INV-005")
                    .customerName("Test Customer")
                    .customerEmail("invalid-email")
                    .items(items)
                    .build();
        });

        assertEquals("Customer email is invalid", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoItems() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Invoice.builder()
                    .invoiceNumber("INV-006")
                    .customerName("Test Customer")
                    .customerEmail("test@example.com")
                    .items(new ArrayList<>())
                    .build();
        });

        assertEquals("Invoice must have at least one item", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDueDateBeforeIssueDate() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Invoice.builder()
                    .invoiceNumber("INV-007")
                    .customerName("Test Customer")
                    .customerEmail("test@example.com")
                    .items(items)
                    .issueDate(LocalDate.now())
                    .dueDate(LocalDate.now().minusDays(1))
                    .build();
        });

        assertEquals("Due date cannot be before issue date", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNegativeTaxRate() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Invoice.builder()
                    .invoiceNumber("INV-008")
                    .customerName("Test Customer")
                    .customerEmail("test@example.com")
                    .items(items)
                    .taxRate(new BigDecimal("-0.10"))
                    .build();
        });

        assertEquals("Tax rate cannot be negative", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDiscountExceedsSubtotal() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Invoice.builder()
                    .invoiceNumber("INV-009")
                    .customerName("Test Customer")
                    .customerEmail("test@example.com")
                    .items(items)
                    .discountAmount(new BigDecimal("20.00"))
                    .build();
        });

        assertEquals("Discount amount cannot be greater than subtotal", exception.getMessage());
    }

    @Test
    void shouldEnsureInvoiceIsImmutable() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-010")
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .items(items)
                .build();

        items.clear();

        assertEquals(1, invoice.getItems().size());

        assertThrows(UnsupportedOperationException.class, () -> {
            invoice.getItems().clear();
        });
    }

    @Test
    void shouldSetDefaultValues() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Test Item")
                .quantity(1)
                .unitPrice(new BigDecimal("10.00"))
                .build());

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-011")
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .items(items)
                .build();

        assertNotNull(invoice.getIssueDate());
        assertNotNull(invoice.getDueDate());
        assertEquals("Net 30", invoice.getPaymentTerms());
        assertEquals(BigDecimal.ZERO, invoice.getDiscountAmount());
    }
}
